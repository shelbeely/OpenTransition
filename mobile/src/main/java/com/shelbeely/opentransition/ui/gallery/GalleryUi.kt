/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.gallery

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import androidx.appcompat.widget.PopupMenu
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.data.Photo
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme
import com.shelbeely.opentransition.ui.widget.AdapterSpanSizeLookup
import com.shelbeely.opentransition.util.applySystemBarInsets
import com.shelbeely.opentransition.util.plusAssign
import com.shelbeely.opentransition.util.settings.SettingsManager
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.lang.ref.WeakReference

sealed class GalleryUiEvent {
    object Back : GalleryUiEvent()
    data class ImageClick(val photoId: String) : GalleryUiEvent()
    data class AddPhotoCamera(@Photo.Type val type: Int) : GalleryUiEvent()
    data class AddPhotoGallery(@Photo.Type val type: Int) : GalleryUiEvent()
    object AddAudioRecording : GalleryUiEvent()
    object StartMultiSelect : GalleryUiEvent()
    data class SelectionUpdated(val selectedIds: ArrayList<String>) : GalleryUiEvent()
    object EndActionMode : GalleryUiEvent()
    data class Share(val selectedIds: ArrayList<String>) : GalleryUiEvent()
    data class Delete(val selectedIds: ArrayList<String>) : GalleryUiEvent()
    /** Long-tap on an audio gallery item when not in selection mode → open session detail. */
    data class AudioSessionDetail(val photoId: String) : GalleryUiEvent()
    /** Tap on the "progress" button in the audio gallery toolbar → open voice progress screen. */
    object ViewVoiceProgress : GalleryUiEvent()
}

sealed class GalleryUiState {
    data class Loaded(val type: Int, val initialDay: Long) : GalleryUiState()
    data class Selection(
        val type: Int,
        val initialDay: Long,
        val selectedIds: ArrayList<String>
    ) : GalleryUiState()

    companion object {
        fun getInitialDay(state: GalleryUiState) = when (state) {
            is GalleryUiState.Loaded -> state.initialDay
            is GalleryUiState.Selection -> state.initialDay
        }

        @Photo.Type
        fun getType(state: GalleryUiState) = when (state) {
            is Loaded -> state.type
            is Selection -> state.type
        }
    }
}

class GalleryView(
    context: Context, attributeSet: AttributeSet
) : FrameLayout(context, attributeSet) {

    private val eventRelay: PublishRelay<GalleryUiEvent> = PublishRelay.create()
    private val viewDisposables: CompositeDisposable = CompositeDisposable()
    val events: Observable<GalleryUiEvent> = eventRelay

    @Photo.Type
    private var type = Photo.TYPE_FACE
    private var currentState: GalleryUiState? = null
    private var isEmpty: Boolean = false

    private val layoutManager = GridLayoutManager(context, GRID_SPAN)

    private val recyclerView: RecyclerView by lazy {
        RecyclerView(context).apply {
            layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
            layoutManager = this@GalleryView.layoutManager
            this@GalleryView.layoutManager.spanSizeLookup = AdapterSpanSizeLookup(this)
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
                (recyclerView.adapter as? GalleryAdapter)?.refreshTitleItems()
            }

        renderCompose()
    }

    override fun onDetachedFromWindow() {
        viewDisposables.clear()
        super.onDetachedFromWindow()
    }

    fun display(state: GalleryUiState) {
        currentState = state
        type = GalleryUiState.getType(state)

        val shouldShowActionMode = state is GalleryUiState.Selection
        if (shouldShowActionMode && !actionModeHandler.isActive()) {
            startActionMode(actionModeHandler)
        } else if (!shouldShowActionMode && actionModeHandler.isActive()) {
            actionModeHandler.finish()
        }

        val selectedIds: ArrayList<String> = when (state) {
            is GalleryUiState.Selection -> state.selectedIds
            else -> ArrayList()
        }

        if (shouldShowActionMode) {
            actionModeHandler.setTitle((state as GalleryUiState.Selection).selectedIds.size.toString())
        }

        if (recyclerView.adapter == null) {
            val adapter = GalleryAdapter(
                type,
                eventRelay,
                state is GalleryUiState.Selection,
                selectedIds,
                postInitialLoad = { adapter ->
                    val scrollTo = adapter.getPositionOfDay(GalleryUiState.getInitialDay(state))
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
            recyclerView.adapter = adapter
        } else {
            val adapter: GalleryAdapter = recyclerView.adapter!! as GalleryAdapter
            adapter.selectionMode = state is GalleryUiState.Selection
            if (adapter.selectionMode) {
                adapter.updateSelectedIds(selectedIds)
            }
        }

        renderCompose()
    }

    private fun showPhotoSourceMenu() {
        if (type == Photo.TYPE_AUDIO) {
            eventRelay.accept(GalleryUiEvent.AddAudioRecording)
            return
        }

        val popup = PopupMenu(context, composeView)
        popup.menuInflater.inflate(R.menu.popup_media_source, popup.menu)
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.media_source_camera ->
                    eventRelay.accept(GalleryUiEvent.AddPhotoCamera(type = type))
                R.id.media_source_gallery ->
                    eventRelay.accept(GalleryUiEvent.AddPhotoGallery(type = type))
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
        popup.show()
    }

    private val actionModeHandler = object : ActionMode.Callback {
        private var modeRef = WeakReference<ActionMode>(null)
        private var titleText = ""

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val adapter: GalleryAdapter = recyclerView.adapter as GalleryAdapter? ?: return false
            val event: GalleryUiEvent = when (item.itemId) {
                R.id.gallery_menu_selection_share -> GalleryUiEvent.Share(adapter.getSelectedIds())
                R.id.gallery_menu_selection_delete -> GalleryUiEvent.Delete(adapter.getSelectedIds())
                else -> throw IllegalArgumentException("Unhandled item")
            }
            eventRelay.accept(event)
            return true
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            modeRef = WeakReference(mode)
            mode.menuInflater.inflate(R.menu.gallery_selection, menu)
            mode.title = titleText
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.title = titleText
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            modeRef = WeakReference<ActionMode>(null)
            eventRelay.accept(GalleryUiEvent.EndActionMode)
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

    private fun renderCompose() {
        val state = currentState ?: return
        composeView.setContent {
            OpenTransitionTheme(colorVariant = SettingsManager.getResolvedComposeColorVariant()) {
                GalleryScreen(
                    state = state,
                    isEmpty = isEmpty,
                    onBack = { eventRelay.accept(GalleryUiEvent.Back) },
                    onAddPhoto = { showPhotoSourceMenu() },
                    onViewProgress = if (GalleryUiState.getType(state) == Photo.TYPE_AUDIO) {
                        { eventRelay.accept(GalleryUiEvent.ViewVoiceProgress) }
                    } else null,
                    recyclerView = recyclerView,
                )
            }
        }
    }

    companion object {
        const val GRID_SPAN = 3
    }
}
