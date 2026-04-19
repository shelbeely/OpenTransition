/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.home

import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.data.Photo
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme
import com.shelbeely.opentransition.ui.widget.SquareConstraintLayout
import com.shelbeely.opentransition.util.RxSchedulers
import com.shelbeely.opentransition.util.isNotDisposed
import com.shelbeely.opentransition.util.openDefault
import com.shelbeely.opentransition.util.settings.SettingsManager
import com.jakewharton.rxrelay3.PublishRelay
import com.squareup.picasso.Picasso
import io.reactivex.rxjava3.disposables.Disposable
import io.realm.kotlin.Realm
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.rx3.asObservable
import kotterknife.bindView
import java.io.File
import java.lang.ref.WeakReference
import java.time.LocalDate

class HomeGalleryAdapter(
    private val currentDate: LocalDate, @Photo.Type private val type: Int,
    eventRelay: PublishRelay<HomeUiEvent>
) : RecyclerView.Adapter<HomeGalleryAdapter.BaseViewHolder>() {
    private var photosDisposable = Disposable.disposed()
    private var result = emptyList<Photo>()

    private val eventRelayRef = WeakReference(eventRelay)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if (photosDisposable.isNotDisposed()) {
            photosDisposable.dispose()
        }

        val mainThreadHandler = Handler(recyclerView.context.mainLooper)

        photosDisposable = Realm.openDefault()
            .query(
                Photo::class,
                "${Photo.FIELD_TYPE} == $type && ${Photo.FIELD_EPOCH_DAY} ==  ${currentDate.toEpochDay()}"
            )
            .sort(Photo.FIELD_TIMESTAMP, Sort.DESCENDING)
            .find()
            .asFlow()
            .asObservable()
            .observeOn(RxSchedulers.io())
            .subscribe {
                result = it.list
                mainThreadHandler.post(::notifyDataSetChanged)
            }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        photosDisposable.dispose()
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (holder) {
            is AddViewHolder -> holder.bind(currentDate, type)
            is PhotoViewHolder -> holder.bind(result[position - 1])
            is AudioViewHolder -> holder.bind(result[position - 1])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            TYPE_ADD -> {
                val context = parent.context
                val density = context.resources.displayMetrics.density
                val margin = (4 * density).toInt()
                val padding = (6 * density).toInt()

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
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    ).apply {
                        marginStart = margin
                        marginEnd = margin
                    }
                    orientation = 0
                    setBackgroundResource(R.drawable.rounded_image_background)
                    setPadding(padding, padding, padding, padding)
                    addView(composeView)
                }

                AddViewHolder(itemView, composeView, eventRelayRef.get())
            }
            TYPE_PHOTO -> PhotoViewHolder(
                LayoutInflater.from(parent.context).inflate(viewType, parent, false),
                eventRelayRef.get()
            )
            TYPE_AUDIO -> AudioViewHolder(
                LayoutInflater.from(parent.context).inflate(viewType, parent, false),
                eventRelayRef.get()
            )
            else -> throw IllegalArgumentException("Unhandled item type")
        }
    }

    override fun getItemCount(): Int = result.count() + 1

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> TYPE_ADD
        else -> if (type == Photo.TYPE_AUDIO) TYPE_AUDIO else TYPE_PHOTO
    }

    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class AddViewHolder(
        itemView: View,
        private val composeView: ComposeView,
        eventRelay: PublishRelay<HomeUiEvent>?
    ) :
        BaseViewHolder(itemView) {
        private val eventRelayRef = WeakReference(eventRelay)

        fun bind(currentDate: LocalDate, @Photo.Type type: Int) {
            composeView.setContent {
                OpenTransitionTheme(
                    colorVariant = SettingsManager.getResolvedComposeColorVariant()
                ) {
                    HomeGalleryAddItem(
                        contentDescription = itemView.context.getString(
                            when (type) {
                                Photo.TYPE_BODY -> R.string.add_body_photo
                                Photo.TYPE_AUDIO -> R.string.add_audio_recording
                                else -> R.string.add_face_photo
                            }
                        ),
                        onClick = { onAddClick(currentDate, type) }
                    )
                }
            }
        }

        private fun onAddClick(currentDate: LocalDate, @Photo.Type type: Int) {
            if (type == Photo.TYPE_AUDIO) {
                eventRelayRef.get()?.accept(HomeUiEvent.AddAudioRecording(currentDate))
                return
            }

            val popup = PopupMenu(itemView.context, itemView)
            popup.menuInflater.inflate(R.menu.popup_media_source, popup.menu)
            popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.media_source_camera -> eventRelayRef.get()
                        ?.accept(HomeUiEvent.AddPhotoCamera(currentDate, type))

                    R.id.media_source_gallery -> eventRelayRef.get()
                        ?.accept(HomeUiEvent.AddPhotoGallery(currentDate, type))

                    else -> return@setOnMenuItemClickListener false
                }

                true
            }
            popup.show()
        }
    }

    class PhotoViewHolder(itemView: View, eventRelay: PublishRelay<HomeUiEvent>?) :
        BaseViewHolder(itemView) {
        private val image: ImageView by bindView(R.id.home_adapter_item_image)

        private val eventRelayRef = WeakReference(eventRelay)

        private var currentPhotoId = ""

        init {
            itemView.setOnClickListener {
                eventRelayRef.get()?.accept(HomeUiEvent.ImageClick(currentPhotoId))
            }
        }

        fun bind(photo: Photo) {
            currentPhotoId = photo.id

            Picasso.get()
                .load(File(photo.filePath))
                .fit()
                .centerCrop()
                .into(image)
        }
    }

    class AudioViewHolder(itemView: View, eventRelay: PublishRelay<HomeUiEvent>?) :
        BaseViewHolder(itemView) {
        private val playButton: ImageButton by bindView(R.id.home_audio_play_button)
        private val waveformView: com.shelbeely.opentransition.ui.widget.WaveformView by bindView(R.id.home_audio_waveform)

        private val eventRelayRef = WeakReference(eventRelay)
        private var currentPhotoId = ""
        private var currentAudioFile: File? = null

        init {
            itemView.setOnClickListener {
                eventRelayRef.get()?.accept(HomeUiEvent.ImageClick(currentPhotoId))
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

        fun bind(photo: Photo) {
            currentPhotoId = photo.id
            currentAudioFile = File(photo.filePath)

            // Update play button state based on current playback
            updatePlayButtonState(com.shelbeely.opentransition.util.AudioPlayerManager.isPlaying(currentPhotoId))

            // Set waveform
            if (currentAudioFile?.exists() == true) {
                waveformView.setAudioFile(currentAudioFile!!)
            }
        }
    }

    @Suppress("MayBeConstant")
    companion object {
        private val TYPE_PHOTO = R.layout.home_adapter_item
        private val TYPE_ADD = R.layout.home_adapter_add_item
        private val TYPE_AUDIO = R.layout.home_adapter_audio_item
    }
}
