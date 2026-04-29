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

package com.shelbeely.opentransition.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.shelbeely.opentransition.shared.WearableConstants

/**
 * Background WearableListenerService that keeps the watch-side milestone cache
 * up-to-date even when the app is not in the foreground.
 *
 * ISSUE-007: Previously all handler bodies were empty stubs. They now:
 * - `handleMilestoneSync` — persists the incoming JSON to the same
 *   "wear_cache" SharedPreferences key that `MainActivity` reads from, so the
 *   UI is always in-sync with the last push from the phone.
 * - `handleSettingsSync` — placeholder with logging; extend as new settings are
 *   added to the Data Layer protocol.
 * - `handleMilestoneUpdate` — re-broadcasts a local broadcast so that the
 *   foreground `MainActivity` can refresh its UI if it is currently visible.
 */
class WearableListenerService : com.google.android.gms.wearable.WearableListenerService() {

    companion object {
        private const val TAG = "WearListenerService"
        const val ACTION_MILESTONE_UPDATED = "com.shelbeely.opentransition.wear.MILESTONE_UPDATED"
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                when (dataItem.uri.path) {
                    WearableConstants.DATA_PATH_MILESTONES -> handleMilestoneSync(dataItem)
                    WearableConstants.DATA_PATH_SETTINGS -> handleSettingsSync(dataItem)
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            WearableConstants.PATH_MILESTONE_UPDATE -> handleMilestoneUpdate(messageEvent.data)
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        Log.d(TAG, "Capability changed: ${capabilityInfo.name}, nodes: ${capabilityInfo.nodes.size}")
    }

    /**
     * Persists the milestone JSON to the same SharedPreferences that
     * [MainActivity] reads on startup so the watch always shows fresh data.
     */
    private fun handleMilestoneSync(dataItem: DataItem) {
        try {
            val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
            val milestonesJson = dataMap.getString(WearableConstants.KEY_MILESTONE_DATA) ?: return
            val count = dataMap.getInt(WearableConstants.KEY_MILESTONE_COUNT, 0)

            getSharedPreferences("wear_cache", Context.MODE_PRIVATE)
                .edit()
                .putString("milestones", milestonesJson)
                .apply()

            Log.d(TAG, "Cached $count milestones from data-layer sync")

            // Notify any foreground activity that the data changed.
            sendBroadcast(android.content.Intent(ACTION_MILESTONE_UPDATED))
        } catch (e: Exception) {
            Log.e(TAG, "Error handling milestone sync", e)
        }
    }

    private fun handleSettingsSync(dataItem: DataItem) {
        Log.d(TAG, "Settings sync received from phone (not yet consumed)")
    }

    /**
     * Phone notified us that an individual milestone was modified.  Broadcast
     * locally so a foreground [MainActivity] can call `requestSync()` to pull
     * the full updated list.
     */
    private fun handleMilestoneUpdate(data: ByteArray) {
        Log.d(TAG, "Milestone update notification received")
        sendBroadcast(android.content.Intent(ACTION_MILESTONE_UPDATED))
    }
}

