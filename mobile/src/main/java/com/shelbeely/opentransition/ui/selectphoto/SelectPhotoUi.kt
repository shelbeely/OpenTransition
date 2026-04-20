/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.selectphoto

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme
import com.shelbeely.opentransition.util.applySystemBarInsets
import com.shelbeely.opentransition.util.isNotDisposed
import com.shelbeely.opentransition.util.settings.PrefUtil
import com.shelbeely.opentransition.util.settings.SettingsManager
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.lang.ref.WeakReference

sealed class SelectPhotoUiEvent {
    object Back : SelectPhotoUiEvent()
    object TakePhoto : SelectPhotoUiEvent()
    data class PhotoSelected(val uri: Uri) : SelectPhotoUiEvent()
    object ViewAlbums : SelectPhotoUiEvent()
    object ExternalGalleries : SelectPhotoUiEvent()
    data class SelectionUpdate(var uris: ArrayList<Uri>) : SelectPhotoUiEvent()
    object EndMultiSelect : SelectPhotoUiEvent()
    data class SaveMultiple(var uris: ArrayList<Uri>) : SelectPhotoUiEvent()
}

sealed class SelectPhotoUiState {
    object Loaded : SelectPhotoUiState()
    data class Selection(val selectedUris: ArrayList<Uri>) : SelectPhotoUiState()
}

class SelectPhotoView(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet) {

    private val eventRelay: PublishRelay<SelectPhotoUiEvent> = PublishRelay.create()
    val events: Observable<SelectPhotoUiEvent> = eventRelay

    private var adapterDisposable: Disposable = Disposable.disposed()
    private var currentState: SelectPhotoUiState? = null
    private var adapter: SelectPhotoAdapter? = null

    private val gridLayoutManager = GridLayoutManager(context, GRID_SPAN)

    val recyclerView: RecyclerView by lazy {
        RecyclerView(context).apply {
            layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
            layoutManager = gridLayoutManager
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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (adapterDisposable.isNotDisposed()) {
            adapterDisposable.dispose()
        }
    }

    fun display(state: SelectPhotoUiState) {
        currentState = state
        val shouldShowActionMode = state is SelectPhotoUiState.Selection

        if (shouldShowActionMode && !actionModeHandler.isActive()) {
            startActionMode(actionModeHandler)
        } else if (!shouldShowActionMode && actionModeHandler.isActive()) {
            actionModeHandler.finish()
        }

        if (shouldShowActionMode) {
            actionModeHandler.setTitle(
                (state as SelectPhotoUiState.Selection).selectedUris.size.toString()
            )
        }

        if (adapter == null) {
            adapter = SelectPhotoAdapter(context)
            recyclerView.adapter = adapter

            val firstVisibleUriString = PrefUtil.getSelectPhotoFirstVisible()
            if (firstVisibleUriString.isNotBlank()) {
                val position = adapter!!.getItemPosition(Uri.parse(firstVisibleUriString))
                if (position != RecyclerView.NO_POSITION) {
                    recyclerView.scrollToPosition(position)
                }
            }
        }

        adapter?.selectionMode = shouldShowActionMode
        if (shouldShowActionMode) {
            val selectedUris: ArrayList<Uri> = when (state) {
                is SelectPhotoUiState.Selection -> state.selectedUris
                else -> ArrayList()
            }
            adapter?.updateSelectedUris(selectedUris)
        }

        if (adapterDisposable.isDisposed) {
            adapterDisposable = adapter!!.events
                .doOnNext { event ->
                    if (event !is SelectPhotoUiEvent.PhotoSelected) return@doOnNext
                    val position = gridLayoutManager.findFirstVisibleItemPosition()
                    if (position == RecyclerView.NO_POSITION) return@doOnNext
                    val uriString: String = adapter?.getUri(position)?.toString() ?: ""
                    PrefUtil.setSelectPhotoFirstVisible(uriString)
                }
                .subscribe(eventRelay)
        }

        renderCompose()
    }

    private fun renderCompose() {
        val state = currentState ?: return
        composeView.setContent {
            OpenTransitionTheme(colorVariant = SettingsManager.getResolvedComposeColorVariant()) {
                SelectPhotoScreen(
                    state = state,
                    recyclerView = recyclerView,
                    onBack = { eventRelay.accept(SelectPhotoUiEvent.Back) },
                    onViewAlbums = { eventRelay.accept(SelectPhotoUiEvent.ViewAlbums) },
                    onExternalGalleries = { eventRelay.accept(SelectPhotoUiEvent.ExternalGalleries) },
                )
            }
        }
    }

    private val actionModeHandler = object : ActionMode.Callback {
        private var modeRef = WeakReference<ActionMode>(null)
        private var titleText = ""

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val adapter: SelectPhotoAdapter = (recyclerView.adapter as SelectPhotoAdapter?)
                ?: return false

            val event: SelectPhotoUiEvent = when (item.itemId) {
                R.id.select_photo_selection_save ->
                    SelectPhotoUiEvent.SaveMultiple(adapter.getSelectedUris())
                else -> throw IllegalArgumentException("Unhandled item")
            }

            eventRelay.accept(event)
            finish()
            return true
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            modeRef = WeakReference(mode)
            mode.menuInflater.inflate(R.menu.select_photo_selection, menu)
            mode.title = titleText
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.title = titleText
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            modeRef = WeakReference<ActionMode>(null)
            eventRelay.accept(SelectPhotoUiEvent.EndMultiSelect)
        }

        fun finish() {
            modeRef.get()?.finish()
        }

        fun isActive(): Boolean = modeRef.get() != null

        fun setTitle(newTitleText: String) {
            titleText = newTitleText
            modeRef.get()?.title = titleText
        }
    }

    companion object {
        const val GRID_SPAN = 3
    }
}
