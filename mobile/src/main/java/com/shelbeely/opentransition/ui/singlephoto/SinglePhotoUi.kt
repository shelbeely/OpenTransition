/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.singlephoto

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

sealed class SinglePhotoUiEvent {
    object Back : SinglePhotoUiEvent()
    data class Edit(val photoId: String) : SinglePhotoUiEvent()
    data class Share(val photoId: String) : SinglePhotoUiEvent()
    data class Delete(val photoId: String) : SinglePhotoUiEvent()
}

sealed class SinglePhotoUiState {
    data class Loaded(
        val photoPath: String, val details: String, val photoId: String
    ) : SinglePhotoUiState()
}

class SinglePhotoView(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet) {

    private val eventRelay: PublishRelay<SinglePhotoUiEvent> = PublishRelay.create()
    val events: Observable<SinglePhotoUiEvent> = eventRelay

    private var currentState: SinglePhotoUiState = SinglePhotoUiState.Loaded("", "", "")
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

    fun display(state: SinglePhotoUiState) {
        currentState = state
        renderCompose()
    }

    private fun renderCompose() {
        composeView.setContent {
            OpenTransitionTheme(colorVariant = SettingsManager.getResolvedComposeColorVariant()) {
                SinglePhotoScreen(
                    state = currentState,
                    onBack = { eventRelay.accept(SinglePhotoUiEvent.Back) },
                    onEdit = { id -> eventRelay.accept(SinglePhotoUiEvent.Edit(id)) },
                    onShare = { id -> eventRelay.accept(SinglePhotoUiEvent.Share(id)) },
                    onDelete = { id -> eventRelay.accept(SinglePhotoUiEvent.Delete(id)) }
                )
            }
        }
    }
}
