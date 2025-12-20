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
import android.net.Uri
import android.util.Log
import com.shelbeely.opentransition.data.AudioAnalysis
import com.shelbeely.opentransition.data.Milestone
import com.shelbeely.opentransition.data.Photo
import com.shelbeely.opentransition.database.DatabaseManager
import com.shelbeely.opentransition.database.room.entities.AudioAnalysisEntity
import com.shelbeely.opentransition.database.room.entities.MilestoneEntity
import com.shelbeely.opentransition.database.room.entities.PhotoEntity
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Handles importing Realm backup files into Room database
 * This allows users to migrate data from the forked version
 */
object RealmBackupImporter {
    private const val TAG = "RealmBackupImporter"
    private const val TEMP_REALM_FILE = "temp_import_realm.realm"
    
    /**
     * Import a Realm backup file from URI
     * @param context Application context
     * @param backupUri URI of the Realm backup file
     * @return Import result with statistics
     */
    suspend fun importFromBackup(context: Context, backupUri: Uri): ImportResult = withContext(Dispatchers.IO) {
        val result = ImportResult()
        val tempRealmFile = File(context.cacheDir, TEMP_REALM_FILE)
        
        try {
            Log.d(TAG, "Starting Realm backup import from URI: $backupUri")
            
            // Copy the backup file to a temporary location
            copyBackupToTemp(context, backupUri, tempRealmFile)
            
            // Open the Realm backup
            val config = RealmConfiguration.Builder(
                schema = setOf(Milestone::class, Photo::class, AudioAnalysis::class)
            )
                .directory(context.cacheDir.absolutePath)
                .name(TEMP_REALM_FILE)
                .build()
            
            val realm = Realm.open(config)
            
            try {
                // Get Room database
                val roomDb = DatabaseManager.getDatabase(context)
                
                // Import Milestones
                val realmMilestones = realm.query(Milestone::class).find()
                Log.d(TAG, "Importing ${realmMilestones.size} milestones")
            
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
                    Log.e(TAG, "Error importing milestone ${realmMilestone.id}", e)
                    result.milestonesFailed++
                }
            }
            
            // Import Photos
            val realmPhotos = realm.query(Photo::class).find()
            Log.d(TAG, "Importing ${realmPhotos.size} photos")
            
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
                    Log.e(TAG, "Error importing photo ${realmPhoto.id}", e)
                    result.photosFailed++
                }
            }
            
            // Import Audio Analyses
            val realmAudioAnalyses = realm.query(AudioAnalysis::class).find()
            Log.d(TAG, "Importing ${realmAudioAnalyses.size} audio analyses")
            
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
                    Log.e(TAG, "Error importing audio analysis ${realmAudio.id}", e)
                    result.audioAnalysesFailed++
                }
            }
            
            result.success = true
            Log.d(TAG, "Import completed successfully: $result")
            
            } finally {
                // Always close realm and clean up temp file
                realm.close()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Import failed", e)
            result.success = false
            result.error = e.message
        } finally {
            // Ensure temp file is deleted even if import fails
            if (tempRealmFile.exists()) {
                tempRealmFile.delete()
            }
        }
        
        result
    }
    
    /**
     * Copy backup file from URI to temporary location
     */
    private fun copyBackupToTemp(context: Context, uri: Uri, destFile: File) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalArgumentException("Unable to read the selected backup file. Please ensure the file is accessible and not corrupted.")
    }
    
    /**
     * Import result with statistics
     */
    data class ImportResult(
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
