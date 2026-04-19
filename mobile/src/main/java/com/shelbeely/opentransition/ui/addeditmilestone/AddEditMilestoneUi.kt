/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.addeditmilestone

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

sealed class AddEditMilestoneUiEvent {
    object Back : AddEditMilestoneUiEvent()
    object Delete : AddEditMilestoneUiEvent()
    data class ChangeDate(val day: Long) : AddEditMilestoneUiEvent()
    data class TitleUpdated(val newTitle: String) : AddEditMilestoneUiEvent()
    data class DescriptionUpdated(val newDescription: String) : AddEditMilestoneUiEvent()
    data class Save(
        val day: Long, val title: String, val description: String
    ) : AddEditMilestoneUiEvent()
}

sealed class AddEditMilestoneUiState {
    object Loading : AddEditMilestoneUiState()
    data class Display(
        val day: Long, val title: String, val description: String, val isAdd: Boolean
    ) : AddEditMilestoneUiState()
}

class AddEditMilestoneView(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet) {

    private val eventRelay: PublishRelay<AddEditMilestoneUiEvent> = PublishRelay.create()
    val events: Observable<AddEditMilestoneUiEvent> = eventRelay

    private var currentState: AddEditMilestoneUiState = AddEditMilestoneUiState.Loading

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

    fun display(state: AddEditMilestoneUiState) {
        currentState = state
        renderCompose()
    }

    private fun renderCompose() {
        val state = currentState
        composeView.setContent {
            OpenTransitionTheme(colorVariant = SettingsManager.getResolvedComposeColorVariant()) {
                AddEditMilestoneScreen(
                    state = state,
                    onBack = { eventRelay.accept(AddEditMilestoneUiEvent.Back) },
                    onDelete = { eventRelay.accept(AddEditMilestoneUiEvent.Delete) },
                    onTitleChanged = { eventRelay.accept(AddEditMilestoneUiEvent.TitleUpdated(it)) },
                    onDescriptionChanged = { eventRelay.accept(AddEditMilestoneUiEvent.DescriptionUpdated(it)) },
                    onChangeDate = { day -> eventRelay.accept(AddEditMilestoneUiEvent.ChangeDate(day)) },
                    onSave = { day, title, desc ->
                        eventRelay.accept(AddEditMilestoneUiEvent.Save(day, title, desc))
                    }
                )
            }
        }
    }
}
