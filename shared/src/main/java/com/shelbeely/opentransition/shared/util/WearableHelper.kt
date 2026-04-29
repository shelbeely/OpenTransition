/*
 * Copyright © 2025-2026 Shelbeely and OpenTransition contributors.
 *
 * Part of OpenTransition, a fork of TransTracks (© 2018-2021 TransTracks),
 * licensed under GPL-3.0-or-later. See the NOTICE and AUTHORS files for
 * full attribution.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.shared.util

import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.gson.Gson
import com.shelbeely.opentransition.shared.WearableConstants
import com.shelbeely.opentransition.shared.models.MilestoneData

/**
 * Helper class for common Wearable Data Layer operations
 */
object WearableHelper {
    
    private val gson = Gson()
    
    /**
     * Send a photo trigger message to the companion device
     * @param messageClient The MessageClient instance
     * @param nodeId The target node ID (from capability query)
     * @param photoType The type of photo to trigger (e.g., "face", "body")
     */
    fun sendPhotoTrigger(messageClient: MessageClient, nodeId: String, photoType: String) {
        val message = photoType.toByteArray()
        messageClient.sendMessage(nodeId, WearableConstants.PATH_TRIGGER_PHOTO, message)
    }
    
    /**
     * Send a sync request message to the companion device
     * @param messageClient The MessageClient instance
     * @param nodeId The target node ID
     */
    fun sendSyncRequest(messageClient: MessageClient, nodeId: String) {
        messageClient.sendMessage(nodeId, WearableConstants.PATH_REQUEST_SYNC, ByteArray(0))
    }
    
    /**
     * Sync milestone data to companion device using DataItem
     * @param dataClient The DataClient instance
     * @param milestones List of milestones to sync
     */
    fun syncMilestones(dataClient: DataClient, milestones: List<MilestoneData>) {
        val putDataMapRequest = PutDataMapRequest.create(WearableConstants.DATA_PATH_MILESTONES)
        val dataMap = putDataMapRequest.dataMap
        
        // Serialize milestones to JSON
        val milestonesJson = gson.toJson(milestones)
        dataMap.putString(WearableConstants.KEY_MILESTONE_DATA, milestonesJson)
        dataMap.putInt(WearableConstants.KEY_MILESTONE_COUNT, milestones.size)
        dataMap.putLong(WearableConstants.KEY_LAST_SYNC, System.currentTimeMillis())
        
        val putDataRequest = putDataMapRequest.asPutDataRequest()
        putDataRequest.setUrgent() // Request immediate sync
        
        dataClient.putDataItem(putDataRequest)
    }
    
    /**
     * Parse milestone data from a DataItem
     * @param jsonString The JSON string containing milestone data
     * @return List of MilestoneData objects
     */
    fun parseMilestones(jsonString: String): List<MilestoneData> {
        return try {
            gson.fromJson(jsonString, Array<MilestoneData>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
