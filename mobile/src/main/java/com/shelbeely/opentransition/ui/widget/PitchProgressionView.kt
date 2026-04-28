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
import com.shelbeely.opentransition.database.room.entities.AudioAnalysisEntity
import com.shelbeely.opentransition.database.room.entities.VoiceGoalEntity
import com.shelbeely.opentransition.util.VoiceMetric
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.max
import kotlin.math.min

/**
 * A custom view that displays one voice metric over time.
 *
 * The currently displayed metric is selected via [setMetric].  Five metrics
 * are supported — see [VoiceMetric].  If a list of [VoiceGoalEntity] rows is
 * provided for the selected metric, data points that fall within the goal range
 * are decorated with a ring to indicate the goal was met that session.
 *
 * No reference lines, zone backgrounds, or implied "correct" ranges are ever
 * drawn.  The chart only reflects the actual measurements in [setData].
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
        color = Color.parseColor("#4CAF50")
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

    private val goalRingPaint = Paint().apply {
        color = Color.parseColor("#FFC107")  // amber — visible against green
        alpha = 230
        strokeWidth = 3f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        alpha = 230
        textSize = 24f
        isAntiAlias = true
    }

    private data class DataPoint(
        val date: LocalDate,
        val value: Float,
        val hitGoal: Boolean
    )

    private val data = mutableListOf<DataPoint>()
    private var activeMetric: VoiceMetric = VoiceMetric.F0_MEAN

    // Reusable Path
    private val linePath = Path()

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Set which metric to display.  Call [setData] again after changing the
     * metric if you want to refresh from the same analysis list.
     */
    fun setMetric(metric: VoiceMetric) {
        activeMetric = metric
        invalidate()
    }

    /**
     * Supply data to the chart.  [goals] is the list of user-defined goals whose
     * [VoiceGoalEntity.metricKey] matches [activeMetric].  A goal-hit ring is
     * drawn on data points whose value falls within any goal's targetMin…targetMax.
     */
    fun setData(
        analyses: List<Pair<LocalDate, AudioAnalysisEntity>>,
        goals: List<VoiceGoalEntity> = emptyList()
    ) {
        data.clear()
        val metricGoals = goals.filter { it.metricKey == activeMetric.key }
        analyses.sortedBy { it.first }.forEach { (date, analysis) ->
            val value = extractMetricValue(analysis, activeMetric)
            val hitGoal = metricGoals.any { g -> value in g.targetMin..g.targetMax }
            data.add(DataPoint(date, value, hitGoal))
        }
        invalidate()
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.isEmpty()) {
            drawEmptyState(canvas)
            return
        }

        val padding = 80f
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding

        val values = data.map { it.value }
        val rawMin = values.min()
        val rawMax = values.max()
        val valueRange = rawMax - rawMin
        val pad = if (valueRange > 0f) valueRange * 0.1f else 10f
        val yMin = rawMin - pad
        val yMax = rawMax + pad

        // Axes
        canvas.drawLine(padding, padding, padding, height - padding, axisPaint)
        canvas.drawLine(padding, height - padding, width - padding, height - padding, axisPaint)

        // Grid lines + Y labels
        for (i in 0..4) {
            val y = padding + (chartHeight * i / 4)
            canvas.drawLine(padding, y, width - padding, y, gridPaint)
            val labelValue = yMax - (yMax - yMin) * i / 4
            val label = when (activeMetric) {
                VoiceMetric.VOICED_RATIO -> "${(labelValue * 100).toInt()}%"
                VoiceMetric.PITCH_STABILITY_SCORE -> String.format("%.2f", labelValue)
                else -> "${labelValue.toInt()} ${activeMetric.unit}"
            }
            canvas.drawText(label, 4f, y + 8f, textPaint.apply { textSize = 20f })
        }
        textPaint.textSize = 24f

        // Data
        if (data.size == 1) {
            val x = padding + chartWidth / 2
            val y = mapToY(data[0].value, yMin, yMax, padding, chartHeight)
            canvas.drawCircle(x, y, 8f, pointPaint)
            if (data[0].hitGoal) canvas.drawCircle(x, y, 14f, goalRingPaint)
        } else {
            val path = linePath.apply { reset() }
            data.forEachIndexed { index, point ->
                val x = padding + (chartWidth * index / (data.size - 1))
                val y = mapToY(point.value, yMin, yMax, padding, chartHeight)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                canvas.drawCircle(x, y, 6f, pointPaint)
                if (point.hitGoal) canvas.drawCircle(x, y, 12f, goalRingPaint)
            }
            canvas.drawPath(path, linePaint)
        }

        drawDateLabels(canvas, padding, chartWidth)
    }

    private fun mapToY(value: Float, yMin: Float, yMax: Float, padding: Float, chartHeight: Float): Float {
        val range = yMax - yMin
        val normalized = if (range > 0f) (value - yMin) / range else 0.5f
        return padding + chartHeight - (normalized * chartHeight)
    }

    private fun drawDateLabels(canvas: Canvas, padding: Float, chartWidth: Float) {
        val labelCount = min(data.size, 5)
        val step = max(1, data.size / labelCount)
        for (i in 0 until data.size step step) {
            val x = padding + (chartWidth * i / max(1, data.size - 1))
            val dateStr = "${data[i].date.monthValue}/${data[i].date.dayOfMonth}"
            canvas.save()
            canvas.rotate(-45f, x, height - padding + 20f)
            canvas.drawText(dateStr, x, height - padding + 30f, textPaint.apply { textSize = 20f })
            canvas.restore()
        }
        textPaint.textSize = 24f
    }

    private fun drawEmptyState(canvas: Canvas) {
        val p = Paint(textPaint).apply { textSize = 32f; textAlign = Paint.Align.CENTER }
        canvas.drawText("No recordings to compare", width / 2f, height / 2f, p)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 800
        val desiredHeight = 400
        val w = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(widthMeasureSpec)
            MeasureSpec.AT_MOST -> minOf(desiredWidth, MeasureSpec.getSize(widthMeasureSpec))
            else -> desiredWidth
        }
        val h = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(heightMeasureSpec)
            MeasureSpec.AT_MOST -> minOf(desiredHeight, MeasureSpec.getSize(heightMeasureSpec))
            else -> desiredHeight
        }
        setMeasuredDimension(w, h)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun extractMetricValue(a: AudioAnalysisEntity, metric: VoiceMetric): Float = when (metric) {
        VoiceMetric.F0_MEAN               -> a.f0Mean
        VoiceMetric.PITCH_RANGE_HZ        -> a.pitchRangeHz
        VoiceMetric.PITCH_STABILITY_SCORE -> a.pitchStabilityScore
        VoiceMetric.VOICED_RATIO          -> a.voicedRatio
        VoiceMetric.INTONATION_MOVEMENT   -> a.intonationMovement
    }
}
