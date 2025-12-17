/*
 * Copyright © 2025 OpenTransition Contributors. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.background

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark

/**
 * ImageAnalysis.Analyzer that detects faces and landmarks in real-time camera preview.
 * 
 * This analyzer uses ML Kit Face Detection to:
 * - Confirm a face is in frame (not saving blank room pics)
 * - Provide "chin up/down" guidance via head pose angles
 * - Track framing consistency via face size (distance estimate)
 * - Detect facial landmarks for alignment guides
 */
class FaceDetectionAnalyzer(
    private val onFaceDetected: (FaceDetectionResult) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .enableTracking()
        .build()

    private val detector = FaceDetection.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            detector.process(image)
                .addOnSuccessListener { faces ->
                    val result = if (faces.isNotEmpty()) {
                        processFace(faces[0], imageProxy)
                    } else {
                        FaceDetectionResult.NoFace
                    }
                    onFaceDetected(result)
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                    onFaceDetected(FaceDetectionResult.Error(exception))
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun processFace(face: Face, imageProxy: ImageProxy): FaceDetectionResult {
        // Get head pose angles for "chin up/down" guidance
        val pitch = face.headEulerAngleX // Nod (up/down)
        val yaw = face.headEulerAngleY // Turn (left/right)
        val roll = face.headEulerAngleZ // Tilt (shoulder to shoulder)

        // Calculate face size for distance estimation
        val faceBounds = face.boundingBox
        val faceSize = faceBounds.width() * faceBounds.height()
        val imageSize = imageProxy.width * imageProxy.height
        val faceRatio = faceSize.toFloat() / imageSize.toFloat()

        // Get key landmarks
        val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
        val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position
        val nose = face.getLandmark(FaceLandmark.NOSE_BASE)?.position
        val mouth = face.getLandmark(FaceLandmark.MOUTH_BOTTOM)?.position

        // Convert Rect to RectF
        val faceRectF = android.graphics.RectF(faceBounds)

        return FaceDetectionResult.FaceDetected(
            bounds = faceRectF,
            headPitch = pitch,
            headYaw = yaw,
            headRoll = roll,
            faceRatio = faceRatio,
            leftEyePos = leftEye,
            rightEyePos = rightEye,
            nosePos = nose,
            mouthPos = mouth,
            trackingId = face.trackingId
        )
    }

    fun close() {
        detector.close()
    }
}

/**
 * Result of face detection analysis
 */
sealed class FaceDetectionResult {
    /** No face detected in frame */
    object NoFace : FaceDetectionResult()

    /** Face detected with pose and landmark information */
    data class FaceDetected(
        val bounds: android.graphics.RectF,
        val headPitch: Float,  // Pitch: positive = looking up, negative = looking down
        val headYaw: Float,    // Yaw: positive = turned right, negative = turned left
        val headRoll: Float,   // Roll: positive = tilted right, negative = tilted left
        val faceRatio: Float,  // Face size relative to image (for distance estimation)
        val leftEyePos: android.graphics.PointF?,
        val rightEyePos: android.graphics.PointF?,
        val nosePos: android.graphics.PointF?,
        val mouthPos: android.graphics.PointF?,
        val trackingId: Int?
    ) : FaceDetectionResult()

    /** Error during face detection */
    data class Error(val exception: Exception) : FaceDetectionResult()
}
