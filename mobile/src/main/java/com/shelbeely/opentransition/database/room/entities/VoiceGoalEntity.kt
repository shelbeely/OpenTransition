/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.database.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A user-defined voice goal: a named target range for one metric.
 *
 * Goals have no presets or gendered defaults.  The user creates them
 * themselves, picks the metric and the min/max values they care about, and
 * gives the goal a name that means something to them.
 *
 * @property id         Random UUID.
 * @property name       Free-text label chosen by the user.
 * @property metricKey  Which voice metric this goal tracks.
 * @property targetMin  Lower bound of the target range (same unit as the metric).
 * @property targetMax  Upper bound of the target range.
 * @property createdAt  Unix epoch ms when the goal was created.
 */
@Entity(tableName = "voice_goals")
data class VoiceGoalEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val metricKey: String,   // one of VoiceMetric.key values
    val targetMin: Float,
    val targetMax: Float,
    val createdAt: Long
) {
    companion object {
        // Possible metricKey values (mirrors VoiceMetric enum)
        const val METRIC_F0_MEAN              = "f0Mean"
        const val METRIC_PITCH_RANGE_HZ       = "pitchRangeHz"
        const val METRIC_VOICED_RATIO         = "voicedRatio"
        const val METRIC_PITCH_STABILITY_SCORE = "pitchStabilityScore"
        const val METRIC_INTONATION_MOVEMENT  = "intonationMovement"
    }
}
