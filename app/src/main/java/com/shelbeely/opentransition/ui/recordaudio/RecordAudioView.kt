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

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.util.applySystemBarInsets
import com.shelbeely.opentransition.util.setVisibleOrGone
import com.shelbeely.opentransition.util.toFullDateString
import kotterknife.bindView
import java.time.LocalDate

class RecordAudioView(context: Context, attributeSet: AttributeSet) :
    ConstraintLayout(context, attributeSet) {
    
    private val toolbar: Toolbar by bindView(R.id.record_audio_toolbar)
    private val title: TextView by bindView(R.id.record_audio_title)
    private val recordButton: FloatingActionButton by bindView(R.id.record_audio_button)
    private val timer: TextView by bindView(R.id.record_audio_timer)
    private val status: TextView by bindView(R.id.record_audio_status)
    private val dateLabel: View by bindView(R.id.record_audio_date_label)
    private val date: Button by bindView(R.id.record_audio_date)
    private val saveButton: Button by bindView(R.id.record_audio_save)
    private val cancelButton: Button by bindView(R.id.record_audio_cancel)
    
    private var selectedDate: LocalDate = LocalDate.now()
    private var isRecording = false
    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        
        // Apply window insets for system bars
        applySystemBarInsets(left = false, top = true, right = false, bottom = true)
        
        toolbar.setNavigationOnClickListener {
            // Handle back navigation
        }
        
        dateLabel.setOnClickListener { date.performClick() }
        
        // Initialize date
        date.text = selectedDate.toFullDateString(context)
    }
    
    fun setOnRecordClickListener(listener: () -> Unit) {
        recordButton.setOnClickListener { listener() }
    }
    
    fun setOnSaveClickListener(listener: () -> Unit) {
        saveButton.setOnClickListener { listener() }
    }
    
    fun setOnCancelClickListener(listener: () -> Unit) {
        cancelButton.setOnClickListener { listener() }
    }
    
    fun setOnDateClickListener(listener: () -> Unit) {
        date.setOnClickListener { listener() }
    }
    
    fun setRecordingState(recording: Boolean) {
        isRecording = recording
        if (recording) {
            recordButton.setImageResource(android.R.drawable.ic_media_pause)
            title.text = context.getString(R.string.recording)
            status.text = context.getString(R.string.recording)
            status.setVisibleOrGone(true)
        } else {
            recordButton.setImageResource(android.R.drawable.ic_btn_speak_now)
            title.text = context.getString(R.string.record_audio)
            status.setVisibleOrGone(false)
        }
    }
    
    fun updateTimer(timeString: String) {
        timer.text = timeString
    }
    
    fun enableSaveButton(enabled: Boolean) {
        saveButton.isEnabled = enabled
    }
    
    fun showAnalyzing(show: Boolean) {
        if (show) {
            status.text = context.getString(R.string.analyzing_audio)
            status.setVisibleOrGone(true)
            saveButton.isEnabled = false
            recordButton.isEnabled = false
        } else {
            status.setVisibleOrGone(false)
            saveButton.isEnabled = true
            recordButton.isEnabled = true
        }
    }
    
    fun getSelectedDate(): LocalDate = selectedDate
    
    fun setSelectedDate(newDate: LocalDate) {
        selectedDate = newDate
        date.text = selectedDate.toFullDateString(context)
    }
}
