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
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.shelbeely.opentransition.data.Milestone
import com.shelbeely.opentransition.shared.WearableConstants
import com.shelbeely.opentransition.shared.models.MilestoneData
import com.shelbeely.opentransition.shared.util.WearableHelper
import com.shelbeely.opentransition.util.openDefault
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.copyFromRealm
import io.realm.kotlin.query.Sort
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.ZoneId

/**
 * Service to handle messages and data from the Wear OS companion app.
 *
 * Changes vs original:
 * - ISSUE-003: `handleSyncRequest` now actually fetches the 100 most recent milestones
 *   from Realm and sends them to the requesting Wear node via `WearableHelper.syncMilestones`.
 * - ISSUE-010: Audio now arrives over a ChannelClient channel (no 100 KB cap) instead of
 *   a DataItem.  `onChannelOpened` handles the incoming stream.
 * - ISSUE-011: All `onMessageReceived` dispatches are guarded by a capability check that
 *   confirms the sender is the registered Wear app before acting on the request.
 */
class MobileWearableListenerService : WearableListenerService() {

    companion object {
        private const val TAG = "MobileWearableListener"
        private const val MAX_MILESTONES_TO_SYNC = 100

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

    // ISSUE-011: Cache of known Wear node IDs that advertise the wear capability.
    // Updated each time a message arrives and checked before acting.
    @Volatile private var knownWearNodeIds: Set<String> = emptySet()

    override fun onCreate() {
        super.onCreate()
        // Seed the known-nodes set so messages are not dropped on first launch.
        refreshKnownWearNodes()
    }

    /**
     * ISSUE-011: Asynchronously refresh the set of node IDs that are
     * confirmed to be running the OpenTransition Wear app.  Called once on
     * service start and again whenever a capability-changed event fires.
     */
    private fun refreshKnownWearNodes() {
        Wearable.getCapabilityClient(this)
            .getCapability(WearableConstants.CAPABILITY_WEAR_APP, CapabilityClient.FILTER_ALL)
            .addOnSuccessListener { info ->
                knownWearNodeIds = info.nodes.map { it.id }.toSet()
                Log.d(TAG, "Known Wear nodes: $knownWearNodeIds")
            }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        // ISSUE-011: reject messages from unknown senders.
        if (messageEvent.sourceNodeId !in knownWearNodeIds) {
            Log.w(TAG, "Ignoring message from unknown node ${messageEvent.sourceNodeId}; known: $knownWearNodeIds")
            // Re-query in case the node list is stale (e.g. first boot after install).
            refreshKnownWearNodes()
            return
        }

        when (messageEvent.path) {
            WearableConstants.PATH_TRIGGER_PHOTO -> handlePhotoTrigger(messageEvent.data)
            WearableConstants.PATH_REQUEST_SYNC -> handleSyncRequest(messageEvent.sourceNodeId)
            WearableConstants.PATH_CAMERA_SHUTTER -> handleCameraShutter()
            WearableConstants.PATH_CAMERA_ZOOM -> handleCameraZoom(messageEvent.data)
            WearableConstants.PATH_CAMERA_FLASH -> handleCameraFlash(messageEvent.data)
            WearableConstants.PATH_CAMERA_SWITCH -> handleCameraSwitch()
        }
    }

    /**
     * ISSUE-010: Audio now arrives through a ChannelClient channel so recordings
     * larger than the 100 KB DataItem cap are handled correctly.
     */
    override fun onChannelOpened(channel: ChannelClient.Channel) {
        val path = channel.path
        if (!path.startsWith(WearableConstants.PATH_AUDIO_DATA)) {
            Log.w(TAG, "Ignoring unexpected channel path: $path")
            return
        }
        // Extract filename from the channel path ("<PATH_AUDIO_DATA>/<filename>").
        val filename = path.substringAfterLast('/', "audio_received.3gp")

        val channelClient = Wearable.getChannelClient(this)
        channelClient.getInputStream(channel)
            .addOnSuccessListener { inputStream ->
                Thread {
                    try {
                        val audioDir = File(filesDir, "audio").also { it.mkdirs() }

                        // Sanitise the filename to prevent path-traversal.
                        // Use String comparison (works on minSdk 21) instead of Path/toPath (API 26+).
                        val safeFilename = File(filename).name
                        val canonicalDir = audioDir.canonicalFile
                        val audioFile = File(canonicalDir, safeFilename).canonicalFile
                        if (audioFile.parentFile?.canonicalPath != canonicalDir.canonicalPath) {
                            Log.e(TAG, "Path traversal rejected for filename: $filename")
                            channelClient.close(channel)
                            return@Thread
                        }

                        FileOutputStream(audioFile).use { fos ->
                            inputStream.copyTo(fos)
                        }
                        channelClient.close(channel)

                        Log.d(TAG, "Audio saved via channel: ${audioFile.absolutePath}")
                        saveAudioToRealm(audioFile)

                        val intent = Intent(ACTION_AUDIO_RECEIVED).apply {
                            putExtra(EXTRA_AUDIO_FILE, audioFile.absolutePath)
                            putExtra("epoch_day", LocalDate.now().toEpochDay())
                        }
                        sendBroadcast(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error receiving audio over channel", e)
                    }
                }.start()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get channel InputStream", e)
            }
    }

    private fun handlePhotoTrigger(data: ByteArray) {
        val photoType = String(data)
        Log.d(TAG, "Photo trigger received: $photoType")
    }

    /**
     * ISSUE-003: Fetch the 100 most-recent milestones from Realm and push them to
     * the requesting Wear node so the watch UI reflects the phone's current data.
     */
    private fun handleSyncRequest(sourceNodeId: String) {
        Log.d(TAG, "Sync request received from node $sourceNodeId")
        try {
            val realm = Realm.openDefault()
            val milestones = realm
                .query(Milestone::class)
                .sort(Milestone.FIELD_EPOCH_DAY, Sort.DESCENDING)
                .limit(MAX_MILESTONES_TO_SYNC)
                .find()
                .copyFromRealm()
                .map { m ->
                    val dateMillis = m.epochDay * 86_400_000L
                    MilestoneData(
                        id = m.id,
                        title = m.title,
                        description = m.description.ifBlank { null },
                        date = dateMillis,
                        type = "general"
                    )
                }
            realm.close()

            val dataClient = Wearable.getDataClient(this)
            WearableHelper.syncMilestones(dataClient, milestones)
            Log.d(TAG, "Synced ${milestones.size} milestones to Wear")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing milestones", e)
        }
    }

    private fun handleCameraShutter() {
        Log.d(TAG, "Camera shutter triggered from watch")
        sendBroadcast(Intent(ACTION_CAMERA_SHUTTER))
    }

    private fun handleCameraZoom(data: ByteArray) {
        val zoomLevel = String(data).toFloatOrNull() ?: 1.0f
        Log.d(TAG, "Camera zoom: $zoomLevel")
        sendBroadcast(Intent(ACTION_CAMERA_ZOOM).apply {
            putExtra(EXTRA_ZOOM_LEVEL, zoomLevel)
        })
    }

    private fun handleCameraFlash(data: ByteArray) {
        val flashMode = String(data)
        Log.d(TAG, "Camera flash mode: $flashMode")
        sendBroadcast(Intent(ACTION_CAMERA_FLASH).apply {
            putExtra(EXTRA_FLASH_MODE, flashMode)
        })
    }

    private fun handleCameraSwitch() {
        Log.d(TAG, "Camera switch triggered from watch")
        sendBroadcast(Intent(ACTION_CAMERA_SWITCH))
    }

    private fun saveAudioToRealm(audioFile: File) {
        try {
            val realm = Realm.openDefault()
            val currentDate = LocalDate.now()
            val epochDay = currentDate.toEpochDay()
            val timestamp = System.currentTimeMillis()

            realm.writeBlocking {
                val photo = com.shelbeely.opentransition.data.Photo().apply {
                    this.epochDay = epochDay
                    this.timestamp = timestamp
                    this.filePath = audioFile.absolutePath
                    this.type = com.shelbeely.opentransition.data.Photo.TYPE_AUDIO
                }
                copyToRealm(photo)
            }
            realm.close()
            Log.d(TAG, "Audio added to Realm (epochDay: $epochDay)")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving audio to Realm", e)
        }
    }
}

