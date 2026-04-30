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

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.wearable.*
import com.shelbeely.opentransition.shared.WearableConstants
import com.shelbeely.opentransition.shared.models.MilestoneData
import com.shelbeely.opentransition.shared.util.WearableHelper
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity(), DataClient.OnDataChangedListener, 
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {

    private lateinit var triggerPhotoButton: Button
    private lateinit var cameraControlButton: Button
    private lateinit var audioRecordButton: Button
    private lateinit var syncButton: Button
    private lateinit var statusText: TextView
    private lateinit var milestoneCountText: TextView
    private lateinit var lastMilestoneText: TextView
    private lateinit var dataClient: DataClient
    private lateinit var messageClient: MessageClient
    private lateinit var capabilityClient: CapabilityClient
    
    private var milestones: List<MilestoneData> = emptyList()
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    /** Receives broadcasts from the background [WearableListenerService] so the
     *  UI refreshes even while the activity is in the foreground. */
    private val milestoneUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadCachedMilestones()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        triggerPhotoButton = findViewById(R.id.trigger_photo_button)
        cameraControlButton = findViewById(R.id.camera_control_button)
        audioRecordButton = findViewById(R.id.audio_record_button)
        syncButton = findViewById(R.id.sync_button)
        statusText = findViewById(R.id.status_text)
        milestoneCountText = findViewById(R.id.milestone_count_text)
        lastMilestoneText = findViewById(R.id.last_milestone_text)

        dataClient = Wearable.getDataClient(this)
        messageClient = Wearable.getMessageClient(this)
        capabilityClient = Wearable.getCapabilityClient(this)

        triggerPhotoButton.setOnClickListener {
            sendPhotoTriggerMessage()
        }
        
        cameraControlButton.setOnClickListener {
            val intent = Intent(this, CameraControlActivity::class.java)
            startActivity(intent)
        }
        
        audioRecordButton.setOnClickListener {
            val intent = Intent(this, AudioRecordActivity::class.java)
            startActivity(intent)
        }
        
        syncButton.setOnClickListener {
            requestSync()
        }

        checkMobileAppConnection()
        loadCachedMilestones()
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(this)
        messageClient.addListener(this)
        capabilityClient.addListener(this, WearableConstants.CAPABILITY_MOBILE_APP)
        registerReceiver(
            milestoneUpdateReceiver,
            IntentFilter(WearableListenerService.ACTION_MILESTONE_UPDATED)
        )
        checkMobileAppConnection()
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(this)
        messageClient.removeListener(this)
        capabilityClient.removeListener(this)
        unregisterReceiver(milestoneUpdateReceiver)
    }

    private fun sendPhotoTriggerMessage() {
        capabilityClient
            .getCapability(WearableConstants.CAPABILITY_MOBILE_APP, CapabilityClient.FILTER_REACHABLE)
            .addOnSuccessListener { capabilityInfo ->
                val nodes = capabilityInfo.nodes
                if (nodes.isNotEmpty()) {
                    val nodeId = nodes.first().id
                    WearableHelper.sendPhotoTrigger(messageClient, nodeId, WearableConstants.PHOTO_TYPE_FACE)
                    
                    runOnUiThread {
                        Toast.makeText(this, R.string.photo_triggered, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, R.string.disconnected, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
    
    private fun requestSync() {
        capabilityClient
            .getCapability(WearableConstants.CAPABILITY_MOBILE_APP, CapabilityClient.FILTER_REACHABLE)
            .addOnSuccessListener { capabilityInfo ->
                val nodes = capabilityInfo.nodes
                if (nodes.isNotEmpty()) {
                    val nodeId = nodes.first().id
                    WearableHelper.sendSyncRequest(messageClient, nodeId)

                    runOnUiThread {
                        Toast.makeText(this, R.string.syncing, Toast.LENGTH_SHORT).show()
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
                        cameraControlButton.isEnabled = true
                        audioRecordButton.isEnabled = true
                        syncButton.isEnabled = true
                    } else {
                        statusText.text = getString(R.string.disconnected)
                        triggerPhotoButton.isEnabled = false
                        cameraControlButton.isEnabled = false
                        audioRecordButton.isEnabled = false
                        syncButton.isEnabled = false
                    }
                }
            }
    }
    
    private fun loadCachedMilestones() {
        val prefs = getSharedPreferences("wear_cache", Context.MODE_PRIVATE)
        val cachedJson = prefs.getString("milestones", null)

        if (cachedJson != null) {
            milestones = WearableHelper.parseMilestones(cachedJson)
            updateMilestoneUI()
        }
    }

    private fun saveMilestones(milestones: List<MilestoneData>) {
        this.milestones = milestones
        updateMilestoneUI()
    }
    
    private fun updateMilestoneUI() {
        runOnUiThread {
            milestoneCountText.text = getString(R.string.milestones_count, milestones.size)

            if (milestones.isNotEmpty()) {
                val latest = milestones.maxByOrNull { it.date }
                if (latest != null) {
                    val dateStr = dateFormat.format(Date(latest.date))
                    lastMilestoneText.text = getString(R.string.milestone_latest, latest.title, dateStr)
                    lastMilestoneText.visibility = View.VISIBLE
                }
            } else {
                lastMilestoneText.text = getString(R.string.no_milestones)
                lastMilestoneText.visibility = View.VISIBLE
            }
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                if (dataItem.uri.path == WearableConstants.DATA_PATH_MILESTONES) {
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    val milestonesJson = dataMap.getString(WearableConstants.KEY_MILESTONE_DATA)

                    if (milestonesJson != null) {
                        val updatedMilestones = WearableHelper.parseMilestones(milestonesJson)
                        saveMilestones(updatedMilestones)

                        runOnUiThread {
                            Toast.makeText(this, R.string.syncing, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            WearableConstants.PATH_MILESTONE_UPDATE -> {
                requestSync()
            }
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        checkMobileAppConnection()
    }
}
