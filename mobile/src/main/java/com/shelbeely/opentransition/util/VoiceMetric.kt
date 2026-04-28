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

/**
 * The set of voice metrics that can be tracked over time in
 * [com.shelbeely.opentransition.ui.widget.PitchProgressionView] and targeted
 * by [com.shelbeely.opentransition.database.room.entities.VoiceGoalEntity].
 *
 * Labels are intentionally plain and descriptive — no implied direction.
 */
enum class VoiceMetric(
    /** Short label shown in toggle chips. */
    val label: String,
    /** Stable key stored in the database (matches AudioAnalysisEntity column names). */
    val key: String,
    /** Unit label shown on the Y axis, e.g. "Hz". Empty string for dimensionless metrics. */
    val unit: String
) {
    F0_MEAN("Pitch average", "f0Mean", "Hz"),
    PITCH_RANGE_HZ("Pitch range", "pitchRangeHz", "Hz"),
    PITCH_STABILITY_SCORE("Stability", "pitchStabilityScore", ""),
    VOICED_RATIO("Voiced %", "voicedRatio", "%"),
    INTONATION_MOVEMENT("Intonation", "intonationMovement", "Hz/frame");
}
