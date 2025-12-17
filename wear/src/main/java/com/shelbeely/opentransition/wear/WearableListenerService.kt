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

import com.google.android.gms.wearable.*
import com.shelbeely.opentransition.shared.WearableConstants

class WearableListenerService : com.google.android.gms.wearable.WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                when (dataItem.uri.path) {
                    WearableConstants.DATA_PATH_MILESTONES -> {
                        // Handle milestone data sync
                        handleMilestoneSync(dataItem)
                    }
                    WearableConstants.DATA_PATH_SETTINGS -> {
                        // Handle settings sync
                        handleSettingsSync(dataItem)
                    }
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            WearableConstants.PATH_MILESTONE_UPDATE -> {
                // Handle milestone update notification
                handleMilestoneUpdate(messageEvent.data)
            }
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        // Handle capability changes (e.g., mobile app connected/disconnected)
        if (capabilityInfo.name == WearableConstants.CAPABILITY_MOBILE_APP) {
            // Update UI or trigger sync if needed
        }
    }

    private fun handleMilestoneSync(dataItem: DataItem) {
        // Parse and store milestone data
        // This would typically update local storage or UI
    }

    private fun handleSettingsSync(dataItem: DataItem) {
        // Parse and apply settings
    }

    private fun handleMilestoneUpdate(data: ByteArray) {
        // Handle individual milestone update
        // Could show a notification or update UI
    }
}
