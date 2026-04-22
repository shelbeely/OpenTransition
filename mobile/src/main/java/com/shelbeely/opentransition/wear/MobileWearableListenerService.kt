/*
 * Copyright © 2025 OpenTransition. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.wear

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.shelbeely.opentransition.data.Photo
import com.shelbeely.opentransition.shared.WearableConstants
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.ZoneId

/**
 * Service to handle messages and data from the Wear OS companion app
 */
class MobileWearableListenerService : WearableListenerService() {

    companion object {
        private const val TAG = "MobileWearableListener"
        
        // Broadcast actions for camera controls
        const val ACTION_CAMERA_SHUTTER = "com.shelbeely.opentransition.CAMERA_SHUTTER"
        const val ACTION_CAMERA_ZOOM = "com.shelbeely.opentransition.CAMERA_ZOOM"
        const val ACTION_CAMERA_FLASH = "com.shelbeely.opentransition.CAMERA_FLASH"
        const val ACTION_CAMERA_SWITCH = "com.shelbeely.opentransition.CAMERA_SWITCH"
        const val ACTION_AUDIO_RECEIVED = "com.shelbeely.opentransition.AUDIO_RECEIVED"
        
        const val EXTRA_ZOOM_LEVEL = "zoom_level"
        const val EXTRA_FLASH_MODE = "flash_mode"
        const val EXTRA_AUDIO_FILE = "audio_file"
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            WearableConstants.PATH_TRIGGER_PHOTO -> {
                handlePhotoTrigger(messageEvent.data)
            }
            WearableConstants.PATH_REQUEST_SYNC -> {
                handleSyncRequest()
            }
            WearableConstants.PATH_CAMERA_SHUTTER -> {
                handleCameraShutter()
            }
            WearableConstants.PATH_CAMERA_ZOOM -> {
                handleCameraZoom(messageEvent.data)
            }
            WearableConstants.PATH_CAMERA_FLASH -> {
                handleCameraFlash(messageEvent.data)
            }
            WearableConstants.PATH_CAMERA_SWITCH -> {
                handleCameraSwitch()
            }
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                if (dataItem.uri.path == WearableConstants.DATA_PATH_AUDIO) {
                    handleAudioData(dataItem)
                }
            }
        }
    }

    private fun handlePhotoTrigger(data: ByteArray) {
        val photoType = String(data)
        
        // TODO: Integrate with existing photo capture functionality
        // For now, we'll just log that we received the request
        // In a full implementation, this would trigger the camera activity
        // with the specified photo type (face, body, etc.)
        
        Log.d(TAG, "Photo trigger received: $photoType")
        
        // Example of what this could do:
        // val intent = Intent(this, CameraActivity::class.java).apply {
        //     putExtra("photo_type", photoType)
        //     addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        // }
        // startActivity(intent)
    }

    private fun handleSyncRequest() {
        // TODO: Implement milestone data sync to wear device
        // This would fetch recent milestones and send them to the wear app
        // using the Wearable Data Layer API
        
        Log.d(TAG, "Sync request received")
    }

    private fun handleCameraShutter() {
        Log.d(TAG, "Camera shutter triggered from watch")
        
        // Broadcast to active camera fragment/activity
        val intent = Intent(ACTION_CAMERA_SHUTTER)
        sendBroadcast(intent)
    }

    private fun handleCameraZoom(data: ByteArray) {
        val zoomLevel = String(data).toFloatOrNull() ?: 1.0f
        Log.d(TAG, "Camera zoom: $zoomLevel")
        
        val intent = Intent(ACTION_CAMERA_ZOOM).apply {
            putExtra(EXTRA_ZOOM_LEVEL, zoomLevel)
        }
        sendBroadcast(intent)
    }

    private fun handleCameraFlash(data: ByteArray) {
        val flashMode = String(data)
        Log.d(TAG, "Camera flash mode: $flashMode")
        
        val intent = Intent(ACTION_CAMERA_FLASH).apply {
            putExtra(EXTRA_FLASH_MODE, flashMode)
        }
        sendBroadcast(intent)
    }

    private fun handleCameraSwitch() {
        Log.d(TAG, "Camera switch triggered from watch")
        
        val intent = Intent(ACTION_CAMERA_SWITCH)
        sendBroadcast(intent)
    }

    private fun handleAudioData(dataItem: com.google.android.gms.wearable.DataItem) {
        try {
            val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
            val audioBytes = dataMap.getByteArray(WearableConstants.KEY_AUDIO_DATA)
            val filename = dataMap.getString(WearableConstants.KEY_AUDIO_FILENAME)
            val timestamp = dataMap.getLong("timestamp", System.currentTimeMillis())
            val autoSent = dataMap.getBoolean("auto_sent", false)
            
            if (audioBytes != null && filename != null) {
                // Get current date for the photo entry
                val currentDate = LocalDate.now()
                val epochDay = currentDate.toEpochDay()
                
                // Save audio file to app's files directory
                val audioDir = File(filesDir, "audio")
                if (!audioDir.exists()) {
                    audioDir.mkdirs()
                }
                
                // Canonicalize the path to prevent path-traversal attacks: reject any filename
                // that resolves outside the designated audio directory.
                val resolvedFile = File(audioDir, filename).canonicalFile
                if (!resolvedFile.canonicalPath.startsWith(audioDir.canonicalPath + File.separator)) {
                    Log.w(TAG, "Rejected audio filename with path traversal: $filename")
                    return
                }
                
                val audioFile = resolvedFile
                FileOutputStream(audioFile).use { fos ->
                    fos.write(audioBytes)
                }
                
                Log.d(TAG, "Audio file saved: ${audioFile.absolutePath}")
                
                // Add audio to database as a Photo entry for current day
                try {
                    val config = RealmConfiguration.Builder(
                        schema = setOf(Photo::class)
                    ).build()
                    
                    val realm = Realm.open(config)
                    
                    realm.writeBlocking {
                        val photo = Photo().apply {
                            this.epochDay = epochDay
                            this.timestamp = timestamp
                            this.filePath = audioFile.absolutePath
                            this.type = Photo.TYPE_AUDIO
                        }
                        copyToRealm(photo)
                    }
                    
                    realm.close()
                    
                    Log.d(TAG, "Audio added to gallery for current day (epochDay: $epochDay)")
                } catch (e: Exception) {
                    Log.e(TAG, "Error adding audio to database", e)
                }
                
                // Broadcast that audio was received
                val intent = Intent(ACTION_AUDIO_RECEIVED).apply {
                    putExtra(EXTRA_AUDIO_FILE, audioFile.absolutePath)
                    putExtra("epoch_day", epochDay)
                    putExtra("auto_sent", autoSent)
                }
                sendBroadcast(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling audio data", e)
        }
    }
}
