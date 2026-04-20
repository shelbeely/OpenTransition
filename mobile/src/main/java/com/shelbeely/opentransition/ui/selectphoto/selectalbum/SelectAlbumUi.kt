/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.selectphoto.selectalbum

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme
import com.shelbeely.opentransition.util.applySystemBarInsets
import com.shelbeely.opentransition.util.isNotDisposed
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable

sealed class SelectAlbumUiEvent {
    object Back : SelectAlbumUiEvent()
    data class SelectAlbum(val bucketId: String) : SelectAlbumUiEvent()
}

sealed class SelectAlbumUiState {
    object Loaded : SelectAlbumUiState()
}

class AlbumView(
    context: Context, attributeSet: AttributeSet
) : FrameLayout(context, attributeSet) {

    private val eventRelay: PublishRelay<SelectAlbumUiEvent> = PublishRelay.create()
    val events: Observable<SelectAlbumUiEvent> = eventRelay

    private var currentState: SelectAlbumUiState = SelectAlbumUiState.Loaded
    private var albumClickDisposable: Disposable = Disposable.disposed()

    private val recyclerView: RecyclerView by lazy {
        RecyclerView(context).apply {
            layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
            layoutManager = LinearLayoutManager(context)
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
        renderCompose()
    }

    fun display(state: SelectAlbumUiState) {
        currentState = state
        when (state) {
            is SelectAlbumUiState.Loaded -> {
                val selectAlbumAdapter: SelectAlbumAdapter

                if (recyclerView.adapter == null) {
                    selectAlbumAdapter = SelectAlbumAdapter()
                    recyclerView.adapter = selectAlbumAdapter
                } else {
                    selectAlbumAdapter = recyclerView.adapter as SelectAlbumAdapter
                }

                selectAlbumAdapter.fetchData(context)
                albumClickDisposable = selectAlbumAdapter.itemClick
                    .map { SelectAlbumUiEvent.SelectAlbum(it) }
                    .subscribe(eventRelay)
            }
        }
        renderCompose()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (albumClickDisposable.isNotDisposed()) {
            albumClickDisposable.dispose()
        }
    }

    private fun renderCompose() {
        composeView.setContent {
            OpenTransitionTheme {
                SelectAlbumScreen(
                    state = currentState,
                    onBack = { eventRelay.accept(SelectAlbumUiEvent.Back) },
                    recyclerView = recyclerView,
                )
            }
        }
    }
}
