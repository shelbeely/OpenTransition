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

import android.content.Context
import android.util.AttributeSet
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import androidx.appcompat.widget.PopupMenu
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.data.Photo
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme
import com.shelbeely.opentransition.util.applySystemBarInsets
import com.shelbeely.opentransition.util.plusAssign
import com.shelbeely.opentransition.util.settings.SettingsManager
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.time.LocalDate

sealed class HomeUiEvent {
    object Settings : HomeUiEvent()
    object PreviousRecord : HomeUiEvent()
    object NextRecord : HomeUiEvent()
    data class Milestones(val day: Long) : HomeUiEvent()
    data class FaceGallery(val day: Long) : HomeUiEvent()
    data class BodyGallery(val day: Long) : HomeUiEvent()
    data class AudioGallery(val day: Long) : HomeUiEvent()
    data class ImageClick(val photoId: String) : HomeUiEvent()
    data class AddPhotoCamera(
        val currentDate: LocalDate? = null, @Photo.Type val type: Int? = null
    ) : HomeUiEvent()

    data class AddPhotoGallery(
        val currentDate: LocalDate? = null, @Photo.Type val type: Int? = null
    ) : HomeUiEvent()
    
    data class AddAudioRecording(val currentDate: LocalDate? = null) : HomeUiEvent()
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Loaded(
        val dayString: String, val showPreviousRecord: Boolean, val showNextRecord: Boolean,
        val startDate: LocalDate, val currentDate: LocalDate, val hasMilestones: Boolean
    ) : HomeUiState()
}

class HomeView(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    private val eventRelay: PublishRelay<HomeUiEvent> = PublishRelay.create()
    private val viewDisposables: CompositeDisposable = CompositeDisposable()
    val events: Observable<HomeUiEvent> = eventRelay

    private var currentState: HomeUiState = HomeUiState.Loading

    val faceRecyclerView: RecyclerView by lazy {
        RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    val bodyRecyclerView: RecyclerView by lazy {
        RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    val audioRecyclerView: RecyclerView by lazy {
        RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
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
            .subscribe { renderCompose() }

        renderCompose()
    }

    override fun onDetachedFromWindow() {
        viewDisposables.clear()
        super.onDetachedFromWindow()
    }

    fun display(state: HomeUiState) {
        currentState = state
        if (state is HomeUiState.Loaded) {
            faceRecyclerView.adapter = HomeGalleryAdapter(
                state.currentDate, Photo.TYPE_FACE, eventRelay
            )
            bodyRecyclerView.adapter = HomeGalleryAdapter(
                state.currentDate, Photo.TYPE_BODY, eventRelay
            )
            audioRecyclerView.adapter = HomeGalleryAdapter(
                state.currentDate, Photo.TYPE_AUDIO, eventRelay
            )
        }
        renderCompose()
    }

    private fun showPhotoSourceMenu() {
        val popup = PopupMenu(context, composeView)
        popup.menuInflater.inflate(R.menu.popup_media_source, popup.menu)
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.media_source_camera ->
                    eventRelay.accept(HomeUiEvent.AddPhotoCamera())

                R.id.media_source_gallery ->
                    eventRelay.accept(HomeUiEvent.AddPhotoGallery())

                else -> return@setOnMenuItemClickListener false
            }
            true
        }
        popup.show()
    }

    private fun renderCompose() {
        composeView.setContent {
            OpenTransitionTheme(colorVariant = SettingsManager.getResolvedComposeColorVariant()) {
                HomeScreen(
                    state = currentState,
                    onTakePhoto = { showPhotoSourceMenu() },
                    onSettings = { eventRelay.accept(HomeUiEvent.Settings) },
                    onPreviousDay = { eventRelay.accept(HomeUiEvent.PreviousRecord) },
                    onNextDay = { eventRelay.accept(HomeUiEvent.NextRecord) },
                    onFaceGallery = {
                        (currentState as? HomeUiState.Loaded)?.currentDate?.toEpochDay()?.let { day ->
                            eventRelay.accept(HomeUiEvent.FaceGallery(day))
                        }
                    },
                    onBodyGallery = {
                        (currentState as? HomeUiState.Loaded)?.currentDate?.toEpochDay()?.let { day ->
                            eventRelay.accept(HomeUiEvent.BodyGallery(day))
                        }
                    },
                    onAudioGallery = {
                        (currentState as? HomeUiState.Loaded)?.currentDate?.toEpochDay()?.let { day ->
                            eventRelay.accept(HomeUiEvent.AudioGallery(day))
                        }
                    },
                    onMilestonesClick = {
                        (currentState as? HomeUiState.Loaded)?.currentDate?.toEpochDay()?.let { day ->
                            eventRelay.accept(HomeUiEvent.Milestones(day))
                        }
                    },
                    faceRecyclerView = faceRecyclerView,
                    bodyRecyclerView = bodyRecyclerView,
                    audioRecyclerView = audioRecyclerView,
                )
            }
        }
    }
}
