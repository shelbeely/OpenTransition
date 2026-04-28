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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.shelbeely.opentransition.util.AudioDecoder
import com.shelbeely.opentransition.util.PitchFrame
import com.shelbeely.opentransition.util.PitchTracker
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Displays a real-time frequency spectrogram of an audio file.
 *
 * A spectrogram plots time (X) against frequency (Y) with intensity encoded
 * as colour.  Unlike a single pitch number or a formant scatter chart, a
 * spectrogram shows the full character of the voice without implying any
 * external "target": each recording looks like its own fingerprint.  This
 * makes it a useful ear-training companion — users can listen to a recording
 * and simultaneously see the frequency content that creates the tone they
 * are hearing.
 *
 * **How it works**
 * 1. [setAudioFile] decodes the audio to raw PCM using [MediaCodec] on a
 *    background thread.
 * 2. The PCM is processed with a Short-Time Fourier Transform (STFT) using a
 *    Cooley-Tukey FFT.  Window size is 1024 samples (≈23 ms at 44 100 Hz);
 *    hop size is 256 samples (75 % overlap).
 * 3. Magnitudes are mapped to dB scale and rendered into a [Bitmap] using a
 *    warm "inferno"-style colour palette (black → purple → orange → yellow).
 * 4. The Y axis is logarithmically scaled from 80 Hz (bottom) to 8 000 Hz
 *    (top), covering the full voice range without wasting space on
 *    inaudible frequencies.
 * 5. An optional playback-progress cursor is drawn as a vertical line.
 *
 * All heavy work runs on a dedicated thread; [renderGeneration] acts as a
 * cancellation token so stale renders are discarded when a new file is set.
 */
class SpectrogramView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    @Volatile private var renderGeneration = 0
    private var spectrogramBitmap: Bitmap? = null

    /** Voiced pitch frames stored parallel to the spectrogram columns. */
    @Volatile private var pitchFrames: List<PitchFrame> = emptyList()
    /** Number of STFT frames that map to the bitmap width. */
    @Volatile private var numSpectrogramFrames: Int = 0
    /** Sample rate of the last decoded audio, needed for axis mapping. */
    @Volatile private var lastSampleRate: Int = 44100

    /** Playback progress 0.0–1.0; draws a vertical cursor when > 0. */
    private var playbackProgress = 0f

    private val backgroundPaint = Paint().apply {
        color = Color.parseColor("#0D0D1A")
        style = Paint.Style.FILL
    }

    private val cursorPaint = Paint().apply {
        color = Color.WHITE
        alpha = 180
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = false
    }

    private val emptyTextPaint = Paint().apply {
        color = Color.WHITE
        alpha = 80
        textSize = 28f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val pitchCurvePaint = Paint().apply {
        color = Color.parseColor("#00E5FF")   // cyan — visible on inferno palette
        alpha = 220
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val unvoicedOverlayPaint = Paint().apply {
        color = Color.BLACK
        alpha = 80
        style = Paint.Style.FILL
    }

    // Reusable path to avoid per-draw allocations
    private val pitchPath = Path()

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Decode [audioFile] and render the spectrogram asynchronously.
     * Any in-progress render for a previous file is abandoned.
     */
    fun setAudioFile(audioFile: File) {
        val gen = ++renderGeneration
        Thread {
            try {
                val (samples, sampleRate) = AudioDecoder.decodeAudioToPcm(audioFile) ?: return@Thread
                if (gen != renderGeneration) return@Thread

                // Run pitch tracker on the same PCM so the overlay is in sync
                val frames = PitchTracker.analyzeFrames(samples, sampleRate,
                    PitchTracker.DEFAULT_FRAME_SIZE, PitchTracker.DEFAULT_HOP_SIZE)
                if (gen != renderGeneration) return@Thread

                val numFrames = ((samples.size - fftSize) / hopSize).coerceAtLeast(1)
                val bitmap = buildSpectrogramBitmap(samples, sampleRate, frames, gen) ?: return@Thread
                if (gen != renderGeneration) { bitmap.recycle(); return@Thread }
                post {
                    if (gen == renderGeneration) {
                        spectrogramBitmap?.recycle()
                        spectrogramBitmap = bitmap
                        pitchFrames = frames
                        numSpectrogramFrames = numFrames
                        lastSampleRate = sampleRate
                        invalidate()
                    } else {
                        bitmap.recycle()
                    }
                }
            } catch (_: Exception) { /* no-op — view will stay blank */ }
        }.start()
    }

    /** Update the playback cursor position (0.0–1.0). */
    fun setPlaybackProgress(progress: Float) {
        playbackProgress = progress.coerceIn(0f, 1f)
        invalidate()
    }

    /** Clear the displayed spectrogram (e.g. when the view is recycled). */
    fun clear() {
        ++renderGeneration
        spectrogramBitmap?.recycle()
        spectrogramBitmap = null
        pitchFrames = emptyList()
        numSpectrogramFrames = 0
        invalidate()
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        val bmp = spectrogramBitmap
        if (bmp != null && !bmp.isRecycled) {
            canvas.drawBitmap(bmp, null, RectF(0f, 0f, width.toFloat(), height.toFloat()), null)

            val frames = pitchFrames
            val nFrames = numSpectrogramFrames.takeIf { it > 0 } ?: frames.size
            if (frames.isNotEmpty() && nFrames > 0) {
                drawVoicedUnvoicedOverlay(canvas, frames, nFrames)
                drawPitchCurve(canvas, frames, nFrames)
            }

            if (playbackProgress > 0f) {
                val cx = playbackProgress * width
                canvas.drawLine(cx, 0f, cx, height.toFloat(), cursorPaint)
            }
        } else {
            canvas.drawText(
                "…",
                width / 2f,
                height / 2f + emptyTextPaint.textSize / 3,
                emptyTextPaint
            )
        }
    }

    /**
     * Draw a translucent black overlay on columns corresponding to unvoiced frames
     * so users can visually identify when phonation stops.
     */
    private fun drawVoicedUnvoicedOverlay(canvas: Canvas, frames: List<PitchFrame>, nFrames: Int) {
        val w = width.toFloat()
        val h = height.toFloat()
        for (i in frames.indices) {
            if (!frames[i].isVoiced) {
                val x0 = i.toLong() * w / nFrames
                val x1 = (i + 1).toLong() * w / nFrames
                canvas.drawRect(x0, 0f, x1, h, unvoicedOverlayPaint)
            }
        }
    }

    /**
     * Draw a pitch curve on top of the spectrogram connecting consecutive voiced frames.
     * The Y position maps the frequency logarithmically to match the spectrogram axes.
     */
    private fun drawPitchCurve(canvas: Canvas, frames: List<PitchFrame>, nFrames: Int) {
        val w = width.toFloat()
        val h = height.toFloat()
        val logMin = ln(freqMin.toDouble())
        val logMax = ln(freqMax.toDouble())

        pitchPath.reset()
        var pathStarted = false

        for (i in frames.indices) {
            val frame = frames[i]
            if (!frame.isVoiced || frame.f0Hz <= 0f) {
                pathStarted = false
                continue
            }
            val f0 = frame.f0Hz.coerceIn(freqMin, freqMax)
            val logF0 = ln(f0.toDouble())
            val t = ((logF0 - logMin) / (logMax - logMin)).coerceIn(0.0, 1.0)
            // Row 0 = top = high freq; so y = (1 − t) * height
            val y = ((1.0 - t) * h).toFloat()
            val x = (i.toLong() * w / nFrames + w / nFrames / 2f).toFloat()

            if (!pathStarted) {
                pitchPath.moveTo(x, y)
                pathStarted = true
            } else {
                pitchPath.lineTo(x, y)
            }
        }
        canvas.drawPath(pitchPath, pitchCurvePaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 800
        val desiredHeight = 160

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

    // ── Audio decoding ────────────────────────────────────────────────────────

    // Delegated to the shared AudioDecoder utility — no duplication here.

    // ── STFT + colour mapping ─────────────────────────────────────────────────

    private val fftSize = 1024
    private val hopSize = 256
    private val freqMin = 80f
    private val freqMax = 8000f

    /**
     * Compute an STFT over [samples] and render it into a [Bitmap].
     * [gen] is checked between frames so the render can be cancelled early.
     * Column brightness is additionally scaled by the frame RMS so louder
     * sections appear brighter — consistent with professional spectrogram tools.
     */
    private fun buildSpectrogramBitmap(
        samples: FloatArray,
        sampleRate: Int,
        pitchFrameList: List<PitchFrame>,
        gen: Int
    ): Bitmap? {
        val numFrames = ((samples.size - fftSize) / hopSize).coerceAtLeast(1)
        val bitmapW = numFrames.coerceAtMost(1024)
        val bitmapH = 160

        val bitmap = Bitmap.createBitmap(bitmapW, bitmapH, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(bitmapW * bitmapH)

        // Hann window coefficients
        val window = FloatArray(fftSize) { i ->
            (0.5 * (1.0 - cos(2.0 * PI * i / (fftSize - 1)))).toFloat()
        }

        val real = FloatArray(fftSize)
        val imag = FloatArray(fftSize)

        // Pre-compute log-frequency mapping for each output row
        val nyquist = sampleRate / 2f
        val fMaxClamped = freqMax.coerceAtMost(nyquist)
        val logMin = ln(freqMin.toDouble())
        val logMax = ln(fMaxClamped.toDouble())
        val rowBins = IntArray(bitmapH) { row ->
            // row 0 = top = high frequency, row bitmapH-1 = bottom = low frequency
            val t = 1.0 - row.toDouble() / (bitmapH - 1)
            val freq = exp(logMin + t * (logMax - logMin)).toFloat()
            (freq * fftSize / sampleRate).toInt().coerceIn(0, fftSize / 2 - 1)
        }

        // First pass: collect all frame magnitudes to find global max for normalisation
        val allMags = Array(numFrames) { FloatArray(fftSize / 2) }
        var globalMax = 1e-10f

        for (frame in 0 until numFrames) {
            if (gen != renderGeneration) { bitmap.recycle(); return null }
            val offset = frame * hopSize
            for (i in 0 until fftSize) {
                real[i] = if (offset + i < samples.size) samples[offset + i] * window[i] else 0f
                imag[i] = 0f
            }
            fft(real, imag)
            for (k in 0 until fftSize / 2) {
                val mag = sqrt((real[k] * real[k] + imag[k] * imag[k]).toDouble()).toFloat()
                allMags[frame][k] = mag
                if (mag > globalMax) globalMax = mag
            }
        }

        // Second pass: render pixels
        for (frame in 0 until numFrames) {
            if (gen != renderGeneration) { bitmap.recycle(); return null }
            val x = (frame.toLong() * bitmapW / numFrames).toInt().coerceIn(0, bitmapW - 1)
            val nextX = ((frame.toLong() + 1) * bitmapW / numFrames).toInt()
                .coerceIn(x + 1, bitmapW)

            // Optional RMS brightness multiplier: louder frames are brighter
            val rmsDb = pitchFrameList.getOrNull(frame)?.rmsDb ?: -40f
            val rmsBrightness = ((rmsDb + 80f) / 80f).coerceIn(0.25f, 1f)

            for (row in 0 until bitmapH) {
                val bin = rowBins[row]
                val mag = allMags[frame][bin] / globalMax
                // Convert to dB, normalise to [0, 1] over 80 dB dynamic range
                val db = if (mag > 0f) (20.0 * log10(mag.toDouble())).toFloat() else -80f
                val intensity = ((db + 80f) / 80f * rmsBrightness).coerceIn(0f, 1f)
                val colour = infernoColour(intensity)
                for (px in x until nextX) {
                    pixels[row * bitmapW + px] = colour
                }
            }
        }

        bitmap.setPixels(pixels, 0, bitmapW, 0, 0, bitmapW, bitmapH)
        return bitmap
    }

    /**
     * In-place Cooley-Tukey radix-2 DIT FFT.
     * Both arrays must have a power-of-two length (we always use [fftSize] = 1024).
     */
    private fun fft(real: FloatArray, imag: FloatArray) {
        val n = real.size
        // Bit-reversal permutation
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) { j = j xor bit; bit = bit shr 1 }
            j = j xor bit
            if (i < j) {
                var tmp = real[i]; real[i] = real[j]; real[j] = tmp
                tmp = imag[i]; imag[i] = imag[j]; imag[j] = tmp
            }
        }
        // Butterfly passes
        var len = 2
        while (len <= n) {
            val half = len ushr 1
            val ang = -PI / half
            val wBaseRe = cos(ang).toFloat()
            val wBaseIm = sin(ang).toFloat()
            var k = 0
            while (k < n) {
                var wRe = 1f; var wIm = 0f
                for (i in 0 until half) {
                    val uRe = real[k + i];          val uIm = imag[k + i]
                    val vRe = real[k + i + half] * wRe - imag[k + i + half] * wIm
                    val vIm = real[k + i + half] * wIm + imag[k + i + half] * wRe
                    real[k + i] = uRe + vRe;        imag[k + i] = uIm + vIm
                    real[k + i + half] = uRe - vRe; imag[k + i + half] = uIm - vIm
                    val newWRe = wRe * wBaseRe - wIm * wBaseIm
                    wIm = wRe * wBaseIm + wIm * wBaseRe
                    wRe = newWRe
                }
                k += len
            }
            len = len shl 1
        }
    }

    /**
     * Maps an intensity value [0, 1] to an ARGB colour using an inferno-style
     * palette: black → dark-purple → deep-red → orange → bright-yellow.
     * This palette is perceptually uniform and carries no gender connotations.
     */
    private fun infernoColour(t: Float): Int {
        return when {
            t < 0.25f -> {
                val p = t / 0.25f
                Color.rgb((p * 52).toInt(), 0, (20 + p * 105).toInt())
            }
            t < 0.50f -> {
                val p = (t - 0.25f) / 0.25f
                Color.rgb((52 + p * 138).toInt(), (p * 25).toInt(), (125 - p * 88).toInt())
            }
            t < 0.75f -> {
                val p = (t - 0.50f) / 0.25f
                Color.rgb((190 + p * 65).toInt(), (25 + p * 135).toInt(), (37 - p * 17).toInt())
            }
            else -> {
                val p = (t - 0.75f) / 0.25f
                Color.rgb(255, (160 + p * 95).toInt(), (20 + p * 205).toInt())
            }
        }
    }
}
