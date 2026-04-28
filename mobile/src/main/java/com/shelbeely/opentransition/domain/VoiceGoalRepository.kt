/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.domain

import android.content.Context
import com.shelbeely.opentransition.database.DatabaseManager
import com.shelbeely.opentransition.database.room.entities.VoiceGoalEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository for [VoiceGoalEntity] — user-defined target ranges for voice metrics.
 *
 * Wraps the DAO with a slightly higher-level API and handles ID generation.
 */
class VoiceGoalRepository(private val context: Context) {

    private val dao get() = DatabaseManager.getDatabase(context).voiceGoalDao()

    /** Observe all goals, newest first. */
    fun getAllGoals(): Flow<List<VoiceGoalEntity>> = dao.getAllGoals()

    /** Observe goals for a specific metric key (see [com.shelbeely.opentransition.util.VoiceMetric.key]). */
    suspend fun getGoalsForMetric(metricKey: String): List<VoiceGoalEntity> =
        dao.getGoalsForMetric(metricKey)

    /** Create a new goal and persist it. Returns the created entity. */
    suspend fun createGoal(
        name: String,
        metricKey: String,
        targetMin: Float,
        targetMax: Float
    ): VoiceGoalEntity {
        val entity = VoiceGoalEntity(
            id         = UUID.randomUUID().toString(),
            name       = name,
            metricKey  = metricKey,
            targetMin  = targetMin,
            targetMax  = targetMax,
            createdAt  = System.currentTimeMillis()
        )
        dao.insertGoal(entity)
        return entity
    }

    /** Update an existing goal. */
    suspend fun updateGoal(goal: VoiceGoalEntity) = dao.updateGoal(goal)

    /** Delete a goal by ID. */
    suspend fun deleteGoalById(id: String) = dao.deleteGoalById(id)

    /** Delete all goals. */
    suspend fun deleteAllGoals() = dao.deleteAllGoals()
}
