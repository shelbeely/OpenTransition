/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.recordaudio

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.data.AudioAnalysis
import com.shelbeely.opentransition.data.Photo
import com.shelbeely.opentransition.database.DatabaseManager
import com.shelbeely.opentransition.database.room.entities.AudioAnalysisEntity
import com.shelbeely.opentransition.database.room.entities.PhotoEntity
import com.shelbeely.opentransition.util.AudioAnalysisUtil
import com.shelbeely.opentransition.util.AudioRecorderUtil
import com.shelbeely.opentransition.util.FileUtil
import com.shelbeely.opentransition.util.SessionSummaryBuilder
import com.shelbeely.opentransition.util.SpeechTranscriptionManager
import com.shelbeely.opentransition.util.openDefault
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class RecordAudioFragment : Fragment(R.layout.record_audio) {
    
    private val audioRecorder by lazy { AudioRecorderUtil(requireContext()) }
    private val handler = Handler(Looper.getMainLooper())
    private var recordingStartTime = 0L
    private var timerRunnable: Runnable? = null
    /** Accumulated live transcript; updated by SpeechTranscriptionManager callbacks. */
    private var liveTranscript = ""
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startRecording()
        } else {
            view?.let {
                Snackbar.make(it, R.string.permission_audio_required, Snackbar.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val recordAudioView = view as RecordAudioView
        
        recordAudioView.setOnRecordClickListener {
            if (audioRecorder.isRecording()) {
                stopRecording()
            } else {
                checkPermissionAndRecord()
            }
        }
        
        recordAudioView.setOnSaveClickListener {
            saveRecording()
        }
        
        recordAudioView.setOnCancelClickListener {
            audioRecorder.cancelRecording()
            findNavController().popBackStack()
        }
        
        recordAudioView.setOnDateClickListener {
            // ITEM-53: Show date picker so users can override the recording date.
            val current = (view as? RecordAudioView)?.getSelectedDate() ?: LocalDate.now()
            // DatePickerDialog uses 0-based months
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    (view as? RecordAudioView)?.setSelectedDate(
                        LocalDate.of(year, month + 1, dayOfMonth)
                    )
                },
                current.year,
                current.monthValue - 1,
                current.dayOfMonth
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }
    }
    
    private fun checkPermissionAndRecord() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startRecording()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
    
    private fun startRecording() {
        val tempFile = FileUtil.getTempAudioFile()
        val success = audioRecorder.startRecording(tempFile)
        
        if (success) {
            recordingStartTime = System.currentTimeMillis()
            startTimer()
            (view as? RecordAudioView)?.setRecordingState(true)
            // Start concurrent speech transcription. Gracefully ignored if unavailable.
            liveTranscript = ""
            SpeechTranscriptionManager.start(requireContext()) { transcript ->
                liveTranscript = transcript
                (view as? RecordAudioView)?.updateTranscript(transcript)
            }
        } else {
            Snackbar.make(requireView(), R.string.audio_record_error, Snackbar.LENGTH_LONG).show()
        }
    }
    
    private fun stopRecording() {
        audioRecorder.stopRecording()
        stopTimer()
        // Collect whatever transcript was accumulated; the manager releases the recognizer.
        liveTranscript = SpeechTranscriptionManager.stop().ifEmpty { liveTranscript }
        (view as? RecordAudioView)?.apply {
            setRecordingState(false)
            enableSaveButton(true)
            // Show waveform preview for the recorded file
            audioRecorder.getOutputFile()?.let { setAudioFile(it) }
            // Show final transcript
            updateTranscript(liveTranscript)
            // ITEM-53: Auto-attach to today's date when photos exist for that day.
            autoAttachToTodayIfPhotosExist()
        }
    }

    /**
     * ITEM-53: If the device has photos for today's epoch day, keep the date field at
     * LocalDate.now() (it already defaults to today).  If no photos exist yet, do nothing
     * — the default is still today and the user can change it via the date picker.
     * Either way, a Snackbar hints the user what date was pre-selected.
     */
    private fun autoAttachToTodayIfPhotosExist() {
        val today = LocalDate.now()
        val epochDay = today.toEpochDay()
        try {
            val realm = Realm.openDefault()
            val hasPhotosForToday = realm
                .query(Photo::class, "${Photo.FIELD_EPOCH_DAY} == $epochDay")
                .count()
                .find() > 0
            realm.close()
            if (hasPhotosForToday) {
                (view as? RecordAudioView)?.setSelectedDate(today)
                view?.let {
                    Snackbar.make(it, R.string.audio_attached_to_today, Snackbar.LENGTH_SHORT).show()
                }
            }
        } catch (_: Exception) {
            // Ignore query errors; user can still pick the date manually
        }
    }
    
    private fun startTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                val elapsed = (System.currentTimeMillis() - recordingStartTime) / 1000
                val minutes = elapsed / 60
                val seconds = elapsed % 60
                (view as? RecordAudioView)?.updateTimer(String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds))
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timerRunnable!!)
    }
    
    private fun stopTimer() {
        timerRunnable?.let { handler.removeCallbacks(it) }
        timerRunnable = null
    }
    
    private fun saveRecording() {
        val recordedFile = audioRecorder.getOutputFile() ?: return
        
        (view as? RecordAudioView)?.showAnalyzing(true)
        
        // Run analysis in background
        Thread {
            val analysis = AudioAnalysisUtil.analyzeAudioFile(recordedFile)
            
            handler.post {
                (view as? RecordAudioView)?.showAnalyzing(false)
                
                if (analysis != null) {
                    saveToDatabase(recordedFile, analysis)
                } else {
                    Snackbar.make(requireView(), R.string.audio_analysis_error, Snackbar.LENGTH_LONG).show()
                }
            }
        }.start()
    }
    
    private fun saveToDatabase(audioFile: java.io.File, analysis: AudioAnalysis) {
        val date = (view as? RecordAudioView)?.getSelectedDate() ?: LocalDate.now()
        val epochDay = date.toEpochDay()
        val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Move file to permanent location
                val permanentFile = FileUtil.getNewAudioFile(date)
                audioFile.copyTo(permanentFile, overwrite = true)
                audioFile.delete()

                val photoId = UUID.randomUUID().toString()

                // Build human-readable session summary
                val summaryText = SessionSummaryBuilder.build(
                    f0Mean              = analysis.f0Mean,
                    f0Min               = analysis.f0Min,
                    f0Max               = analysis.f0Max,
                    pitchRangeHz        = analysis.pitchRangeHz,
                    voicedRatio         = analysis.voicedRatio,
                    pitchStabilityScore = analysis.pitchStabilityScore,
                    intonationMovement  = analysis.intonationMovement,
                    intensityMeanDb     = analysis.intensityMeanDb,
                    durationSeconds     = analysis.durationSeconds
                )

                // Persist photo to Realm (legacy path kept for backwards compat)
                val realm = Realm.openDefault()
                realm.writeBlocking {
                    val photo = Photo().apply {
                        this.id        = photoId
                        this.epochDay  = epochDay
                        this.timestamp = timestamp
                        this.filePath  = permanentFile.absolutePath
                        this.type      = Photo.TYPE_AUDIO
                    }
                    copyToRealm(photo, UpdatePolicy.ALL)

                    analysis.photoId   = photoId
                    analysis.transcript = liveTranscript
                    analysis.sessionSummaryText = summaryText
                    copyToRealm(analysis, UpdatePolicy.ALL)
                }
                realm.close()

                // Persist to Room (primary database)
                val db = DatabaseManager.getDatabase(requireContext())
                db.photoDao().insertPhoto(
                    PhotoEntity(
                        id        = photoId,
                        epochDay  = epochDay,
                        timestamp = timestamp,
                        filePath  = permanentFile.absolutePath,
                        type      = Photo.TYPE_AUDIO
                    )
                )
                db.audioAnalysisDao().insertAudioAnalysis(
                    AudioAnalysisEntity(
                        id                   = analysis.id,
                        photoId              = photoId,
                        f0Mean               = analysis.f0Mean,
                        f0Min                = analysis.f0Min,
                        f0Max                = analysis.f0Max,
                        f1Mean               = analysis.f1Mean,
                        f2Mean               = analysis.f2Mean,
                        f3Mean               = analysis.f3Mean,
                        f4Mean               = analysis.f4Mean,
                        f0StdDev             = analysis.f0StdDev,
                        durationSeconds      = analysis.durationSeconds,
                        analysisTimestamp    = analysis.analysisTimestamp,
                        pitchConfidenceMean  = analysis.pitchConfidenceMean,
                        voicedRatio          = analysis.voicedRatio,
                        intensityMeanDb      = analysis.intensityMeanDb,
                        intensityMaxDb       = analysis.intensityMaxDb,
                        pitchRangeHz         = analysis.pitchRangeHz,
                        pitchStabilityScore  = analysis.pitchStabilityScore,
                        intonationMovement   = analysis.intonationMovement,
                        sessionSummaryText   = summaryText
                    )
                )

                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        Snackbar.make(requireView(), R.string.audio_analysis_complete, Snackbar.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        Snackbar.make(requireView(), R.string.error_saving_photo, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    
    override fun onDestroyView() {
        stopTimer()
        SpeechTranscriptionManager.stop()
        audioRecorder.release()
        super.onDestroyView()
    }
}
