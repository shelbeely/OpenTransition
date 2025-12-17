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
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.wearable.*
import com.google.gson.Gson
import com.shelbeely.opentransition.shared.WearableConstants
import com.shelbeely.opentransition.shared.models.MilestoneData
import com.shelbeely.opentransition.shared.util.WearableHelper
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity(), DataClient.OnDataChangedListener, 
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {

    private lateinit var triggerPhotoButton: Button
    private lateinit var syncButton: Button
    private lateinit var statusText: TextView
    private lateinit var milestoneCountText: TextView
    private lateinit var lastMilestoneText: TextView
    private lateinit var dataClient: DataClient
    private lateinit var messageClient: MessageClient
    private lateinit var capabilityClient: CapabilityClient
    
    private var milestones: List<MilestoneData> = emptyList()
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        triggerPhotoButton = findViewById(R.id.trigger_photo_button)
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
        checkMobileAppConnection()
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
                        Toast.makeText(this, "Syncing...", Toast.LENGTH_SHORT).show()
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
                        syncButton.isEnabled = true
                    } else {
                        statusText.text = getString(R.string.disconnected)
                        triggerPhotoButton.isEnabled = false
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
        
        val prefs = getSharedPreferences("wear_cache", Context.MODE_PRIVATE)
        val json = Gson().toJson(milestones)
        prefs.edit().putString("milestones", json).apply()
        
        updateMilestoneUI()
    }
    
    private fun updateMilestoneUI() {
        runOnUiThread {
            milestoneCountText.text = "Milestones: ${milestones.size}"
            
            if (milestones.isNotEmpty()) {
                val latest = milestones.maxByOrNull { it.date }
                if (latest != null) {
                    val dateStr = dateFormat.format(Date(latest.date))
                    lastMilestoneText.text = "Latest: ${latest.title}\n$dateStr"
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
                        val milestones = WearableHelper.parseMilestones(milestonesJson)
                        saveMilestones(milestones)
                        
                        runOnUiThread {
                            Toast.makeText(this, "Milestones synced", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            WearableConstants.PATH_MILESTONE_UPDATE -> {
                runOnUiThread {
                    Toast.makeText(this, "Milestone updated", Toast.LENGTH_SHORT).show()
                }
                requestSync()
            }
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        checkMobileAppConnection()
    }
}
