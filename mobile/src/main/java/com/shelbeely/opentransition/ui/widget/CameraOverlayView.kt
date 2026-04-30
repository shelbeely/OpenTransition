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

package com.shelbeely.opentransition.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.background.FaceDetectionResult
import kotlin.math.abs

/**
 * Custom overlay view for displaying face detection guides on camera preview.
 * 
 * Features:
 * - Face alignment box
 * - Head tilt indicator
 * - "Chin up/down" guidance
 * - Face detected indicator
 */
class CameraOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var faceResult: FaceDetectionResult = FaceDetectionResult.NoFace
    private var overlaysEnabled = true

    // Paints for drawing
    private val faceBoxPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val guidePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
        color = Color.WHITE
    }

    private val textPaint = Paint().apply {
        textSize = 48f
        isAntiAlias = true
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }

    private val landmarkPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = Color.CYAN
    }

    // Ideal pose thresholds
    private val idealPitchRange = -10f..10f  // Neutral head angle
    private val idealYawRange = -15f..15f    // Face forward
    private val idealRollRange = -10f..10f   // Head level
    private val idealFaceRatioRange = 0.08f..0.15f  // Appropriate distance

    fun updateFaceDetection(result: FaceDetectionResult) {
        faceResult = result
        invalidate()
    }

    fun setOverlaysEnabled(enabled: Boolean) {
        overlaysEnabled = enabled
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!overlaysEnabled) return

        when (val result = faceResult) {
            is FaceDetectionResult.FaceDetected -> drawFaceOverlay(canvas, result)
            is FaceDetectionResult.NoFace -> drawNoFaceMessage(canvas)
            is FaceDetectionResult.Error -> drawErrorMessage(canvas)
        }
    }

    private fun drawFaceOverlay(canvas: Canvas, result: FaceDetectionResult.FaceDetected) {
        // Determine if pose is good
        val pitchGood = result.headPitch in idealPitchRange
        val yawGood = result.headYaw in idealYawRange
        val rollGood = result.headRoll in idealRollRange
        val distanceGood = result.faceRatio in idealFaceRatioRange
        val allGood = pitchGood && yawGood && rollGood && distanceGood

        // Update face box color based on alignment
        faceBoxPaint.color = when {
            allGood -> ContextCompat.getColor(context, R.color.success_green)
            else -> ContextCompat.getColor(context, R.color.warning_yellow)
        }

        // Draw face bounding box
        canvas.drawRect(result.bounds, faceBoxPaint)

        // Draw shoulder line guide (horizontal line through face bottom)
        val shoulderY = result.bounds.bottom + 100f
        canvas.drawLine(
            result.bounds.left,
            shoulderY,
            result.bounds.right,
            shoulderY,
            guidePaint
        )

        // Draw center alignment guide
        val centerX = width / 2f
        val savedAlpha = guidePaint.alpha
        guidePaint.alpha = 64 // Semi-transparent
        canvas.drawLine(centerX, 0f, centerX, height.toFloat(), guidePaint)
        guidePaint.alpha = savedAlpha // Restore alpha

        // Draw landmarks
        result.leftEyePos?.let { drawLandmark(canvas, it) }
        result.rightEyePos?.let { drawLandmark(canvas, it) }
        result.nosePos?.let { drawLandmark(canvas, it) }
        result.mouthPos?.let { drawLandmark(canvas, it) }

        // Draw guidance text
        drawGuidanceText(canvas, result, pitchGood, yawGood, rollGood, distanceGood, allGood)
    }

    private fun drawLandmark(canvas: Canvas, point: PointF) {
        canvas.drawCircle(point.x, point.y, 6f, landmarkPaint)
    }

    private fun drawGuidanceText(
        canvas: Canvas,
        result: FaceDetectionResult.FaceDetected,
        pitchGood: Boolean,
        yawGood: Boolean,
        rollGood: Boolean,
        distanceGood: Boolean,
        allGood: Boolean
    ) {
        val messages = mutableListOf<String>()

        // Head pitch guidance (chin up/down)
        if (!pitchGood) {
            messages.add(
                when {
                    result.headPitch > idealPitchRange.endInclusive -> "Chin down"
                    result.headPitch < idealPitchRange.start -> "Chin up"
                    else -> ""
                }
            )
        }

        // Yaw guidance (turn face)
        if (!yawGood) {
            messages.add(
                when {
                    result.headYaw > idealYawRange.endInclusive -> "Turn left"
                    result.headYaw < idealYawRange.start -> "Turn right"
                    else -> ""
                }
            )
        }

        // Roll guidance (level head)
        if (!rollGood && abs(result.headRoll) > 5f) {
            messages.add("Level your head")
        }

        // Distance guidance
        if (!distanceGood) {
            messages.add(
                when {
                    result.faceRatio < idealFaceRatioRange.start -> "Move closer"
                    result.faceRatio > idealFaceRatioRange.endInclusive -> "Move back"
                    else -> ""
                }
            )
        }

        // All good message
        if (allGood) {
            messages.add("✓ Perfect alignment")
            textPaint.color = ContextCompat.getColor(context, R.color.success_green)
        } else {
            textPaint.color = Color.WHITE
        }

        // Draw messages
        val messageY = 100f
        messages.forEachIndexed { index, message ->
            if (message.isNotEmpty()) {
                canvas.drawText(message, width / 2f, messageY + (index * 60f), textPaint)
            }
        }
    }

    private fun drawNoFaceMessage(canvas: Canvas) {
        textPaint.color = ContextCompat.getColor(context, R.color.warning_yellow)
        canvas.drawText(
            "Position your face in frame",
            width / 2f,
            height / 2f,
            textPaint
        )
    }

    private fun drawErrorMessage(canvas: Canvas) {
        textPaint.color = Color.RED
        canvas.drawText(
            "Face detection error",
            width / 2f,
            height / 2f,
            textPaint
        )
    }
}
