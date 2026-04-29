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

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.wearable.*
import com.shelbeely.opentransition.shared.WearableConstants
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Audio recording activity for Wear OS.
 *
 * Changes vs original:
 * - ISSUE-009: The duration-timer `Runnable` is now cancelled in `onPause` so it cannot
 *   leak and keep posting after the activity is no longer visible.
 * - ISSUE-010: Audio is shipped to the phone via `ChannelClient` instead of
 *   `DataClient.putDataItem`. The DataItem API has a hard 100 KB limit; larger
 *   recordings were silently truncated or dropped. `ChannelClient` has no
 *   such cap and is the recommended path for large payloads.
 * - ISSUE-017: All UI text is now sourced from `strings.xml`.
 */
class AudioRecordActivity : Activity() {

    private lateinit var recordButton: Button
    private lateinit var statusText: TextView
    private lateinit var durationText: TextView

    private lateinit var capabilityClient: CapabilityClient
    private lateinit var channelClient: ChannelClient

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false
    private var recordingStartTime = 0L

    // Wear PARTIAL_WAKE_LOCK: keep CPU running while audio is being captured so
    // the recording isn't truncated when the screen turns off.
    private var wakeLock: PowerManager.WakeLock? = null

    // ISSUE-009: keep a reference so we can cancel the runnable in onPause.
    private val handler = Handler(Looper.getMainLooper())
    private val durationRunnable = object : Runnable {
        override fun run() {
            if (isRecording) {
                val duration = (System.currentTimeMillis() - recordingStartTime) / 1000
                durationText.text = String.format(Locale.ROOT, "%02d:%02d", duration / 60, duration % 60)
                handler.postDelayed(this, 1_000)
            }
        }
    }

    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_record)

        channelClient = Wearable.getChannelClient(this)
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
                Toast.makeText(this, R.string.microphone_permission_required, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startRecording() {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "audio_$timestamp.3gp"
            audioFile = File(cacheDir, fileName)

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile!!.absolutePath)
                prepare()
                start()
            }

            // Wear PARTIAL_WAKE_LOCK: hold the CPU until the recording stops so the
            // sample isn't truncated when the screen times out. Released in stopRecording().
            acquireRecordingWakeLock()

            isRecording = true
            recordingStartTime = System.currentTimeMillis()

            recordButton.text = getString(R.string.stop_label)
            statusText.text = getString(R.string.recording_in_progress)

            // ISSUE-009: start timer via the retained Runnable reference.
            handler.post(durationRunnable)

        } catch (e: Exception) {
            // Recording failed to start — make sure we don't leak the wake lock.
            releaseRecordingWakeLock()
            Toast.makeText(this, getString(R.string.recording_failed, e.message), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        try {
            // ISSUE-009: cancel the pending timer callback first.
            isRecording = false
            handler.removeCallbacks(durationRunnable)

            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            recordButton.text = getString(R.string.record_label)
            statusText.text = getString(R.string.sending_to_phone)

            audioFile?.let { file ->
                sendAudioToPhone(file)
            }

        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.stop_failed, e.message), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        } finally {
            // Always release the wake lock paired with startRecording().
            releaseRecordingWakeLock()
        }
    }

    private fun acquireRecordingWakeLock() {
        if (wakeLock?.isHeld == true) return
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "OpenTransition:AudioRecord"
        ).apply {
            setReferenceCounted(false)
            // 10-minute safety timeout in case stopRecording() is never called.
            acquire(10 * 60 * 1000L)
        }
    }

    private fun releaseRecordingWakeLock() {
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
    }

    override fun onPause() {
        super.onPause()
        // ISSUE-009: ensure the timer is not posting after the activity is paused.
        handler.removeCallbacks(durationRunnable)
    }

    private fun sendAudioToPhone(file: File) {
        if (!file.exists()) {
            Toast.makeText(this, R.string.no_audio_file, Toast.LENGTH_SHORT).show()
            statusText.text = getString(R.string.error_file_not_found)
            return
        }

        capabilityClient
            .getCapability(WearableConstants.CAPABILITY_MOBILE_APP, CapabilityClient.FILTER_REACHABLE)
            .addOnSuccessListener { capabilityInfo ->
                val node = capabilityInfo.nodes.firstOrNull()
                if (node != null) {
                    sendAudioViaChannel(file, node)
                } else {
                    runOnUiThread {
                        Toast.makeText(this, R.string.phone_not_connected, Toast.LENGTH_SHORT).show()
                        statusText.text = getString(R.string.phone_disconnected)
                    }
                }
            }
    }

    /**
     * ISSUE-010: Send audio to phone via ChannelClient instead of DataClient.putDataItem.
     *
     * The DataItem API caps payloads at 100 KB — any audio longer than ~5 s would be
     * silently dropped. ChannelClient streams the file with no size limit.
     */
    private fun sendAudioViaChannel(file: File, node: Node) {
        // Use a path that encodes the original filename so the mobile side can
        // reconstruct the file with the right name.
        val channelPath = "${WearableConstants.PATH_AUDIO_DATA}/${file.name}"
        channelClient.openChannel(node.id, channelPath)
            .addOnSuccessListener { channel ->
                channelClient.getOutputStream(channel)
                    .addOnSuccessListener { outputStream: OutputStream ->
                        Thread {
                            try {
                                FileInputStream(file).use { inputStream: InputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                                outputStream.close()
                                channelClient.close(channel)
                                file.delete()
                                runOnUiThread {
                                    statusText.text = getString(R.string.sent_to_phone)
                                    Toast.makeText(this, R.string.audio_saved_to_day, Toast.LENGTH_SHORT).show()
                                    durationText.text = getString(R.string.duration_zero)
                                    handler.postDelayed({ finish() }, 2_000)
                                }
                            } catch (e: Exception) {
                                runOnUiThread {
                                    statusText.text = getString(R.string.send_failed)
                                    Toast.makeText(
                                        this,
                                        getString(R.string.error_sending_audio, e.message),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }.start()
                    }
                    .addOnFailureListener { e ->
                        runOnUiThread {
                            statusText.text = getString(R.string.send_failed)
                            Toast.makeText(this, getString(R.string.failed_to_send, e.message), Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            .addOnFailureListener { e ->
                runOnUiThread {
                    statusText.text = getString(R.string.send_failed)
                    Toast.makeText(this, getString(R.string.failed_to_send, e.message), Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRecording) {
            stopRecording()
        }
        // Defensive release in case stopRecording wasn't called (e.g. crash on stop()).
        releaseRecordingWakeLock()
    }
}

