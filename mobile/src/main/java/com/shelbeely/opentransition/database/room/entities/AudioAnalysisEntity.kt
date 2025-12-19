/*
 * Copyright © 2023 TransTracks. All rights reserved.
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

@Entity(tableName = "audio_analysis")
data class AudioAnalysisEntity(
    @PrimaryKey
    val id: String,
    val photoId: String,
    val f0Mean: Float,
    val f0Min: Float,
    val f0Max: Float,
    val f1Mean: Float,
    val f2Mean: Float,
    val f3Mean: Float,
    val f4Mean: Float,
    val f0StdDev: Float,
    val durationSeconds: Float,
    val analysisTimestamp: Long
)
