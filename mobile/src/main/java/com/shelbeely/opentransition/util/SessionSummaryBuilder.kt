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

import java.util.Locale
import kotlin.math.roundToInt

/**
 * Builds a short, neutral, purely descriptive summary of one recording session.
 *
 * The language is intentionally metric-neutral: no "good", "bad", "high", "low",
 * no external reference points, no gendered comparisons.  Every sentence describes
 * *this* voice *on this day* using relative adverbs that reflect the actual values.
 *
 * The result is stored as [com.shelbeely.opentransition.database.room.entities.AudioAnalysisEntity.sessionSummaryText]
 * and displayed on [com.shelbeely.opentransition.ui.voicesession.VoiceSessionDetailScreen].
 */
object SessionSummaryBuilder {

    /**
     * Build a summary from the provided metrics.
     *
     * All parameters accept the same units as the corresponding
     * [com.shelbeely.opentransition.database.room.entities.AudioAnalysisEntity] columns.
     *
     * @param f0Mean              Mean pitch in Hz (voiced frames only).
     * @param f0Min               Minimum pitch in Hz.
     * @param f0Max               Maximum pitch in Hz.
     * @param pitchRangeHz        f0Max − f0Min.
     * @param voicedRatio         0–1 fraction of frames classified as voiced.
     * @param pitchStabilityScore 0–1; 1 = very steady.
     * @param intonationMovement  Mean |Δf0| between consecutive voiced frames, Hz/frame.
     * @param intensityMeanDb     Mean RMS level in dBFS.
     * @param durationSeconds     Recording duration.
     */
    fun build(
        f0Mean: Float,
        f0Min: Float,
        f0Max: Float,
        pitchRangeHz: Float,
        voicedRatio: Float,
        pitchStabilityScore: Float,
        intonationMovement: Float,
        intensityMeanDb: Float,
        durationSeconds: Float
    ): String {
        // No voiced speech at all
        if (voicedRatio < 0.05f || f0Mean <= 0f) {
            return "No voiced speech was detected in this recording " +
                   "(${durationSeconds.roundToInt()} s)."
        }

        val sb = StringBuilder()

        // ── Pitch range sentence ──────────────────────────────────────────
        val pct = (voicedRatio * 100f).roundToInt()
        sb.append(
            "Pitch ranged from ${f0Min.roundToInt()}–${f0Max.roundToInt()} Hz " +
            "(${pitchRangeHz.roundToInt()} Hz span) across the voiced sections " +
            "($pct% of the recording)."
        )

        // ── Stability sentence ────────────────────────────────────────────
        val stabilityAdverb = when {
            pitchStabilityScore >= 0.85f -> "very steady"
            pitchStabilityScore >= 0.70f -> "quite steady"
            pitchStabilityScore >= 0.50f -> "moderately varied"
            pitchStabilityScore >= 0.30f -> "quite variable"
            else                         -> "very variable"
        }
        sb.append(" The voice was $stabilityAdverb")

        // ── Intonation movement addendum ──────────────────────────────────
        val movementAdverb = when {
            intonationMovement < 5f  -> "moved very little"
            intonationMovement < 15f -> "moved gently"
            intonationMovement < 30f -> "moved quite a bit"
            else                     -> "moved a lot"
        }
        sb.append(" — intonation $movementAdverb")
        sb.append(
            " (${String.format(Locale.US, "%.1f", intonationMovement)} Hz/frame on average)."
        )

        // ── Intensity sentence ────────────────────────────────────────────
        val intensityAdverb = when {
            intensityMeanDb >= -20f -> "loud and clear"
            intensityMeanDb >= -35f -> "at a comfortable level"
            intensityMeanDb >= -50f -> "fairly quiet"
            else                    -> "very quiet"
        }
        sb.append(" Overall volume was $intensityAdverb.")

        return sb.toString()
    }
}
