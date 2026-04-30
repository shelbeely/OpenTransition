/*
 * Copyright © 2025-2026 Shelbeely and OpenTransition contributors.
 *
 * Part of OpenTransition, a fork of TransTracks (© 2018-2021 TransTracks),
 * licensed under GPL-3.0-or-later. See the NOTICE and AUTHORS files for
 * full attribution.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.background

import android.content.Context
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.shelbeely.opentransition.util.FileUtil
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Modern CameraX-based camera handler with face detection support.
 * 
 * This handler provides:
 * - Camera preview
 * - Image capture with face detection
 * - Real-time face analysis
 */
class CameraXHandler(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
    private val onFaceDetection: (FaceDetectionResult) -> Unit,
    private val useFrontCamera: Boolean = true  // Default to front camera for selfies
) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var faceDetectionAnalyzer: FaceDetectionAnalyzer? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var isFaceDetectionEnabled = true

    /**
     * Start camera with preview and face detection
     */
    fun startCamera(onSuccess: () -> Unit = {}, onError: (Exception) -> Unit = {}) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
                onSuccess()
            } catch (exc: Exception) {
                exc.printStackTrace()
                onError(exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera not initialized")

        // Preview
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        // Image capture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()

        // Image analysis for face detection
        if (isFaceDetectionEnabled) {
            faceDetectionAnalyzer = FaceDetectionAnalyzer(onFaceDetection)
            
            imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, faceDetectionAnalyzer!!)
                }
        }

        // Select camera based on configuration
        val cameraSelector = if (useFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        try {
            // Unbind all use cases before rebinding
            cameraProvider.unbindAll()

            // Bind use cases to camera
            if (isFaceDetectionEnabled && imageAnalysis != null) {
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture,
                    imageAnalysis
                )
            } else {
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            }
        } catch (exc: Exception) {
            exc.printStackTrace()
        }
    }

    /**
     * Take a photo and save it to a file
     */
    fun takePhoto(onSuccess: (File) -> Unit, onError: (ImageCaptureException) -> Unit) {
        val imageCapture = imageCapture ?: run {
            onError(ImageCaptureException(
                ImageCapture.ERROR_UNKNOWN,
                "Image capture not initialized",
                null
            ))
            return
        }

        // Create output file
        val photoFile = FileUtil.getTempImageFile()

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    onSuccess(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                    onError(exception)
                }
            }
        )
    }

    /**
     * Enable or disable face detection
     */
    fun setFaceDetectionEnabled(enabled: Boolean) {
        if (isFaceDetectionEnabled != enabled) {
            isFaceDetectionEnabled = enabled
            // Rebind camera to update use cases
            cameraProvider?.let { bindCameraUseCases() }
        }
    }

    /**
     * Clean up resources
     */
    fun shutdown() {
        faceDetectionAnalyzer?.close()
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
    }
}
