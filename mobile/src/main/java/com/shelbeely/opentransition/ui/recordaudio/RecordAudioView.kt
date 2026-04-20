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
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme
import com.shelbeely.opentransition.util.applySystemBarInsets
import com.shelbeely.opentransition.util.settings.SettingsManager
import java.io.File
import java.time.LocalDate

class RecordAudioView(context: Context, attributeSet: AttributeSet?) :
    FrameLayout(context, attributeSet) {

    private val timerTextState = mutableStateOf("00:00")
    private val isRecordingState = mutableStateOf(false)
    private val isSaveEnabledState = mutableStateOf(false)
    private val isAnalyzingState = mutableStateOf(false)
    private val selectedDateState = mutableStateOf(LocalDate.now())
    private val audioFileState = mutableStateOf<File?>(null)

    private var onRecordClick: (() -> Unit)? = null
    private var onSaveClick: (() -> Unit)? = null
    private var onCancelClick: (() -> Unit)? = null
    private var onDateClick: (() -> Unit)? = null

    private val composeView: ComposeView by lazy {
        ComposeView(context).also { cv ->
            cv.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            addView(cv, LayoutParams(MATCH_PARENT, MATCH_PARENT))
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        applySystemBarInsets(left = false, top = true, right = false, bottom = true)
        composeView.setContent {
            OpenTransitionTheme(colorVariant = SettingsManager.getResolvedComposeColorVariant()) {
                RecordAudioScreen(
                    timerText = timerTextState.value,
                    isRecording = isRecordingState.value,
                    isSaveEnabled = isSaveEnabledState.value,
                    isAnalyzing = isAnalyzingState.value,
                    selectedDate = selectedDateState.value,
                    audioFile = audioFileState.value,
                    onRecordClick = { onRecordClick?.invoke() },
                    onSaveClick = { onSaveClick?.invoke() },
                    onCancelClick = { onCancelClick?.invoke() },
                    onDateClick = { onDateClick?.invoke() },
                )
            }
        }
    }

    fun setOnRecordClickListener(listener: () -> Unit) {
        onRecordClick = listener
    }

    fun setOnSaveClickListener(listener: () -> Unit) {
        onSaveClick = listener
    }

    fun setOnCancelClickListener(listener: () -> Unit) {
        onCancelClick = listener
    }

    fun setOnDateClickListener(listener: () -> Unit) {
        onDateClick = listener
    }

    fun setRecordingState(recording: Boolean) {
        isRecordingState.value = recording
    }

    fun updateTimer(timeString: String) {
        timerTextState.value = timeString
    }

    fun enableSaveButton(enabled: Boolean) {
        isSaveEnabledState.value = enabled
    }

    fun showAnalyzing(show: Boolean) {
        isAnalyzingState.value = show
    }

    fun getSelectedDate(): LocalDate = selectedDateState.value

    fun setSelectedDate(newDate: LocalDate) {
        selectedDateState.value = newDate
    }

    fun setAudioFile(file: File) {
        audioFileState.value = file
    }
}
