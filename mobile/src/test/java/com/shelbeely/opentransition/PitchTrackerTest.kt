/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition

import com.shelbeely.opentransition.util.PitchTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Unit tests for [PitchTracker] using synthetic sine-wave PCM.
 *
 * Sine waves provide a ground-truth F0 that the autocorrelation algorithm
 * should recover to within a few Hz.
 */
class PitchTrackerTest {

    private val sampleRate = 44100

    // ── Helper: generate a mono sine-wave at a given frequency ───────────────

    private fun sineWave(
        frequencyHz: Float,
        durationSeconds: Float = 1f,
        amplitude: Float = 0.5f
    ): FloatArray {
        val n = (sampleRate * durationSeconds).toInt()
        return FloatArray(n) { i ->
            (amplitude * sin(2.0 * PI * frequencyHz * i / sampleRate)).toFloat()
        }
    }

    /** Mix two sine waves (ensures we're testing robustness on non-pure tones). */
    private fun twoSines(f1: Float, a1: Float, f2: Float, a2: Float, durationSeconds: Float = 1f): FloatArray {
        val n = (sampleRate * durationSeconds).toInt()
        return FloatArray(n) { i ->
            (a1 * sin(2.0 * PI * f1 * i / sampleRate) +
             a2 * sin(2.0 * PI * f2 * i / sampleRate)).toFloat()
        }
    }

    // ── Single-frame API ──────────────────────────────────────────────────────

    @Test
    fun singleFrame_pureHarmonic_detectsCorrectF0() {
        val targetHz = 200f
        val samples = sineWave(targetHz)
        val frame = samples.copyOfRange(0, PitchTracker.DEFAULT_FRAME_SIZE)
        val minLag = (sampleRate / PitchTracker.F0_MAX_HZ).toInt()
        val maxLag = (sampleRate / PitchTracker.F0_MIN_HZ).toInt()

        val result = PitchTracker.analyzeFrame(
            frame, sampleRate, minLag, maxLag,
            PitchTracker.DEFAULT_VOICED_THRESHOLD
        )

        assertTrue("Frame should be voiced", result.isVoiced)
        assertEquals(targetHz, result.f0Hz, 10f)  // within 10 Hz is good
    }

    @Test
    fun singleFrame_silence_isUnvoiced() {
        val frame = FloatArray(PitchTracker.DEFAULT_FRAME_SIZE) { 0f }
        val minLag = (sampleRate / PitchTracker.F0_MAX_HZ).toInt()
        val maxLag = (sampleRate / PitchTracker.F0_MIN_HZ).toInt()

        val result = PitchTracker.analyzeFrame(
            frame, sampleRate, minLag, maxLag,
            PitchTracker.DEFAULT_VOICED_THRESHOLD
        )

        assertFalse("Silent frame should be unvoiced", result.isVoiced)
        assertEquals(0f, result.f0Hz, 0.001f)
    }

    @Test
    fun singleFrame_rmsDb_silenceIsLow() {
        val frame = FloatArray(PitchTracker.DEFAULT_FRAME_SIZE) { 0f }
        val minLag = (sampleRate / PitchTracker.F0_MAX_HZ).toInt()
        val maxLag = (sampleRate / PitchTracker.F0_MIN_HZ).toInt()

        val result = PitchTracker.analyzeFrame(
            frame, sampleRate, minLag, maxLag,
            PitchTracker.DEFAULT_VOICED_THRESHOLD
        )

        assertTrue("Silence dBFS should be ≤ −60 dB", result.rmsDb <= -60f)
    }

    @Test
    fun singleFrame_rmsDb_fullScaleIsNearZero() {
        // Full-scale sine wave: RMS ≈ 0.707 → dBFS ≈ −3 dB
        val amplitude = 1f / sqrt(2f)
        val samples = sineWave(200f, amplitude = amplitude)
        val frame = samples.copyOfRange(0, PitchTracker.DEFAULT_FRAME_SIZE)
        val minLag = (sampleRate / PitchTracker.F0_MAX_HZ).toInt()
        val maxLag = (sampleRate / PitchTracker.F0_MIN_HZ).toInt()

        val result = PitchTracker.analyzeFrame(
            frame, sampleRate, minLag, maxLag,
            PitchTracker.DEFAULT_VOICED_THRESHOLD
        )

        assertTrue("Full-scale RMS dBFS should be > −10 dB, got ${result.rmsDb}", result.rmsDb > -10f)
    }

    // ── Multi-frame API ───────────────────────────────────────────────────────

    @Test
    fun analyzeFrames_pureHarmonic_majorityOfFramesVoiced() {
        val samples = sineWave(150f, durationSeconds = 2f)
        val frames = PitchTracker.analyzeFrames(samples, sampleRate)

        assertTrue("Expected at least 1 frame", frames.isNotEmpty())
        val voicedCount = frames.count { it.isVoiced }
        val voicedRatio = voicedCount.toFloat() / frames.size
        assertTrue(
            "Voiced ratio should be > 0.7, got $voicedRatio",
            voicedRatio > 0.70f
        )
    }

    @Test
    fun analyzeFrames_pureHarmonic_meanF0IsAccurate() {
        val targetHz = 170f
        val samples = sineWave(targetHz, durationSeconds = 2f)
        val frames = PitchTracker.analyzeFrames(samples, sampleRate)

        val voicedF0s = frames.filter { it.isVoiced }.map { it.f0Hz }
        assertTrue("Should have some voiced frames", voicedF0s.isNotEmpty())
        val meanF0 = voicedF0s.average().toFloat()

        assertEquals(targetHz, meanF0, 15f)  // within 15 Hz
    }

    @Test
    fun analyzeFrames_silence_noVoicedFrames() {
        val samples = FloatArray(sampleRate * 2) { 0f }
        val frames = PitchTracker.analyzeFrames(samples, sampleRate)

        assertTrue("No voiced frames in silence", frames.none { it.isVoiced })
    }

    @Test
    fun analyzeFrames_emptyInput_returnsEmpty() {
        val result = PitchTracker.analyzeFrames(FloatArray(0), sampleRate)
        assertTrue(result.isEmpty())
    }

    @Test
    fun analyzeFrames_twoOctaves_separateDetections() {
        // Two recordings at different pitches — means should differ
        val low  = sineWave(100f, durationSeconds = 1f)
        val high = sineWave(200f, durationSeconds = 1f)

        val framesLow  = PitchTracker.analyzeFrames(low,  sampleRate)
        val framesHigh = PitchTracker.analyzeFrames(high, sampleRate)

        val meanLow  = framesLow .filter { it.isVoiced }.map { it.f0Hz }.average()
        val meanHigh = framesHigh.filter { it.isVoiced }.map { it.f0Hz }.average()

        assertTrue(
            "High mean ($meanHigh) should be > low mean ($meanLow) + 50 Hz",
            meanHigh > meanLow + 50
        )
    }

    @Test
    fun analyzeFrames_highConfidenceForPureTone() {
        val samples = sineWave(220f, durationSeconds = 1f, amplitude = 0.9f)
        val frames = PitchTracker.analyzeFrames(samples, sampleRate)

        val voicedFrames = frames.filter { it.isVoiced }
        assertTrue("Should have voiced frames", voicedFrames.isNotEmpty())
        val meanConf = voicedFrames.map { it.confidence }.average()
        assertTrue("Mean confidence should be > 0.5, got $meanConf", meanConf > 0.5)
    }
}
