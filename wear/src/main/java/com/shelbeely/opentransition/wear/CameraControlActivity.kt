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
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.wearable.*
import com.shelbeely.opentransition.shared.WearableConstants

/**
 * Camera remote control activity for Wear OS
 * Provides shutter, zoom, flash toggle, and camera switch controls
 */
class CameraControlActivity : Activity() {

    private lateinit var shutterButton: Button
    private lateinit var flashButton: Button
    private lateinit var switchCameraButton: Button
    private lateinit var zoomSeekBar: SeekBar
    private lateinit var zoomText: TextView
    private lateinit var statusText: TextView
    private lateinit var messageClient: MessageClient
    private lateinit var capabilityClient: CapabilityClient
    
    private var currentFlashMode = WearableConstants.FLASH_MODE_AUTO
    private var currentZoom = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_control)

        messageClient = Wearable.getMessageClient(this)
        capabilityClient = Wearable.getCapabilityClient(this)

        shutterButton = findViewById(R.id.shutter_button)
        flashButton = findViewById(R.id.flash_button)
        switchCameraButton = findViewById(R.id.switch_camera_button)
        zoomSeekBar = findViewById(R.id.zoom_seekbar)
        zoomText = findViewById(R.id.zoom_text)
        statusText = findViewById(R.id.camera_status_text)

        setupControls()
        checkConnection()
    }

    private fun setupControls() {
        // Shutter button - take photo
        shutterButton.setOnClickListener {
            sendCameraCommand(WearableConstants.PATH_CAMERA_SHUTTER, ByteArray(0))
            Toast.makeText(this, R.string.shutter_triggered, Toast.LENGTH_SHORT).show()
        }

        // Flash button - cycle through modes
        flashButton.setOnClickListener {
            currentFlashMode = when (currentFlashMode) {
                WearableConstants.FLASH_MODE_AUTO -> WearableConstants.FLASH_MODE_ON
                WearableConstants.FLASH_MODE_ON -> WearableConstants.FLASH_MODE_OFF
                else -> WearableConstants.FLASH_MODE_AUTO
            }
            updateFlashButton()
            sendCameraCommand(WearableConstants.PATH_CAMERA_FLASH, currentFlashMode.toByteArray())
        }

        // Switch camera button - toggle front/back
        switchCameraButton.setOnClickListener {
            sendCameraCommand(WearableConstants.PATH_CAMERA_SWITCH, ByteArray(0))
            Toast.makeText(this, R.string.camera_switched, Toast.LENGTH_SHORT).show()
        }

        // Zoom control
        zoomSeekBar.max = 100
        zoomSeekBar.progress = 0
        zoomSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentZoom = 1.0f + (progress / 100f) * 9.0f // 1.0x to 10.0x
                zoomText.text = String.format("%.1fx", currentZoom)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                sendCameraCommand(WearableConstants.PATH_CAMERA_ZOOM, currentZoom.toString().toByteArray())
            }
        })

        updateFlashButton()
    }

    private fun updateFlashButton() {
        flashButton.text = when (currentFlashMode) {
            WearableConstants.FLASH_MODE_AUTO -> getString(R.string.flash_auto)
            WearableConstants.FLASH_MODE_ON -> getString(R.string.flash_on)
            else -> getString(R.string.flash_off)
        }
    }

    private fun sendCameraCommand(path: String, data: ByteArray) {
        capabilityClient
            .getCapability(WearableConstants.CAPABILITY_MOBILE_APP, CapabilityClient.FILTER_REACHABLE)
            .addOnSuccessListener { capabilityInfo ->
                val nodes = capabilityInfo.nodes
                if (nodes.isNotEmpty()) {
                    val nodeId = nodes.first().id
                    messageClient.sendMessage(nodeId, path, data)
                } else {
                    runOnUiThread {
                        Toast.makeText(this, R.string.phone_not_connected, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun checkConnection() {
        capabilityClient
            .getCapability(WearableConstants.CAPABILITY_MOBILE_APP, CapabilityClient.FILTER_REACHABLE)
            .addOnSuccessListener { capabilityInfo ->
                val nodes = capabilityInfo.nodes
                runOnUiThread {
                    if (nodes.isNotEmpty()) {
                        statusText.text = getString(R.string.camera_ready)
                        enableControls(true)
                    } else {
                        statusText.text = getString(R.string.phone_disconnected)
                        enableControls(false)
                    }
                }
            }
    }

    private fun enableControls(enabled: Boolean) {
        shutterButton.isEnabled = enabled
        flashButton.isEnabled = enabled
        switchCameraButton.isEnabled = enabled
        zoomSeekBar.isEnabled = enabled
    }
}
