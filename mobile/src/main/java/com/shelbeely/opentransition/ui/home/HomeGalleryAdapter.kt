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
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.data.Photo
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme
import com.shelbeely.opentransition.ui.widget.SquareConstraintLayout
import com.shelbeely.opentransition.util.AudioPlayerManager
import com.shelbeely.opentransition.util.RxSchedulers
import com.shelbeely.opentransition.util.isNotDisposed
import com.shelbeely.opentransition.util.openDefault
import com.shelbeely.opentransition.util.settings.SettingsManager
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.disposables.Disposable
import io.realm.kotlin.Realm
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.rx3.asObservable
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
            is PhotoViewHolder -> holder.bind(result[position])
            is AudioViewHolder -> holder.bind(result[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            TYPE_PHOTO -> {
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

                PhotoViewHolder(itemView, composeView, eventRelayRef.get())
            }
            TYPE_AUDIO -> {
                val context = parent.context
                val density = context.resources.displayMetrics.density
                val margin = (4 * density).toInt()
                val padding = (8 * density).toInt()
                val width = (120 * density).toInt()

                val composeView = ComposeView(context).apply {
                    layoutParams = ConstraintLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setViewCompositionStrategy(
                        ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
                    )
                }

                val itemView = ConstraintLayout(context).apply {
                    layoutParams = RecyclerView.LayoutParams(
                        width,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    ).apply {
                        marginStart = margin
                        marginEnd = margin
                    }
                    setBackgroundResource(R.drawable.rounded_image_background)
                    setPadding(padding, padding, padding, padding)
                    addView(composeView)
                }

                AudioViewHolder(itemView, composeView, eventRelayRef.get())
            }
            else -> throw IllegalArgumentException("Unhandled item type")
        }
    }

    override fun getItemCount(): Int = result.count()

    override fun getItemViewType(position: Int): Int =
        if (type == Photo.TYPE_AUDIO) TYPE_AUDIO else TYPE_PHOTO

    override fun onViewRecycled(holder: BaseViewHolder) {
        super.onViewRecycled(holder)
        if (holder is AudioViewHolder) holder.onRecycled()
    }

    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class PhotoViewHolder(
        itemView: View,
        private val composeView: ComposeView,
        eventRelay: PublishRelay<HomeUiEvent>?
    ) : BaseViewHolder(itemView) {
        private val eventRelayRef = WeakReference(eventRelay)

        fun bind(photo: Photo) {
            composeView.setContent {
                OpenTransitionTheme(
                    colorVariant = SettingsManager.getResolvedComposeColorVariant()
                ) {
                    HomeGalleryPhotoItem(
                        filePath = photo.filePath,
                        onClick = { eventRelayRef.get()?.accept(HomeUiEvent.ImageClick(photo.id)) }
                    )
                }
            }
        }
    }

    class AudioViewHolder(
        itemView: View,
        private val composeView: ComposeView,
        eventRelay: PublishRelay<HomeUiEvent>?
    ) : BaseViewHolder(itemView) {
        private val eventRelayRef = WeakReference(eventRelay)
        private val isPlayingState = mutableStateOf(false)
        private var currentPhotoId = ""
        private var currentFilePath = ""
        private var playbackDisposable: Disposable? = null

        fun bind(photo: Photo) {
            playbackDisposable?.dispose()

            currentPhotoId = photo.id
            currentFilePath = photo.filePath
            isPlayingState.value = AudioPlayerManager.isPlaying(photo.id)

            val photoId = currentPhotoId

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
                    HomeGalleryAudioItem(
                        photoId = currentPhotoId,
                        audioFilePath = currentFilePath,
                        isPlaying = isPlayingState.value,
                        onPlayPause = {
                            val playing = AudioPlayerManager.togglePlayback(
                                currentPhotoId, File(currentFilePath)
                            )
                            isPlayingState.value = playing
                        },
                        onClick = {
                            eventRelayRef.get()?.accept(HomeUiEvent.ImageClick(currentPhotoId))
                        }
                    )
                }
            }
        }

        fun onRecycled() {
            playbackDisposable?.dispose()
        }
    }

    @Suppress("MayBeConstant")
    companion object {
        private const val TYPE_PHOTO = 2
        private const val TYPE_AUDIO = 3
    }
}
