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

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.wearable.*
import com.shelbeely.opentransition.shared.WearableConstants

class MainActivity : Activity(), DataClient.OnDataChangedListener, 
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {

    private lateinit var triggerPhotoButton: Button
    private lateinit var statusText: TextView
    private lateinit var dataClient: DataClient
    private lateinit var messageClient: MessageClient
    private lateinit var capabilityClient: CapabilityClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        triggerPhotoButton = findViewById(R.id.trigger_photo_button)
        statusText = findViewById(R.id.status_text)

        dataClient = Wearable.getDataClient(this)
        messageClient = Wearable.getMessageClient(this)
        capabilityClient = Wearable.getCapabilityClient(this)

        triggerPhotoButton.setOnClickListener {
            sendPhotoTriggerMessage()
        }

        checkMobileAppConnection()
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(this)
        messageClient.addListener(this)
        capabilityClient.addListener(this, WearableConstants.CAPABILITY_MOBILE_APP)
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(this)
        messageClient.removeListener(this)
        capabilityClient.removeListener(this)
    }

    private fun sendPhotoTriggerMessage() {
        capabilityClient
            .getCapability(WearableConstants.CAPABILITY_MOBILE_APP, CapabilityClient.FILTER_REACHABLE)
            .addOnSuccessListener { capabilityInfo ->
                val nodes = capabilityInfo.nodes
                if (nodes.isNotEmpty()) {
                    val nodeId = nodes.first().id
                    val message = WearableConstants.PHOTO_TYPE_FACE.toByteArray()
                    
                    messageClient.sendMessage(nodeId, WearableConstants.PATH_TRIGGER_PHOTO, message)
                        .addOnSuccessListener {
                            runOnUiThread {
                                Toast.makeText(this, R.string.photo_triggered, Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            runOnUiThread {
                                Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, R.string.disconnected, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun checkMobileAppConnection() {
        capabilityClient
            .getCapability(WearableConstants.CAPABILITY_MOBILE_APP, CapabilityClient.FILTER_REACHABLE)
            .addOnSuccessListener { capabilityInfo ->
                val nodes = capabilityInfo.nodes
                runOnUiThread {
                    if (nodes.isNotEmpty()) {
                        statusText.text = getString(R.string.connected)
                        triggerPhotoButton.isEnabled = true
                    } else {
                        statusText.text = getString(R.string.disconnected)
                        triggerPhotoButton.isEnabled = false
                    }
                }
            }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        // Handle data changes from mobile app
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                if (dataItem.uri.path == WearableConstants.DATA_PATH_MILESTONES) {
                    // Handle milestone data updates
                    runOnUiThread {
                        Toast.makeText(this, "Milestones updated", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        // Handle messages from mobile app
        when (messageEvent.path) {
            WearableConstants.PATH_MILESTONE_UPDATE -> {
                runOnUiThread {
                    Toast.makeText(this, "Milestone updated", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        checkMobileAppConnection()
    }
}
