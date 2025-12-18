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

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.wearable.*
import com.shelbeely.opentransition.shared.WearableConstants
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Audio recording activity for Wear OS
 * Records audio on the watch and sends it to the mobile app
 */
class AudioRecordActivity : Activity() {

    private lateinit var recordButton: Button
    private lateinit var statusText: TextView
    private lateinit var durationText: TextView
    
    private lateinit var messageClient: MessageClient
    private lateinit var dataClient: DataClient
    private lateinit var capabilityClient: CapabilityClient
    
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false
    private var recordingStartTime = 0L
    
    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_record)

        messageClient = Wearable.getMessageClient(this)
        dataClient = Wearable.getDataClient(this)
        capabilityClient = Wearable.getCapabilityClient(this)

        recordButton = findViewById(R.id.record_button)
        statusText = findViewById(R.id.audio_status_text)
        durationText = findViewById(R.id.duration_text)

        setupControls()
        checkPermissions()
    }

    private fun setupControls() {
        recordButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startRecording() {
        try {
            // Create audio file
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "audio_$timestamp.3gp"
            audioFile = File(cacheDir, fileName)

            // Setup MediaRecorder
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile!!.absolutePath)
                prepare()
                start()
            }

            isRecording = true
            recordingStartTime = System.currentTimeMillis()
            
            recordButton.text = "⏹️ Stop"
            statusText.text = "Recording..."
            
            // Start duration timer
            updateDuration()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Recording failed: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            
            isRecording = false
            recordButton.text = "🎤 Record"
            statusText.text = "Sending to phone..."
            
            // Automatically send audio to phone
            audioFile?.let { file ->
                sendAudioToPhone(file)
            }
            
        } catch (e: Exception) {
            Toast.makeText(this, "Stop failed: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun updateDuration() {
        if (isRecording) {
            val duration = (System.currentTimeMillis() - recordingStartTime) / 1000
            durationText.text = String.format("%02d:%02d", duration / 60, duration % 60)
            durationText.postDelayed({ updateDuration() }, 1000)
        }
    }

    private fun sendAudioToPhone(file: File) {
        if (!file.exists()) {
            Toast.makeText(this, "No audio file to send", Toast.LENGTH_SHORT).show()
            statusText.text = "Error: File not found"
            return
        }

        capabilityClient
            .getCapability(WearableConstants.CAPABILITY_MOBILE_APP, CapabilityClient.FILTER_REACHABLE)
            .addOnSuccessListener { capabilityInfo ->
                val nodes = capabilityInfo.nodes
                if (nodes.isNotEmpty()) {
                    sendAudioData(file)
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Phone not connected", Toast.LENGTH_SHORT).show()
                        statusText.text = "Phone disconnected"
                    }
                }
            }
    }

    private fun sendAudioData(file: File) {
        try {
            statusText.text = "Sending audio..."
            
            // Read audio file
            val audioBytes = FileInputStream(file).use { it.readBytes() }
            
            // Get current date for association
            val calendar = Calendar.getInstance()
            val dateKey = String.format("%04d%02d%02d", 
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            
            // Create PutDataRequest with audio data
            val putDataMapRequest = PutDataMapRequest.create(WearableConstants.DATA_PATH_AUDIO)
            val dataMap = putDataMapRequest.dataMap
            
            dataMap.putByteArray(WearableConstants.KEY_AUDIO_DATA, audioBytes)
            dataMap.putString(WearableConstants.KEY_AUDIO_FILENAME, file.name)
            dataMap.putLong("timestamp", System.currentTimeMillis())
            dataMap.putString("date_key", dateKey)  // Add date association
            dataMap.putBoolean("auto_sent", true)   // Mark as auto-sent
            
            val putDataRequest = putDataMapRequest.asPutDataRequest()
            putDataRequest.setUrgent()
            
            dataClient.putDataItem(putDataRequest)
                .addOnSuccessListener {
                    runOnUiThread {
                        statusText.text = "Sent to phone"
                        Toast.makeText(this, "Audio saved to current day", Toast.LENGTH_SHORT).show()
                        
                        // Clean up
                        file.delete()
                        audioFile = null
                        durationText.text = "00:00"
                        
                        // Close activity after successful send
                        durationText.postDelayed({ finish() }, 2000)
                    }
                }
                .addOnFailureListener { e ->
                    runOnUiThread {
                        statusText.text = "Send failed"
                        Toast.makeText(this, "Failed to send: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                
        } catch (e: Exception) {
            Toast.makeText(this, "Error sending audio: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRecording) {
            stopRecording()
        }
    }
}
