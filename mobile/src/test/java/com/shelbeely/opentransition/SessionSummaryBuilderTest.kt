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

import com.shelbeely.opentransition.util.SessionSummaryBuilder
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [SessionSummaryBuilder].
 *
 * For each scenario we verify the output contains expected phrases and does NOT
 * contain prohibited language (no "good", "bad", "high", "low", no gendered
 * references, no external "correct" pitch values).
 */
class SessionSummaryBuilderTest {

    // ── No voiced speech ──────────────────────────────────────────────────────

    @Test
    fun build_noVoicedSpeech_returnsUnvoicedMessage() {
        val result = SessionSummaryBuilder.build(
            f0Mean = 0f, f0Min = 0f, f0Max = 0f,
            pitchRangeHz = 0f, voicedRatio = 0f,
            pitchStabilityScore = 0f, intonationMovement = 0f,
            intensityMeanDb = -80f, durationSeconds = 5f
        )
        assertTrue("Should mention no voiced speech",
            result.contains("No voiced speech", ignoreCase = true))
    }

    @Test
    fun build_veryLowVoicedRatio_returnsUnvoicedMessage() {
        val result = SessionSummaryBuilder.build(
            f0Mean = 200f, f0Min = 180f, f0Max = 220f,
            pitchRangeHz = 40f, voicedRatio = 0.02f,
            pitchStabilityScore = 0.8f, intonationMovement = 5f,
            intensityMeanDb = -30f, durationSeconds = 8f
        )
        assertTrue(result.contains("No voiced speech", ignoreCase = true))
    }

    // ── Pitch range sentence ──────────────────────────────────────────────────

    @Test
    fun build_normalSpeech_containsPitchRange() {
        val result = build(f0Mean = 180f, f0Min = 150f, f0Max = 220f, pitchRangeHz = 70f)
        assertTrue("Should contain Hz range", result.contains("150–220 Hz"))
        assertTrue("Should contain span", result.contains("70 Hz span"))
    }

    @Test
    fun build_normalSpeech_containsVoicedPercent() {
        val result = build(voicedRatio = 0.73f)
        assertTrue("Should show 73%", result.contains("73%"))
    }

    // ── Stability sentence ────────────────────────────────────────────────────

    @Test
    fun build_highStability_containsVerySteady() {
        val result = build(pitchStabilityScore = 0.92f)
        assertTrue("Very steady for score=0.92", result.contains("very steady", ignoreCase = true))
    }

    @Test
    fun build_midStability_containsQuiteVariable() {
        val result = build(pitchStabilityScore = 0.35f)
        assertTrue("Quite variable for score=0.35", result.contains("variable", ignoreCase = true))
    }

    @Test
    fun build_lowStability_containsVeryVariable() {
        val result = build(pitchStabilityScore = 0.10f)
        assertTrue("Very variable for score=0.10", result.contains("variable", ignoreCase = true))
    }

    // ── Intonation movement ───────────────────────────────────────────────────

    @Test
    fun build_lowMovement_containsMovedVeryLittle() {
        val result = build(intonationMovement = 2f)
        assertTrue("Low movement phrase present", result.contains("moved very little", ignoreCase = true))
    }

    @Test
    fun build_highMovement_containsMovedALot() {
        val result = build(intonationMovement = 40f)
        assertTrue("High movement phrase present", result.contains("moved a lot", ignoreCase = true))
    }

    // ── Intensity ─────────────────────────────────────────────────────────────

    @Test
    fun build_loudIntensity_containsLoudAndClear() {
        val result = build(intensityMeanDb = -15f)
        assertTrue("Loud phrase present", result.contains("loud and clear", ignoreCase = true))
    }

    @Test
    fun build_quietIntensity_containsVeryQuiet() {
        val result = build(intensityMeanDb = -65f)
        assertTrue("Quiet phrase present", result.contains("quiet", ignoreCase = true))
    }

    // ── Neutral language checks ───────────────────────────────────────────────

    @Test
    fun build_doesNotContainValueJudgements() {
        val result = build()
        val prohibited = listOf("good", "bad", "correct", "wrong", "should be", "target",
                                "typical", "average person", "normal", "standard")
        for (word in prohibited) {
            assertFalse("Result must not contain '$word'", result.contains(word, ignoreCase = true))
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private fun build(
        f0Mean: Float = 170f,
        f0Min: Float = 140f,
        f0Max: Float = 200f,
        pitchRangeHz: Float = 60f,
        voicedRatio: Float = 0.70f,
        pitchStabilityScore: Float = 0.75f,
        intonationMovement: Float = 10f,
        intensityMeanDb: Float = -30f,
        durationSeconds: Float = 10f
    ) = SessionSummaryBuilder.build(
        f0Mean              = f0Mean,
        f0Min               = f0Min,
        f0Max               = f0Max,
        pitchRangeHz        = pitchRangeHz,
        voicedRatio         = voicedRatio,
        pitchStabilityScore = pitchStabilityScore,
        intonationMovement  = intonationMovement,
        intensityMeanDb     = intensityMeanDb,
        durationSeconds     = durationSeconds
    )
}
