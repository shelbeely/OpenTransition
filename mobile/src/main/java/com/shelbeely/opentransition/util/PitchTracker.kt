/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.util

import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * On-device fundamental-frequency (F0 / pitch) estimator.
 *
 * Uses normalised Autocorrelation Function (nACF) pitch detection.  This
 * approach is robust on real speech, requires no external library, and is
 * straightforward to unit-test with synthetic sine-wave signals.
 *
 * **Algorithm**
 * 1. Divide the PCM stream into overlapping frames (default 1024 samples,
 *    256-sample hop).
 * 2. For each frame compute the frame RMS (= intensity / loudness proxy).
 * 3. Compute the normalised autocorrelation for every lag in the range that
 *    corresponds to 50–600 Hz (covers the full human voice range plus some
 *    margin for checking).
 * 4. Find the peak correlation value and its lag.  Use parabolic interpolation
 *    to get sub-sample accuracy.
 * 5. If the peak exceeds [voicedThreshold] the frame is declared voiced;
 *    otherwise unvoiced (f0Hz = 0).
 *
 * @see PitchFrame — the per-frame result data class.
 */
object PitchTracker {

    /** Default analysis window in samples. */
    const val DEFAULT_FRAME_SIZE = 1024

    /** Default hop between consecutive frames in samples. */
    const val DEFAULT_HOP_SIZE = 256

    /**
     * Normalised autocorrelation peak above which a frame is considered voiced.
     * Range 0–1; 0.45 is a reasonable default for clean recordings.
     */
    const val DEFAULT_VOICED_THRESHOLD = 0.45f

    /**
     * Minimum F0 considered (Hz).  Below this lag we ignore correlation peaks
     * to avoid detecting room rumble or DC as pitch.
     */
    const val F0_MIN_HZ = 50f

    /**
     * Maximum F0 considered (Hz).  Above this only noise or artefacts live.
     */
    const val F0_MAX_HZ = 600f

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Analyse [samples] at [sampleRate] Hz and return one [PitchFrame] per
     * analysis window.
     *
     * @param samples        Mono PCM, range −1…+1.
     * @param sampleRate     Sample rate in Hz (e.g. 44100).
     * @param frameSize      Analysis window length in samples (power of 2 preferred).
     * @param hopSize        Step between consecutive frames.
     * @param voicedThreshold Minimum normalised correlation to declare voiced.
     */
    fun analyzeFrames(
        samples: FloatArray,
        sampleRate: Int,
        frameSize: Int = DEFAULT_FRAME_SIZE,
        hopSize: Int = DEFAULT_HOP_SIZE,
        voicedThreshold: Float = DEFAULT_VOICED_THRESHOLD
    ): List<PitchFrame> {
        if (samples.isEmpty() || sampleRate <= 0) return emptyList()

        val minLag = (sampleRate / F0_MAX_HZ).toInt().coerceAtLeast(1)
        val maxLag = (sampleRate / F0_MIN_HZ).toInt().coerceAtMost(frameSize - 1)

        val numFrames = ((samples.size - frameSize) / hopSize).coerceAtLeast(0)
        val frames = ArrayList<PitchFrame>(numFrames + 1)

        var offset = 0
        while (offset + frameSize <= samples.size) {
            val frame = FloatArray(frameSize) { i -> samples[offset + i] }
            frames.add(analyzeFrame(frame, sampleRate, minLag, maxLag, voicedThreshold))
            offset += hopSize
        }
        return frames
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    internal fun analyzeFrame(
        frame: FloatArray,
        sampleRate: Int,
        minLag: Int,
        maxLag: Int,
        voicedThreshold: Float
    ): PitchFrame {
        val rms = computeRms(frame)
        val rmsDb = if (rms > 0f) 20f * log10(rms).coerceAtLeast(-80f) else -80f

        // Normalised autocorrelation: r(lag) / r(0)
        val r0 = autocorrelationAt(frame, 0)
        if (r0 < 1e-10f) {
            return PitchFrame(f0Hz = 0f, confidence = 0f, isVoiced = false, rmsDb = rmsDb)
        }

        // Find the lag with the highest normalised autocorrelation
        var bestLag = minLag
        var bestCorr = -1f
        for (lag in minLag..maxLag) {
            val corr = autocorrelationAt(frame, lag) / r0
            if (corr > bestCorr) {
                bestCorr = corr
                bestLag = lag
            }
        }

        // Parabolic interpolation for sub-sample pitch accuracy
        val refinedLag = if (bestLag in (minLag + 1)..maxLag) {
            val corrPrev = autocorrelationAt(frame, bestLag - 1) / r0
            val corrNext = if (bestLag + 1 <= maxLag)
                autocorrelationAt(frame, bestLag + 1) / r0
            else bestCorr
            parabolicPeak(bestLag.toFloat(), corrPrev, bestCorr, corrNext)
        } else {
            bestLag.toFloat()
        }

        val confidence = bestCorr.coerceIn(0f, 1f)
        val isVoiced = confidence >= voicedThreshold
        val f0 = if (isVoiced && refinedLag > 0f) sampleRate / refinedLag else 0f

        return PitchFrame(
            f0Hz = f0,
            confidence = confidence,
            isVoiced = isVoiced,
            rmsDb = rmsDb
        )
    }

    /** Compute un-normalised autocorrelation at a single [lag]. */
    private fun autocorrelationAt(frame: FloatArray, lag: Int): Float {
        val n = frame.size
        var sum = 0.0
        for (i in 0 until n - lag) {
            sum += frame[i] * frame[i + lag]
        }
        return sum.toFloat()
    }

    /** Root-mean-square of [frame]. */
    private fun computeRms(frame: FloatArray): Float {
        var sumSq = 0.0
        for (s in frame) sumSq += s * s
        return sqrt(sumSq / frame.size).toFloat()
    }

    /**
     * Fit a parabola through three points (x−1, x, x+1) and return the x
     * coordinate of the peak.  Stays at [x] if the denominator is zero.
     */
    private fun parabolicPeak(x: Float, ym1: Float, y0: Float, yp1: Float): Float {
        val denom = 2f * (2f * y0 - ym1 - yp1)
        return if (abs(denom) < 1e-10f) x else x + (ym1 - yp1) / denom
    }
}

/**
 * Result of analysing one analysis window (frame) of PCM audio.
 *
 * @property f0Hz       Estimated fundamental frequency in Hz; 0 if [isVoiced] is false.
 * @property confidence Normalised autocorrelation peak (0–1).  Values ≥ 0.45 are
 *                      typically voiced.
 * @property isVoiced   True when [confidence] ≥ the caller-supplied threshold.
 * @property rmsDb      Frame RMS level in dBFS.  −80 dB is effectively silence.
 */
data class PitchFrame(
    val f0Hz: Float,
    val confidence: Float,
    val isVoiced: Boolean,
    val rmsDb: Float
)
