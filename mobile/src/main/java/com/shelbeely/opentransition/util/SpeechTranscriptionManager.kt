/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Manages concurrent speech transcription during audio recording.
 *
 * Uses Android's built-in [SpeechRecognizer] to capture live transcription
 * while [AudioRecorderUtil] simultaneously records the audio file.
 *
 * The recognizer restarts automatically after each speech segment completes
 * so transcription continues for the full length of the recording.  If the
 * device does not support speech recognition, or if the recognizer encounters
 * an audio-source conflict with MediaRecorder, [start] returns `false` and the
 * caller should continue without transcription.
 *
 * **Thread safety**: All public methods must be called from the main thread
 * (required by [SpeechRecognizer]).
 */
object SpeechTranscriptionManager {

    private var recognizer: SpeechRecognizer? = null

    /** Text confirmed by completed speech segments. */
    private val confirmedText = StringBuilder()

    /** Latest partial result from the current in-progress segment. */
    private var pendingPartial = ""

    /** Whether transcription is requested to be active. */
    @Volatile private var active = false

    /** Callback invoked on the main thread whenever the visible transcript changes. */
    private var onUpdate: ((String) -> Unit)? = null

    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Starts continuous transcription.
     *
     * @param context   Application or Activity context (not a Service).
     * @param onUpdate  Called whenever the live transcript text changes.
     * @return `true` if speech recognition is available and listening started,
     *         `false` if recognition is unavailable on this device.
     */
    fun start(context: Context, onUpdate: (String) -> Unit): Boolean {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return false

        stop() // release any previous instance

        active = true
        confirmedText.clear()
        pendingPartial = ""
        this.onUpdate = onUpdate

        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(listener)
        }
        startListening()
        return true
    }

    /**
     * Stops transcription and returns the final accumulated transcript.
     * Safe to call even if [start] was never called or returned `false`.
     */
    fun stop(): String {
        active = false
        onUpdate = null
        try {
            recognizer?.stopListening()
            recognizer?.destroy()
        } catch (_: Exception) { /* ignore */ }
        recognizer = null

        val result = buildTranscript()
        confirmedText.clear()
        pendingPartial = ""
        return result
    }

    // ──────────────────────────────────────────────────────────────────────────

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            // Extend speech timeout to capture longer pauses mid-recording.
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 0L)
        }
        try {
            recognizer?.startListening(intent)
        } catch (_: Exception) {
            // Recognizer may have been destroyed concurrently — safe to ignore.
        }
    }

    private fun buildTranscript(): String {
        val base = confirmedText.toString().trimEnd()
        val partial = pendingPartial.trim()
        return when {
            base.isNotEmpty() && partial.isNotEmpty() -> "$base $partial"
            base.isNotEmpty() -> base
            else -> partial
        }
    }

    private fun notifyUpdate() {
        onUpdate?.invoke(buildTranscript())
    }

    // ──────────────────────────────────────────────────────────────────────────

    private val listener = object : RecognitionListener {

        override fun onPartialResults(partialResults: Bundle) {
            val texts = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = texts?.firstOrNull() ?: return
            pendingPartial = text
            notifyUpdate()
        }

        override fun onResults(results: Bundle) {
            val texts = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = texts?.firstOrNull()
            if (!text.isNullOrBlank()) {
                if (confirmedText.isNotEmpty()) confirmedText.append(' ')
                confirmedText.append(text.trim())
            }
            pendingPartial = ""
            notifyUpdate()

            // Restart immediately to capture the next speech segment.
            if (active) startListening()
        }

        override fun onError(error: Int) {
            pendingPartial = ""
            if (!active) return

            when (error) {
                // Non-fatal: no speech detected in the window → just restart.
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                    if (active) startListening()
                }
                // ERROR_AUDIO or other fatal errors: stop silently (graceful fallback).
                else -> {
                    active = false
                    // Do NOT clear confirmedText — preserve what was captured so far.
                    notifyUpdate()
                }
            }
        }

        // ── Unused callbacks ────────────────────────────────────────────────

        override fun onReadyForSpeech(params: Bundle?) = Unit
        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() = Unit
        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }
}
