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

import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.data.AudioAnalysis
import com.shelbeely.opentransition.data.Photo
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme
import com.shelbeely.opentransition.ui.widget.AdapterSpanSizeLookup
import com.shelbeely.opentransition.ui.widget.SquareConstraintLayout
import com.shelbeely.opentransition.util.AudioPlayerManager
import com.shelbeely.opentransition.util.RxSchedulers
import com.shelbeely.opentransition.util.openDefault
import com.shelbeely.opentransition.util.settings.SettingsManager
import com.shelbeely.opentransition.util.toFullDateString
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.Sort.DESCENDING
import kotlinx.coroutines.rx3.asObservable
import java.io.File
import java.lang.ref.WeakReference
import java.time.LocalDate

class GalleryAdapter(
    @Photo.Type private val type: Int, eventRelay: PublishRelay<GalleryUiEvent>,
    initialSelectionMode: Boolean, private val selectedIds: ArrayList<String>,
    private val postInitialLoad: (adapter: GalleryAdapter) -> Unit,
    private val postLoad: (adapter: GalleryAdapter) -> Unit
) : RecyclerView.Adapter<GalleryAdapter.BaseViewHolder>(), AdapterSpanSizeLookup.Interface {
    private val photosFlow = Realm.openDefault()
        .query(Photo::class, "${Photo.FIELD_TYPE} == $type")
        .sort(Photo.FIELD_EPOCH_DAY, DESCENDING)
        .find()
        .asFlow()
        .asObservable()
        .observeOn(RxSchedulers.main())
        .subscribe {
            result = it.list
            generateItems()
            if (initialLoad) {
                initialLoad = false
                postInitialLoad.invoke(this@GalleryAdapter)
            }

            postLoad.invoke(this@GalleryAdapter)
        }

    private var result = emptyList<Photo>()

    private val eventRelayRef = WeakReference(eventRelay)

    private var items = ArrayList<GalleryAdapterItem>()

    var selectionMode: Boolean = initialSelectionMode
        set(value) {
            val didChange = field != value

            field = value

            if (didChange) {
                generateItems(true)
            }
        }

    private var initialLoad = true

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (holder) {
            is TitleViewHolder -> holder.bind(items[position])
            is PhotoViewHolder -> holder.bind(items[position], selectionMode)
            is AudioViewHolder -> holder.bind(items[position], selectionMode)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            TYPE_TITLE -> {
                val composeView = ComposeView(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    // Pool-aware disposal strategy for RecyclerView items
                    setViewCompositionStrategy(
                        ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
                    )
                }
                TitleViewHolder(composeView)
            }
            TYPE_PHOTO -> {
                val context = parent.context
                val density = context.resources.displayMetrics.density

                if (type == Photo.TYPE_AUDIO) {
                    val m = (8 * density).toInt()
                    val mv = (6 * density).toInt()
                    val composeView = ComposeView(context).apply {
                        layoutParams = RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply { setMargins(m, mv, m, mv) }
                        setViewCompositionStrategy(
                            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
                        )
                    }
                    AudioViewHolder(composeView, this)
                } else {
                    val m = (2 * density).toInt()
                    val composeView = ComposeView(context).apply {
                        layoutParams = ConstraintLayout.LayoutParams(0, 0).apply {
                            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                            topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                            bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                        }
                        setViewCompositionStrategy(
                            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
                        )
                    }
                    val itemView = SquareConstraintLayout(context).apply {
                        layoutParams = RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply { setMargins(m, m, m, m) }
                        orientation = 1 // horizontal → square based on width
                        addView(composeView)
                    }
                    PhotoViewHolder(itemView, composeView, this)
                }
            }
            else -> throw IllegalArgumentException("Unhandled item type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when {
        items[position].epochDay != null -> TYPE_TITLE
        items[position].photo != null -> TYPE_PHOTO
        else -> throw IllegalArgumentException("Unhandled item type")
    }

    override fun getSpanSize(position: Int): Int = when (getItemViewType(position)) {
        TYPE_TITLE -> GalleryView.GRID_SPAN
        TYPE_PHOTO -> if (type == Photo.TYPE_AUDIO) GalleryView.GRID_SPAN else 1
        else -> throw IllegalArgumentException("Unhandled item type")
    }

    fun getPositionOfDay(day: Long): Int {
        items.forEachIndexed { index, item ->
            if (item.epochDay != null && item.epochDay == day) {
                return index
            }
        }

        return -1
    }

    private fun generateItems(selectionModeChanged: Boolean = false) {
        val newItems = ArrayList<GalleryAdapterItem>()

        result.forEach { photo ->
            var indexOfTitle = newItems.indexOfFirst { item -> photo.epochDay == item.epochDay }

            if (indexOfTitle == -1) {
                newItems.add(GalleryAdapterItem(photo.epochDay))
                indexOfTitle = newItems.lastIndex
            }

            var indexToInsertAt: Int = newItems.size

            for (i in (indexOfTitle + 1) until newItems.size) {
                //Find the next title, and we will insert at that index
                if (newItems[i].epochDay != null) {
                    indexToInsertAt = i
                }
            }

            newItems.add(indexToInsertAt, GalleryAdapterItem(photo, selectedIds.contains(photo.id)))
        }

        if (selectionModeChanged) {
            items = newItems
            notifyDataSetChanged()
            return
        }

        val results = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val old = items[oldItemPosition]
                val new = newItems[newItemPosition]

                if (old.epochDay != null) {
                    return old.epochDay == new.epochDay
                }

                if (old.photo == null || !old.photo.isValid() || new.photo == null || !new.photo.isValid()) {
                    return false
                }

                return old.photo.id == new.photo.id
            }

            override fun getOldListSize(): Int = items.size

            override fun getNewListSize(): Int = newItems.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val old = items[oldItemPosition]
                val new = newItems[newItemPosition]

                if (old.epochDay != null) {
                    return old.epochDay == new.epochDay
                }

                val oldPhoto = old.photo!!
                val newPhoto = new.photo!!

                return old.selected == new.selected && oldPhoto == newPhoto
                        && oldPhoto.id == newPhoto.id && oldPhoto.epochDay == newPhoto.epochDay
                        && oldPhoto.timestamp == newPhoto.timestamp
                        && oldPhoto.filePath == newPhoto.filePath && oldPhoto.type == newPhoto.type
            }
        }, true)

        items = newItems
        results.dispatchUpdatesTo(this)
    }

    fun getSelectedIds() = selectedIds

    fun updateSelectedIds(newSelectedIds: ArrayList<String>) {
        selectedIds.clear()
        selectedIds.addAll(newSelectedIds)

        generateItems()
    }

    fun refreshTitleItems() {
        items.forEachIndexed { index, item ->
            if (item.epochDay != null) {
                notifyItemChanged(index)
            }
        }
    }

    class GalleryAdapterItem {
        val photo: Photo?
        val epochDay: Long?
        var selected = false

        constructor(photo: Photo, isSelected: Boolean) {
            this.photo = photo
            this.selected = isSelected
            epochDay = null
        }

        constructor(epochDay: Long) {
            photo = null
            this.epochDay = epochDay
        }
    }

    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class TitleViewHolder(private val composeView: ComposeView) : BaseViewHolder(composeView) {
        fun bind(item: GalleryAdapterItem) {
            val title = LocalDate.ofEpochDay(item.epochDay!!).toFullDateString(composeView.context)
            composeView.setContent {
                OpenTransitionTheme(
                    colorVariant = SettingsManager.getResolvedComposeColorVariant()
                ) {
                    GalleryDateTitleItem(title = title)
                }
            }
        }
    }

    class PhotoViewHolder(
        itemView: View,
        private val composeView: ComposeView,
        creatingAdapter: GalleryAdapter?
    ) : BaseViewHolder(itemView) {
        private val adapterRef = WeakReference(creatingAdapter)
        private var currentPhotoId = ""

        fun bind(item: GalleryAdapterItem, selectionMode: Boolean) {
            currentPhotoId = item.photo!!.id
            val photoId = currentPhotoId
            val filePath = item.photo.filePath
            val isSelected = item.selected

            composeView.setContent {
                OpenTransitionTheme(
                    colorVariant = SettingsManager.getResolvedComposeColorVariant()
                ) {
                    GalleryPhotoItem(
                        filePath = filePath,
                        isSelected = isSelected,
                        selectionMode = selectionMode,
                        onClick = {
                            val adapter = adapterRef.get() ?: return@GalleryPhotoItem
                            val event: GalleryUiEvent = when (adapter.selectionMode) {
                                true -> {
                                    if (adapter.selectedIds.contains(photoId)) {
                                        adapter.selectedIds.remove(photoId)
                                    } else {
                                        adapter.selectedIds.add(photoId)
                                    }
                                    val list = ArrayList<String>(adapter.selectedIds.size)
                                    list.addAll(adapter.selectedIds)
                                    GalleryUiEvent.SelectionUpdated(list)
                                }
                                false -> GalleryUiEvent.ImageClick(photoId)
                            }
                            adapter.eventRelayRef.get()?.accept(event)
                        },
                        onLongClick = {
                            val adapter = adapterRef.get() ?: return@GalleryPhotoItem
                            if (!adapter.selectionMode) {
                                adapter.eventRelayRef.get()
                                    ?.accept(GalleryUiEvent.SelectionUpdated(arrayListOf(photoId)))
                            } else {
                                if (adapter.selectedIds.contains(photoId)) {
                                    adapter.selectedIds.remove(photoId)
                                } else {
                                    adapter.selectedIds.add(photoId)
                                }
                                val list = ArrayList<String>(adapter.selectedIds.size)
                                list.addAll(adapter.selectedIds)
                                adapter.eventRelayRef.get()
                                    ?.accept(GalleryUiEvent.SelectionUpdated(list))
                            }
                        }
                    )
                }
            }
        }
    }

    class AudioViewHolder(
        composeView: ComposeView,
        creatingAdapter: GalleryAdapter?
    ) : BaseViewHolder(composeView) {
        private val composeView: ComposeView = composeView
        private val adapterRef = WeakReference(creatingAdapter)
        private val isPlayingState = mutableStateOf(false)
        private val pitchState = mutableStateOf("")
        private val formantsState = mutableStateOf("")
        private val transcriptState = mutableStateOf("")
        private var currentPhotoId = ""
        private var currentFilePath = ""
        private var analysisDisposable: Disposable? = null
        private var playbackDisposable: Disposable? = null

        fun bind(item: GalleryAdapterItem, selectionMode: Boolean) {
            analysisDisposable?.dispose()
            playbackDisposable?.dispose()

            currentPhotoId = item.photo!!.id
            currentFilePath = item.photo.filePath
            isPlayingState.value = AudioPlayerManager.isPlaying(currentPhotoId)

            val photoId = currentPhotoId
            val filePath = currentFilePath
            val isSelected = item.selected
            val dateText = LocalDate.ofEpochDay(item.photo.epochDay)
                .toFullDateString(composeView.context)

            // Show placeholder while analysis loads
            pitchState.value = composeView.context.getString(R.string.pitch_format, "…")
            formantsState.value = composeView.context.getString(R.string.formants_format, "…", "…")
            transcriptState.value = ""

            // Fetch analysis data off the main thread
            analysisDisposable = Single.fromCallable {
                val realm = Realm.openDefault()
                val analysis = realm.query(AudioAnalysis::class, "photoId == '$photoId'")
                    .first().find()
                val pitch = if (analysis != null) {
                    composeView.context.getString(
                        R.string.pitch_format,
                        String.format(java.util.Locale.US, "%.0f", analysis.f0Mean)
                    )
                } else {
                    composeView.context.getString(R.string.pitch_format, "N/A")
                }
                val formants = if (analysis != null) {
                    composeView.context.getString(
                        R.string.formants_format,
                        String.format(java.util.Locale.US, "%.0f", analysis.f1Mean),
                        String.format(java.util.Locale.US, "%.0f", analysis.f2Mean)
                    )
                } else {
                    composeView.context.getString(R.string.formants_format, "N/A", "N/A")
                }
                val transcript = analysis?.transcript.orEmpty()
                realm.close()
                Triple(pitch, formants, transcript)
            }
                .subscribeOn(RxSchedulers.io())
                .observeOn(RxSchedulers.main())
                .subscribe({ (pitch, formants, transcript) ->
                    if (currentPhotoId == photoId) {
                        pitchState.value = pitch
                        formantsState.value = formants
                        transcriptState.value = transcript
                    }
                }, { /* keep placeholder on error */ })

            // Subscribe to playback broadcast so state updates when audio completes
            playbackDisposable = AudioPlayerManager.playbackEvents
                .observeOn(RxSchedulers.main())
                .subscribe { (audioId, playing) ->
                    if (audioId == photoId) {
                        isPlayingState.value = playing
                    }
                }

            composeView.setContent {
                OpenTransitionTheme(
                    colorVariant = SettingsManager.getResolvedComposeColorVariant()
                ) {
                    GalleryAudioItem(
                        photoId = photoId,
                        audioFilePath = filePath,
                        dateText = dateText,
                        pitchText = pitchState.value,
                        formantsText = formantsState.value,
                        transcriptText = transcriptState.value,
                        isPlaying = isPlayingState.value,
                        isSelected = isSelected,
                        selectionMode = selectionMode,
                        onPlayPause = {
                            val playing = AudioPlayerManager.togglePlayback(
                                photoId, File(filePath)
                            )
                            isPlayingState.value = playing
                        },
                        onClick = {
                            val adapter = adapterRef.get() ?: return@GalleryAudioItem
                            val event: GalleryUiEvent = when (adapter.selectionMode) {
                                true -> {
                                    if (adapter.selectedIds.contains(photoId)) {
                                        adapter.selectedIds.remove(photoId)
                                    } else {
                                        adapter.selectedIds.add(photoId)
                                    }
                                    val list = ArrayList<String>(adapter.selectedIds.size)
                                    list.addAll(adapter.selectedIds)
                                    GalleryUiEvent.SelectionUpdated(list)
                                }
                                false -> GalleryUiEvent.ImageClick(photoId)
                            }
                            adapter.eventRelayRef.get()?.accept(event)
                        },
                        onLongClick = {
                            val adapter = adapterRef.get() ?: return@GalleryAudioItem
                            if (!adapter.selectionMode) {
                                adapter.eventRelayRef.get()
                                    ?.accept(GalleryUiEvent.SelectionUpdated(arrayListOf(photoId)))
                            } else {
                                if (adapter.selectedIds.contains(photoId)) {
                                    adapter.selectedIds.remove(photoId)
                                } else {
                                    adapter.selectedIds.add(photoId)
                                }
                                val list = ArrayList<String>(adapter.selectedIds.size)
                                list.addAll(adapter.selectedIds)
                                adapter.eventRelayRef.get()
                                    ?.accept(GalleryUiEvent.SelectionUpdated(list))
                            }
                        }
                    )
                }
            }
        }

        fun onRecycled() {
            analysisDisposable?.dispose()
            playbackDisposable?.dispose()
        }
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        super.onViewRecycled(holder)
        if (holder is AudioViewHolder) holder.onRecycled()
    }

    @Suppress("MayBeConstant")
    companion object {
        private val TYPE_TITLE = 0
        private val TYPE_PHOTO = 1
    }
}
