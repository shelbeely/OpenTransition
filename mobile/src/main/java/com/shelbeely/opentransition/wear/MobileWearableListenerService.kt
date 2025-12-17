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
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.shelbeely.opentransition.shared.WearableConstants

/**
 * Service to handle messages and data from the Wear OS companion app
 */
class MobileWearableListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            WearableConstants.PATH_TRIGGER_PHOTO -> {
                handlePhotoTrigger(messageEvent.data)
            }
            WearableConstants.PATH_REQUEST_SYNC -> {
                handleSyncRequest()
            }
        }
    }

    private fun handlePhotoTrigger(data: ByteArray) {
        val photoType = String(data)
        
        // TODO: Integrate with existing photo capture functionality
        // For now, we'll just log that we received the request
        // In a full implementation, this would trigger the camera activity
        // with the specified photo type (face, body, etc.)
        
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
    }
}
