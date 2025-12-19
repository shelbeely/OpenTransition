/*
 * Copyright © 2023-2025 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.database.room.dao

import androidx.room.*
import com.shelbeely.opentransition.database.room.entities.MilestoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestones ORDER BY epochDay DESC, timestamp DESC")
    fun getAllMilestones(): Flow<List<MilestoneEntity>>
    
    @Query("SELECT * FROM milestones WHERE id = :id")
    suspend fun getMilestoneById(id: String): MilestoneEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: MilestoneEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<MilestoneEntity>)
    
    @Update
    suspend fun updateMilestone(milestone: MilestoneEntity)
    
    @Delete
    suspend fun deleteMilestone(milestone: MilestoneEntity)
    
    @Query("DELETE FROM milestones WHERE id = :id")
    suspend fun deleteMilestoneById(id: String)
    
    @Query("DELETE FROM milestones")
    suspend fun deleteAllMilestones()
}
