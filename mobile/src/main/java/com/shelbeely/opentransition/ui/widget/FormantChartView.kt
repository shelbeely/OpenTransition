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
import android.util.AttributeSet
import android.view.View
import com.shelbeely.opentransition.data.AudioAnalysis

/**
 * A custom view that displays a formant chart (F1 vs F2 plot).
 * This helps visualize voice characteristics and track changes over time.
 */
class FormantChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val axisPaint = Paint().apply {
        color = Color.WHITE
        alpha = 179 // 70% opacity
        strokeWidth = 2f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val gridPaint = Paint().apply {
        color = Color.WHITE
        alpha = 64 // 25% opacity
        strokeWidth = 1f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val pointPaint = Paint().apply {
        color = Color.parseColor("#4CAF50") // Material Green
        alpha = 255
        strokeWidth = 8f
        isAntiAlias = true
        style = Paint.Style.FILL_AND_STROKE
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        alpha = 230
        textSize = 28f
        isAntiAlias = true
    }

    // Reusable Paints to avoid per-draw allocations (fixes DrawAllocation lint warning).
    private val pointPaintColored = Paint(pointPaint)
    private val labelPaint = Paint(textPaint).apply {
        textSize = 18f
        textAlign = Paint.Align.CENTER
    }

    private val data = mutableListOf<AudioAnalysis>()

    // Formant chart typical ranges (in Hz)
    private val f2Min = 800f
    private val f2Max = 2800f
    private val f1Min = 200f
    private val f1Max = 1000f

    fun setData(analyses: List<AudioAnalysis>) {
        data.clear()
        data.addAll(analyses)
        invalidate()
    }
    
    fun addDataPoint(analysis: AudioAnalysis) {
        data.add(analysis)
        invalidate()
    }
    
    fun clearData() {
        data.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = 80f
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding

        // Draw axes
        // X-axis (F2 - horizontal)
        canvas.drawLine(padding, height - padding, width - padding, height - padding, axisPaint)
        // Y-axis (F1 - vertical, inverted because higher formants = higher pitch)
        canvas.drawLine(padding, padding, padding, height - padding, axisPaint)

        // Draw grid lines
        for (i in 0..4) {
            val x = padding + (chartWidth * i / 4)
            val y = padding + (chartHeight * i / 4)
            
            // Vertical grid lines
            canvas.drawLine(x, padding, x, height - padding, gridPaint)
            // Horizontal grid lines
            canvas.drawLine(padding, y, width - padding, y, gridPaint)
        }

        // Draw axis labels
        canvas.drawText("F2 (Hz)", width / 2f - 40f, height - padding + 50f, textPaint)
        
        canvas.save()
        canvas.rotate(-90f, padding - 60f, height / 2f)
        canvas.drawText("F1 (Hz)", padding - 60f, height / 2f, textPaint)
        canvas.restore()

        // Draw F2 axis values
        for (i in 0..4) {
            val f2Value = f2Min + (f2Max - f2Min) * i / 4
            val x = padding + (chartWidth * i / 4)
            canvas.drawText(
                "${f2Value.toInt()}",
                x - 20f,
                height - padding + 30f,
                textPaint.apply { textSize = 24f }
            )
        }

        // Draw F1 axis values (inverted)
        for (i in 0..4) {
            val f1Value = f1Max - (f1Max - f1Min) * i / 4
            val y = padding + (chartHeight * i / 4)
            canvas.drawText(
                "${f1Value.toInt()}",
                padding - 60f,
                y + 8f,
                textPaint.apply { textSize = 24f }
            )
        }

        textPaint.textSize = 28f // Reset text size

        // Draw reference regions first (so points appear on top)
        drawReferenceRegions(canvas, padding, chartWidth, chartHeight)

        // Plot data points with color gradient (oldest = blue, newest = green)
        data.forEachIndexed { index, analysis ->
            val x = mapF2ToX(analysis.f2Mean, padding, chartWidth)
            val y = mapF1ToY(analysis.f1Mean, padding, chartHeight)
            
            // Color gradient from blue to green based on chronological order
            val progress = if (data.size > 1) index.toFloat() / (data.size - 1) else 0.5f
            val color = interpolateColor(
                Color.parseColor("#2196F3"), // Blue (oldest)
                Color.parseColor("#4CAF50"), // Green (newest)
                progress
            )
            
            pointPaintColored.color = color
            canvas.drawCircle(x, y, 12f, pointPaintColored)
            
            // Draw small label with index (uses pre-allocated labelPaint)
            canvas.drawText("${index + 1}", x, y - 18f, labelPaint)
        }
    }
    
    private fun interpolateColor(startColor: Int, endColor: Int, fraction: Float): Int {
        val startA = Color.alpha(startColor)
        val startR = Color.red(startColor)
        val startG = Color.green(startColor)
        val startB = Color.blue(startColor)
        
        val endA = Color.alpha(endColor)
        val endR = Color.red(endColor)
        val endG = Color.green(endColor)
        val endB = Color.blue(endColor)
        
        return Color.argb(
            (startA + fraction * (endA - startA)).toInt(),
            (startR + fraction * (endR - startR)).toInt(),
            (startG + fraction * (endG - startG)).toInt(),
            (startB + fraction * (endB - startB)).toInt()
        )
    }

    private fun mapF2ToX(f2: Float, padding: Float, chartWidth: Float): Float {
        val normalized = (f2 - f2Min) / (f2Max - f2Min)
        return padding + normalized * chartWidth
    }

    private fun mapF1ToY(f1: Float, padding: Float, chartHeight: Float): Float {
        // Invert Y axis so lower F1 (higher tongue) is at top
        val normalized = (f1 - f1Min) / (f1Max - f1Min)
        return padding + chartHeight - (normalized * chartHeight)
    }

    private fun drawReferenceRegions(canvas: Canvas, padding: Float, chartWidth: Float, chartHeight: Float) {
        val referencePaint = Paint().apply {
            color = Color.WHITE
            alpha = 40
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        val labelPaint = Paint().apply {
            color = Color.WHITE
            alpha = 180
            textSize = 32f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        // Typical feminine range (higher F1, higher F2)
        val femX1 = mapF2ToX(1500f, padding, chartWidth)
        val femX2 = mapF2ToX(2500f, padding, chartWidth)
        val femY1 = mapF1ToY(850f, padding, chartHeight)
        val femY2 = mapF1ToY(600f, padding, chartHeight)
        
        canvas.drawRect(femX1, femY2, femX2, femY1, referencePaint.apply { 
            color = Color.parseColor("#E91E63") // Pink
            alpha = 30
        })
        canvas.drawText("Fem", (femX1 + femX2) / 2, (femY1 + femY2) / 2, labelPaint)

        // Typical masculine range (lower F1, lower F2)
        val mascX1 = mapF2ToX(1000f, padding, chartWidth)
        val mascX2 = mapF2ToX(1600f, padding, chartWidth)
        val mascY1 = mapF1ToY(650f, padding, chartHeight)
        val mascY2 = mapF1ToY(400f, padding, chartHeight)
        
        canvas.drawRect(mascX1, mascY2, mascX2, mascY1, referencePaint.apply {
            color = Color.parseColor("#2196F3") // Blue
            alpha = 30
        })
        canvas.drawText("Masc", (mascX1 + mascX2) / 2, (mascY1 + mascY2) / 2, labelPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 800
        val desiredHeight = 600

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
