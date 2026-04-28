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
import com.shelbeely.opentransition.database.room.entities.AudioAnalysisEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioAnalysisDao {
    @Query("SELECT * FROM audio_analysis ORDER BY analysisTimestamp DESC")
    fun getAllAudioAnalyses(): Flow<List<AudioAnalysisEntity>>

    @Query("SELECT * FROM audio_analysis ORDER BY analysisTimestamp DESC")
    suspend fun getAllAudioAnalysesList(): List<AudioAnalysisEntity>

    @Query("SELECT * FROM audio_analysis WHERE id = :id")
    suspend fun getAudioAnalysisById(id: String): AudioAnalysisEntity?

    @Query("SELECT * FROM audio_analysis WHERE photoId = :photoId")
    suspend fun getAudioAnalysisByPhotoId(photoId: String): AudioAnalysisEntity?

    @Query("SELECT * FROM audio_analysis WHERE photoId = :photoId LIMIT 1")
    fun observeByPhotoId(photoId: String): Flow<AudioAnalysisEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioAnalysis(audioAnalysis: AudioAnalysisEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioAnalyses(audioAnalyses: List<AudioAnalysisEntity>)
    
    @Update
    suspend fun updateAudioAnalysis(audioAnalysis: AudioAnalysisEntity)
    
    @Delete
    suspend fun deleteAudioAnalysis(audioAnalysis: AudioAnalysisEntity)
    
    @Query("DELETE FROM audio_analysis WHERE id = :id")
    suspend fun deleteAudioAnalysisById(id: String)
    
    @Query("DELETE FROM audio_analysis")
    suspend fun deleteAllAudioAnalyses()
}
