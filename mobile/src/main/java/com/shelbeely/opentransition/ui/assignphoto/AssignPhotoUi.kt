/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.assignphoto

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme
import com.shelbeely.opentransition.util.applySystemBarInsets
import com.shelbeely.opentransition.util.settings.SettingsManager
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import java.time.LocalDate

sealed class AssignPhotoUiEvent {
    object Back : AssignPhotoUiEvent()
    data class ChangeDate(val index: Int) : AssignPhotoUiEvent()
    data class UsePhotoDate(val index: Int, val photoDate: LocalDate) : AssignPhotoUiEvent()
    data class ChangeType(val index: Int) : AssignPhotoUiEvent()
    data class Save(val index: Int) : AssignPhotoUiEvent()
    data class Skip(val index: Int, val count: Int) : AssignPhotoUiEvent()
}

sealed class AssignPhotoUiState {
    object Loading : AssignPhotoUiState()

    data class Loaded(
        val index: Int, val count: Int, val photoUri: Uri, val title: String,
        val date: String, val photoDate: LocalDate?, val type: String,
        val showSkip: Boolean
    ) : AssignPhotoUiState()
}

class AssignPhotoView(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet) {

    private val eventRelay: PublishRelay<AssignPhotoUiEvent> = PublishRelay.create()
    val events: Observable<AssignPhotoUiEvent> = eventRelay

    private var currentState: AssignPhotoUiState = AssignPhotoUiState.Loading

    private val composeView: ComposeView by lazy {
        ComposeView(context).also { cv ->
            cv.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            addView(cv, LayoutParams(MATCH_PARENT, MATCH_PARENT))
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        applySystemBarInsets(left = false, top = true, right = false, bottom = true)
        renderCompose()
    }

    fun display(state: AssignPhotoUiState) {
        currentState = state
        renderCompose()
    }

    private fun renderCompose() {
        composeView.setContent {
            OpenTransitionTheme(colorVariant = SettingsManager.getResolvedComposeColorVariant()) {
                AssignPhotoScreen(
                    state = currentState,
                    onBack = { eventRelay.accept(AssignPhotoUiEvent.Back) },
                    onChangeDate = { idx -> eventRelay.accept(AssignPhotoUiEvent.ChangeDate(idx)) },
                    onUsePhotoDate = { idx, date -> eventRelay.accept(AssignPhotoUiEvent.UsePhotoDate(idx, date)) },
                    onChangeType = { idx -> eventRelay.accept(AssignPhotoUiEvent.ChangeType(idx)) },
                    onSave = { idx -> eventRelay.accept(AssignPhotoUiEvent.Save(idx)) },
                    onSkip = { idx, count -> eventRelay.accept(AssignPhotoUiEvent.Skip(idx, count)) }
                )
            }
        }
    }
}
