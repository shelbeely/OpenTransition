/*
 * Copyright © 2018 TransTracks. All rights reserved.
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
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.shelbeely.opentransition.data.AudioAnalysis
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

/**
 * A custom view that displays pitch progression over time.
 * Shows F0 (fundamental frequency/pitch) changes across multiple recordings.
 */
class PitchProgressionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val axisPaint = Paint().apply {
        color = Color.WHITE
        alpha = 179
        strokeWidth = 2f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val gridPaint = Paint().apply {
        color = Color.WHITE
        alpha = 64
        strokeWidth = 1f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val linePaint = Paint().apply {
        color = Color.parseColor("#4CAF50") // Material Green
        alpha = 255
        strokeWidth = 4f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val pointPaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        alpha = 255
        strokeWidth = 10f
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        alpha = 230
        textSize = 24f
        isAntiAlias = true
    }

    private val rangeLinePaint = Paint().apply {
        color = Color.parseColor("#2196F3") // Material Blue
        alpha = 100
        strokeWidth = 2f
        isAntiAlias = true
        style = Paint.Style.STROKE
        pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 5f), 0f)
    }

    private data class DataPoint(
        val date: LocalDate,
        val f0Mean: Float,
        val f0Min: Float,
        val f0Max: Float
    )

    private val data = mutableListOf<DataPoint>()

    // Reusable Path to avoid per-frame allocation in onDraw (fixes DrawAllocation lint warning).
    private val linePath = Path()

    fun setData(analyses: List<Pair<LocalDate, AudioAnalysis>>) {
        data.clear()
        analyses.sortedBy { it.first }.forEach { (date, analysis) ->
            data.add(DataPoint(date, analysis.f0Mean, analysis.f0Min, analysis.f0Max))
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.isEmpty()) {
            drawEmptyState(canvas)
            return
        }

        val padding = 80f
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding

        // Calculate pitch range
        val minPitch = data.minOfOrNull { it.f0Min } ?: 0f
        val maxPitch = data.maxOfOrNull { it.f0Max } ?: 300f
        val pitchRange = maxPitch - minPitch
        val pitchPadding = pitchRange * 0.1f

        val yMin = minPitch - pitchPadding
        val yMax = maxPitch + pitchPadding

        // Draw axes
        canvas.drawLine(padding, padding, padding, height - padding, axisPaint)
        canvas.drawLine(padding, height - padding, width - padding, height - padding, axisPaint)

        // Draw grid lines
        for (i in 0..4) {
            val y = padding + (chartHeight * i / 4)
            canvas.drawLine(padding, y, width - padding, y, gridPaint)

            // Y-axis labels (pitch values)
            val pitchValue = yMax - (yMax - yMin) * i / 4
            canvas.drawText(
                "${pitchValue.toInt()} Hz",
                10f,
                y + 8f,
                textPaint
            )
        }

        // Draw data
        if (data.size == 1) {
            // Single point
            val x = padding + chartWidth / 2
            val y = mapPitchToY(data[0].f0Mean, yMin, yMax, padding, chartHeight)
            canvas.drawCircle(x, y, 8f, pointPaint)
        } else {
            // Draw line connecting mean values (reuses pre-allocated linePath)
            val path = linePath.apply { reset() }
            data.forEachIndexed { index, point ->
                val x = padding + (chartWidth * index / (data.size - 1))
                val y = mapPitchToY(point.f0Mean, yMin, yMax, padding, chartHeight)

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }

                // Draw data point
                canvas.drawCircle(x, y, 6f, pointPaint)

                // Draw min/max range as vertical line
                val yMin = mapPitchToY(point.f0Min, yMin, yMax, padding, chartHeight)
                val yMax = mapPitchToY(point.f0Max, yMin, yMax, padding, chartHeight)
                canvas.drawLine(x, yMin, x, yMax, rangeLinePaint)
            }

            canvas.drawPath(path, linePaint)
        }

        // Draw X-axis labels (dates)
        drawDateLabels(canvas, padding, chartWidth)
    }

    private fun mapPitchToY(pitch: Float, yMin: Float, yMax: Float, padding: Float, chartHeight: Float): Float {
        val normalized = (pitch - yMin) / (yMax - yMin)
        return padding + chartHeight - (normalized * chartHeight)
    }

    private fun drawDateLabels(canvas: Canvas, padding: Float, chartWidth: Float) {
        val labelCount = min(data.size, 5)
        val step = max(1, data.size / labelCount)

        for (i in 0 until data.size step step) {
            val x = padding + (chartWidth * i / max(1, data.size - 1))
            val dateStr = formatDate(data[i].date)

            canvas.save()
            canvas.rotate(-45f, x, height - padding + 20f)
            canvas.drawText(dateStr, x, height - padding + 30f, textPaint.apply { textSize = 20f })
            canvas.restore()
        }

        textPaint.textSize = 24f // Reset
    }

    private fun formatDate(date: LocalDate): String {
        return "${date.monthValue}/${date.dayOfMonth}"
    }

    private fun drawEmptyState(canvas: Canvas) {
        val emptyTextPaint = Paint(textPaint).apply {
            textSize = 32f
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText(
            "No recordings to compare",
            width / 2f,
            height / 2f,
            emptyTextPaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 800
        val desiredHeight = 400

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> minOf(desiredWidth, widthSize)
            else -> desiredWidth
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }
}
