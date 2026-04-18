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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.data.Photo
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme
import com.shelbeely.opentransition.ui.widget.AdapterSpanSizeLookup
import com.shelbeely.opentransition.util.RxSchedulers
import com.shelbeely.opentransition.util.getString
import com.shelbeely.opentransition.util.openDefault
import com.shelbeely.opentransition.util.settings.SettingsManager
import com.shelbeely.opentransition.util.setVisibleOrGone
import com.shelbeely.opentransition.util.toFullDateString
import com.jakewharton.rxrelay3.PublishRelay
import com.squareup.picasso.Picasso
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.Sort.DESCENDING
import kotlinx.coroutines.rx3.asObservable
import kotterknife.bindView
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
                if (type == Photo.TYPE_AUDIO) {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.gallery_adapter_audio_item, parent, false)
                    AudioViewHolder(view, this)
                } else {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.gallery_adapter_item, parent, false)
                    PhotoViewHolder(view, this)
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
        TYPE_PHOTO -> 1
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

    class PhotoViewHolder(itemView: View, creatingAdapter: GalleryAdapter?) :
        BaseViewHolder(itemView) {
        private val image: ImageView by bindView(R.id.gallery_adapter_item_image)
        private val selection: ImageView by bindView(R.id.gallery_adapter_item_selection)

        private val adapterRef = WeakReference(creatingAdapter)

        private var currentPhotoId = ""

        init {
            itemView.setOnClickListener {
                val adapter = adapterRef.get() ?: return@setOnClickListener

                val event: GalleryUiEvent = when (adapter.selectionMode) {
                    true -> {
                        if (adapter.selectedIds.contains(currentPhotoId)) {
                            adapter.selectedIds.remove(currentPhotoId)
                        } else {
                            adapter.selectedIds.add(currentPhotoId)
                        }

                        val returnList = ArrayList<String>(adapter.selectedIds.size)
                        returnList.addAll(adapter.selectedIds)

                        GalleryUiEvent.SelectionUpdated(returnList)
                    }

                    false -> GalleryUiEvent.ImageClick(currentPhotoId)
                }

                adapter.eventRelayRef.get()?.accept(event)
            }

            itemView.setOnLongClickListener {
                val adapter = adapterRef.get() ?: return@setOnLongClickListener false

                if (!adapter.selectionMode) {
                    adapter.eventRelayRef.get()
                        ?.accept(GalleryUiEvent.SelectionUpdated(arrayListOf(currentPhotoId)))
                } else {
                    itemView.performClick()
                }

                return@setOnLongClickListener true
            }
        }

        fun bind(item: GalleryAdapterItem, selectionMode: Boolean) {
            currentPhotoId = item.photo!!.id

            Picasso.get()
                .load(File(item.photo.filePath))
                .fit()
                .centerCrop()
                .into(image)

            selection.setVisibleOrGone(selectionMode)

            if (selectionMode) {
                val selectionRes = when (item.selected) {
                    true -> R.drawable.ic_selected_primary_36dp
                    false -> R.drawable.ic_unselected_primary_36dp
                }

                selection.setImageResource(selectionRes)
                selection.contentDescription = when (item.selected) {
                    true -> itemView.getString(R.string.selected)
                    false -> itemView.getString(R.string.not_selected)
                }
            }
        }
    }

    class AudioViewHolder(itemView: View, creatingAdapter: GalleryAdapter?) :
        BaseViewHolder(itemView) {
        private val playButton: ImageButton by bindView(R.id.audio_play_button)
        private val dateText: TextView by bindView(R.id.audio_date)
        private val pitchText: TextView by bindView(R.id.audio_pitch)
        private val formantsText: TextView by bindView(R.id.audio_formants)
        private val waveformView: com.shelbeely.opentransition.ui.widget.WaveformView by bindView(R.id.audio_waveform)
        private val selectionCheckbox: CheckBox by bindView(R.id.audio_selection_checkbox)

        private val adapterRef = WeakReference(creatingAdapter)
        private var currentPhotoId = ""
        private var currentAudioFile: File? = null

        init {
            itemView.setOnClickListener {
                val adapter = adapterRef.get() ?: return@setOnClickListener

                val event: GalleryUiEvent = when (adapter.selectionMode) {
                    true -> {
                        if (adapter.selectedIds.contains(currentPhotoId)) {
                            adapter.selectedIds.remove(currentPhotoId)
                        } else {
                            adapter.selectedIds.add(currentPhotoId)
                        }

                        val returnList = ArrayList<String>(adapter.selectedIds.size)
                        returnList.addAll(adapter.selectedIds)

                        GalleryUiEvent.SelectionUpdated(returnList)
                    }

                    false -> GalleryUiEvent.ImageClick(currentPhotoId)
                }

                adapter.eventRelayRef.get()?.accept(event)
            }

            itemView.setOnLongClickListener {
                val adapter = adapterRef.get() ?: return@setOnLongClickListener false

                if (!adapter.selectionMode) {
                    adapter.eventRelayRef.get()
                        ?.accept(GalleryUiEvent.SelectionUpdated(arrayListOf(currentPhotoId)))
                } else {
                    itemView.performClick()
                }

                return@setOnLongClickListener true
            }

            playButton.setOnClickListener {
                currentAudioFile?.let { file ->
                    val isPlaying = com.shelbeely.opentransition.util.AudioPlayerManager.togglePlayback(currentPhotoId, file)
                    updatePlayButtonState(isPlaying)
                }
            }
        }
        
        private fun updatePlayButtonState(isPlaying: Boolean) {
            playButton.setImageResource(
                if (isPlaying) android.R.drawable.ic_media_pause
                else android.R.drawable.ic_media_play
            )
        }

        fun bind(item: GalleryAdapterItem, selectionMode: Boolean) {
            currentPhotoId = item.photo!!.id
            currentAudioFile = File(item.photo.filePath)

            // Display date
            dateText.text = LocalDate.ofEpochDay(item.photo.epochDay).toFullDateString(itemView.context)

            // Update play button state based on current playback
            updatePlayButtonState(com.shelbeely.opentransition.util.AudioPlayerManager.isPlaying(currentPhotoId))

            // Set waveform
            if (currentAudioFile?.exists() == true) {
                waveformView.setAudioFile(currentAudioFile!!)
            }

            // Load audio analysis if available
            val realm = Realm.openDefault()
            val analysis = realm.query(com.shelbeely.opentransition.data.AudioAnalysis::class, 
                "photoId == '$currentPhotoId'")
                .first()
                .find()

            if (analysis != null) {
                pitchText.text = itemView.context.getString(
                    R.string.pitch_format,
                    String.format("%.0f", analysis.f0Mean)
                )
                formantsText.text = itemView.context.getString(
                    R.string.formants_format,
                    String.format("%.0f", analysis.f1Mean),
                    String.format("%.0f", analysis.f2Mean)
                )
            } else {
                pitchText.text = itemView.context.getString(R.string.pitch_format, "N/A")
                formantsText.text = itemView.context.getString(R.string.formants_format, "N/A", "N/A")
            }

            realm.close()

            selectionCheckbox.setVisibleOrGone(selectionMode)
            selectionCheckbox.isChecked = item.selected
        }
    }

    @Suppress("MayBeConstant")
    companion object {
        private val TYPE_TITLE = 0
        private val TYPE_PHOTO = 1
    }
}
