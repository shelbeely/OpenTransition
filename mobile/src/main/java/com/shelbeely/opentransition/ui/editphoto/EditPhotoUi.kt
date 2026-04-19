/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.editphoto

import android.content.Context
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

sealed class EditPhotoUiEvent {
    object Back : EditPhotoUiEvent()
    object ChangeDate : EditPhotoUiEvent()
    object ChangeType : EditPhotoUiEvent()
    object Update : EditPhotoUiEvent()
}

sealed class EditPhotoUiState {
    object Loading : EditPhotoUiState()
    data class Loaded(
        val photoPath: String, val date: String, val type: String
    ) : EditPhotoUiState()
}

class EditPhotoView(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet) {

    private val eventRelay: PublishRelay<EditPhotoUiEvent> = PublishRelay.create()
    val events: Observable<EditPhotoUiEvent> = eventRelay

    private var currentState: EditPhotoUiState = EditPhotoUiState.Loading
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

    fun display(state: EditPhotoUiState) {
        currentState = state
        renderCompose()
    }

    private fun renderCompose() {
        composeView.setContent {
            OpenTransitionTheme(colorVariant = SettingsManager.getResolvedComposeColorVariant()) {
                EditPhotoScreen(
                    state = currentState,
                    onBack = { eventRelay.accept(EditPhotoUiEvent.Back) },
                    onChangeDate = { eventRelay.accept(EditPhotoUiEvent.ChangeDate) },
                    onChangeType = { eventRelay.accept(EditPhotoUiEvent.ChangeType) },
                    onUpdate = { eventRelay.accept(EditPhotoUiEvent.Update) }
                )
            }
        }
    }
}
