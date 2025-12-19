/*
 * Copyright © 2023-2025 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.database.migration

import android.content.Context
import android.util.Log
import com.shelbeely.opentransition.data.AudioAnalysis
import com.shelbeely.opentransition.data.Milestone
import com.shelbeely.opentransition.data.Photo
import com.shelbeely.opentransition.database.DatabaseManager
import com.shelbeely.opentransition.database.room.entities.AudioAnalysisEntity
import com.shelbeely.opentransition.database.room.entities.MilestoneEntity
import com.shelbeely.opentransition.database.room.entities.PhotoEntity
import com.shelbeely.opentransition.util.openDefault
import io.realm.kotlin.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Handles migration from Realm database to Room encrypted database
 */
object RealmToRoomMigration {
    private const val TAG = "RealmToRoomMigration"
    private const val MIGRATION_PREFS_KEY = "realm_to_room_migration_completed"
    
    /**
     * Check if migration has been completed
     */
    fun isMigrationComplete(context: Context): Boolean {
        val prefs = context.getSharedPreferences("migration_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean(MIGRATION_PREFS_KEY, false)
    }
    
    /**
     * Mark migration as complete
     */
    private fun markMigrationComplete(context: Context) {
        val prefs = context.getSharedPreferences("migration_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean(MIGRATION_PREFS_KEY, true).apply()
    }
    
    /**
     * Perform migration from Realm to Room
     * @return Migration result with statistics
     */
    suspend fun migrate(context: Context): MigrationResult = withContext(Dispatchers.IO) {
        val result = MigrationResult()
        
        try {
            Log.d(TAG, "Starting Realm to Room migration")
            
            // Open Realm database
            val realm = Realm.openDefault()
            
            // Get Room database
            val roomDb = DatabaseManager.getDatabase(context)
            
            // Migrate Milestones
            val realmMilestones = realm.query(Milestone::class).find()
            Log.d(TAG, "Migrating ${realmMilestones.size} milestones")
            
            realmMilestones.forEach { realmMilestone ->
                try {
                    val roomMilestone = MilestoneEntity(
                        id = realmMilestone.id,
                        epochDay = realmMilestone.epochDay,
                        timestamp = realmMilestone.timestamp,
                        title = realmMilestone.title,
                        description = realmMilestone.description
                    )
                    roomDb.milestoneDao().insertMilestone(roomMilestone)
                    result.milestonesSuccess++
                } catch (e: Exception) {
                    Log.e(TAG, "Error migrating milestone ${realmMilestone.id}", e)
                    result.milestonesFailed++
                }
            }
            
            // Migrate Photos
            val realmPhotos = realm.query(Photo::class).find()
            Log.d(TAG, "Migrating ${realmPhotos.size} photos")
            
            realmPhotos.forEach { realmPhoto ->
                try {
                    val roomPhoto = PhotoEntity(
                        id = realmPhoto.id,
                        epochDay = realmPhoto.epochDay,
                        timestamp = realmPhoto.timestamp,
                        filePath = realmPhoto.filePath,
                        type = realmPhoto.type
                    )
                    roomDb.photoDao().insertPhoto(roomPhoto)
                    result.photosSuccess++
                } catch (e: Exception) {
                    Log.e(TAG, "Error migrating photo ${realmPhoto.id}", e)
                    result.photosFailed++
                }
            }
            
            // Migrate Audio Analyses
            val realmAudioAnalyses = realm.query(AudioAnalysis::class).find()
            Log.d(TAG, "Migrating ${realmAudioAnalyses.size} audio analyses")
            
            realmAudioAnalyses.forEach { realmAudio ->
                try {
                    val roomAudio = AudioAnalysisEntity(
                        id = realmAudio.id,
                        photoId = realmAudio.photoId,
                        f0Mean = realmAudio.f0Mean,
                        f0Min = realmAudio.f0Min,
                        f0Max = realmAudio.f0Max,
                        f1Mean = realmAudio.f1Mean,
                        f2Mean = realmAudio.f2Mean,
                        f3Mean = realmAudio.f3Mean,
                        f4Mean = realmAudio.f4Mean,
                        f0StdDev = realmAudio.f0StdDev,
                        durationSeconds = realmAudio.durationSeconds,
                        analysisTimestamp = realmAudio.analysisTimestamp
                    )
                    roomDb.audioAnalysisDao().insertAudioAnalysis(roomAudio)
                    result.audioAnalysesSuccess++
                } catch (e: Exception) {
                    Log.e(TAG, "Error migrating audio analysis ${realmAudio.id}", e)
                    result.audioAnalysesFailed++
                }
            }
            
            realm.close()
            
            // Mark migration as complete
            markMigrationComplete(context)
            
            result.success = true
            Log.d(TAG, "Migration completed successfully: $result")
            
        } catch (e: Exception) {
            Log.e(TAG, "Migration failed", e)
            result.success = false
            result.error = e.message
        }
        
        result
    }
    
    /**
     * Migration result with statistics
     */
    data class MigrationResult(
        var success: Boolean = false,
        var error: String? = null,
        var milestonesSuccess: Int = 0,
        var milestonesFailed: Int = 0,
        var photosSuccess: Int = 0,
        var photosFailed: Int = 0,
        var audioAnalysesSuccess: Int = 0,
        var audioAnalysesFailed: Int = 0
    ) {
        val totalSuccess: Int
            get() = milestonesSuccess + photosSuccess + audioAnalysesSuccess
            
        val totalFailed: Int
            get() = milestonesFailed + photosFailed + audioAnalysesFailed
            
        val total: Int
            get() = totalSuccess + totalFailed
    }
}
