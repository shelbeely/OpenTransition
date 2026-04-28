/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.database.room.dao

import androidx.room.*
import com.shelbeely.opentransition.database.room.entities.VoiceGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceGoalDao {

    @Query("SELECT * FROM voice_goals ORDER BY createdAt DESC")
    fun getAllGoals(): Flow<List<VoiceGoalEntity>>

    @Query("SELECT * FROM voice_goals ORDER BY createdAt DESC")
    suspend fun getAllGoalsList(): List<VoiceGoalEntity>

    @Query("SELECT * FROM voice_goals WHERE id = :id")
    suspend fun getGoalById(id: String): VoiceGoalEntity?

    @Query("SELECT * FROM voice_goals WHERE metricKey = :metricKey")
    suspend fun getGoalsForMetric(metricKey: String): List<VoiceGoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: VoiceGoalEntity)

    @Update
    suspend fun updateGoal(goal: VoiceGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: VoiceGoalEntity)

    @Query("DELETE FROM voice_goals WHERE id = :id")
    suspend fun deleteGoalById(id: String)

    @Query("DELETE FROM voice_goals")
    suspend fun deleteAllGoals()
}
