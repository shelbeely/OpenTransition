/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.milestones

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme
import com.shelbeely.opentransition.util.applySystemBarInsets
import com.shelbeely.opentransition.util.plusAssign
import com.shelbeely.opentransition.util.settings.SettingsManager
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable

sealed class MilestonesUiEvent {
    object Back : MilestonesUiEvent()
    data class AddMilestone(val day: Long) : MilestonesUiEvent()
    data class EditMilestone(val id: String) : MilestonesUiEvent()
}

sealed class MilestonesUiState {
    data class Loaded(val initialDay: Long) : MilestonesUiState()

    companion object {
        fun getInitialDay(state: MilestonesUiState): Long = when (state) {
            is Loaded -> state.initialDay
        }
    }
}

class MilestonesView(
    context: Context, attributeSet: AttributeSet
) : FrameLayout(context, attributeSet) {

    private val eventRelay: PublishRelay<MilestonesUiEvent> = PublishRelay.create()
    private val viewDisposables: CompositeDisposable = CompositeDisposable()
    val events: Observable<MilestonesUiEvent> = eventRelay

    private var currentState: MilestonesUiState = MilestonesUiState.Loaded(0L)
    private var isEmpty: Boolean = false

    private val layoutManager = LinearLayoutManager(context)

    private val recyclerView: RecyclerView by lazy {
        RecyclerView(context).apply {
            layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
            this.layoutManager = this@MilestonesView.layoutManager
        }
    }

    private val composeView: ComposeView by lazy {
        ComposeView(context).also { cv ->
            cv.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            addView(cv, LayoutParams(MATCH_PARENT, MATCH_PARENT))
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        applySystemBarInsets(left = false, top = true, right = false, bottom = true)

        viewDisposables += SettingsManager.themeUpdated
            .subscribe {
                (recyclerView.adapter as? MilestonesAdapter)?.refreshComposeItems()
            }

        renderCompose()
    }

    override fun onDetachedFromWindow() {
        viewDisposables.clear()
        super.onDetachedFromWindow()
    }

    fun display(state: MilestonesUiState) {
        currentState = state
        when (state) {
            is MilestonesUiState.Loaded -> {
                if (recyclerView.adapter == null) {
                    recyclerView.adapter = MilestonesAdapter(
                        eventRelay,
                        postInitialLoad = { adapter ->
                            val scrollTo = adapter.getPositionOfDay(state.initialDay)
                            if (scrollTo != -1) {
                                Handler(Looper.getMainLooper()).post {
                                    layoutManager.scrollToPositionWithOffset(scrollTo, 0)
                                }
                            }
                        },
                        postLoad = { adapter ->
                            isEmpty = adapter.itemCount == 0
                            renderCompose()
                        }
                    )
                }
            }
        }
        renderCompose()
    }

    private fun renderCompose() {
        composeView.setContent {
            OpenTransitionTheme(colorVariant = SettingsManager.getResolvedComposeColorVariant()) {
                MilestonesScreen(
                    state = currentState,
                    isEmpty = isEmpty,
                    onBack = { eventRelay.accept(MilestonesUiEvent.Back) },
                    onAdd = {
                        eventRelay.accept(
                            MilestonesUiEvent.AddMilestone(
                                MilestonesUiState.getInitialDay(currentState)
                            )
                        )
                    },
                    recyclerView = recyclerView,
                )
            }
        }
    }
}
