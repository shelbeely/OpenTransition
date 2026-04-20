/*
 * Copyright © 2018-2023 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.home

import android.Manifest
import android.content.DialogInterface
import android.net.Uri
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.TransTracksApp
import com.shelbeely.opentransition.background.CameraHandler
import com.shelbeely.opentransition.background.StoragePermissionHandler
import com.shelbeely.opentransition.data.Photo
import com.shelbeely.opentransition.domain.HomeAction
import com.shelbeely.opentransition.domain.HomeDomain
import com.shelbeely.opentransition.domain.HomeResult
import com.shelbeely.opentransition.ui.MainActivity
import com.shelbeely.opentransition.ui.PickMediaHandlingData
import com.shelbeely.opentransition.ui.home.HomeFragmentDirections
import com.shelbeely.opentransition.util.AnalyticsUtil
import com.shelbeely.opentransition.util.Event
import com.shelbeely.opentransition.util.Observables
import com.shelbeely.opentransition.util.boxed
import com.shelbeely.opentransition.util.isNotDisposed
import com.shelbeely.opentransition.util.ofType
import com.shelbeely.opentransition.util.plusAssign
import com.shelbeely.opentransition.util.settings.SettingsManager
import com.shelbeely.opentransition.util.toFullDateString
import io.reactivex.rxjava3.core.ObservableTransformer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.io.File

class HomeFragment : Fragment(R.layout.home) {
    private var resultDisposable: Disposable = Disposable.disposed()
    private var photoTakenDisposable: Disposable = Disposable.disposed()
    private var viewDisposables: CompositeDisposable = CompositeDisposable()

    private var lastCameraEvent: HomeUiEvent.AddPhotoCamera? = null

    override fun onStart() {
        super.onStart()
        val view = view as? HomeView ?: throw AssertionError("View must be HomeView")

        AnalyticsUtil.logEvent(Event.HomeControllerShown)

        val domain: HomeDomain = TransTracksApp.instance.domainManager.homeDomain

        if (resultDisposable.isDisposed) {
            resultDisposable = domain.results.subscribe()
        }

        domain.actions.accept(HomeAction.ReloadDay)

        if (SettingsManager.showWelcome()) {
            val builder = AlertDialog.Builder(requireContext())
                .setTitle(R.string.welcome)
                .setMessage(R.string.welcome_message)

            val composeView = ComposeView(builder.context).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                setContent {
                    val startDateText = SettingsManager.getStartDate(requireActivity()).toFullDateString(context)
                    val lockText = SettingsManager.getLockType().let { lockType ->
                        if (lockType == com.shelbeely.opentransition.util.settings.LockType.off) getString(R.string.disabled)
                        else getString(lockType.displayNameRes())
                    }
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(getString(R.string.start_date_label), style = MaterialTheme.typography.bodyMedium)
                            Text(startDateText, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(getString(R.string.lock_label), style = MaterialTheme.typography.bodyMedium)
                            Text(lockText, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            builder.setView(composeView)
                .setPositiveButton(R.string.looks_good, null)
                .setNegativeButton(R.string.change_setting) { dialog: DialogInterface, _: Int ->
                    findNavController().navigate(HomeFragmentDirections.actionGoToSettings())
                    dialog.dismiss()
                }
                .show()

            SettingsManager.setShowWelcome(false, requireActivity())
        } else if (SettingsManager.showAccountWarning()) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.warning_title)
                .setMessage(R.string.warning_message)
                .setPositiveButton(R.string.create_account) { dialog, _ ->
                    SettingsManager.setAccountWarning(false, context)
                    dialog.dismiss()
                    findNavController().navigate(HomeFragmentDirections.actionGoToSettings())
                }
                .setNegativeButton(R.string.risk_it) { dialog, _ ->
                    SettingsManager.setAccountWarning(false, context)
                    dialog.dismiss()
                }
                .show()
        }

        viewDisposables += domain.results
            .compose(homeResultsToStates)
            .subscribe { state -> view.display(state) }

        val sharedEvents = view.events.share()

        viewDisposables += sharedEvents
            .ofType<HomeUiEvent.PreviousRecord>()
            .map { HomeAction.PreviousDay }
            .subscribe(domain.actions)

        viewDisposables += sharedEvents
            .ofType<HomeUiEvent.NextRecord>()
            .map { HomeAction.NextDay }
            .subscribe(domain.actions)

        viewDisposables += StoragePermissionHandler.storagePermissionBlocked
            .filter { showRationale -> !showRationale }
            .subscribe { _ ->
                StoragePermissionHandler.showStoragePermissionDisabledSnackBar(
                    view, activity as AppCompatActivity
                )
            }

        viewDisposables += sharedEvents.ofType<HomeUiEvent.AddPhotoCamera>()
            .subscribe { event ->
                // Navigate to new CameraFragment with face detection
                findNavController().navigate(
                    HomeFragmentDirections.actionGlobalCamera(
                        type = event.type ?: Photo.TYPE_FACE,
                        destinationToPopTo = R.id.homeFragment,
                        epochDay = event.currentDate?.toEpochDay()?.boxed()
                    )
                )
            }

        viewDisposables += Observables.combineLatest(
            sharedEvents.ofType<HomeUiEvent.AddPhotoGallery>(),
            StoragePermissionHandler.storagePermissionEnabled
        ) { event, storageEnabled -> event to storageEnabled }
            .subscribe { (event, storageEnabled) ->
                val activity = requireActivity() as? MainActivity ?: return@subscribe
                val type = event.type ?: Photo.TYPE_BODY

                if (PickVisualMedia.isPhotoPickerAvailable(requireContext())) {
                    activity.launchPickMedia(
                        PickVisualMedia.ImageOnly,
                        PickMediaHandlingData(
                            type, R.id.homeFragment, event.currentDate?.toEpochDay()
                        )
                    )
                } else if (storageEnabled) {
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeToSelectPhoto(
                            type = type,
                            destinationToPopTo = R.id.homeFragment,
                            epochDay = event.currentDate?.toEpochDay()?.boxed(),
                        )
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    CameraHandler.requestPhotoFromAnotherApp(activity)
                } else {
                    StoragePermissionHandler.handleRequestingPermission(
                        view, activity as AppCompatActivity
                    )
                }
            }

        viewDisposables += sharedEvents
            .filter { event ->
                event !is HomeUiEvent.PreviousRecord
                        && event !is HomeUiEvent.NextRecord
                        && event !is HomeUiEvent.AddPhotoCamera
                        && event !is HomeUiEvent.AddPhotoGallery
                        && event !is HomeUiEvent.AddAudioRecording
            }
            .subscribe { event ->
                when (event) {
                    is HomeUiEvent.Settings -> findNavController()
                        .navigate(HomeFragmentDirections.actionGoToSettings())

                    is HomeUiEvent.Milestones -> findNavController()
                        .navigate(HomeFragmentDirections.actionShowMilestones(event.day))

                    is HomeUiEvent.FaceGallery -> findNavController().navigate(
                        HomeFragmentDirections.actionShowGallery(
                            isFaceGallery = true, initialDay = event.day
                        )
                    )

                    is HomeUiEvent.BodyGallery -> findNavController().navigate(
                        HomeFragmentDirections.actionShowGallery(
                            isFaceGallery = false, initialDay = event.day
                        )
                    )

                    is HomeUiEvent.AudioGallery -> findNavController().navigate(
                        HomeFragmentDirections.actionShowGallery(
                            isFaceGallery = false, 
                            initialDay = event.day,
                            galleryType = Photo.TYPE_AUDIO
                        )
                    )

                    is HomeUiEvent.ImageClick -> findNavController().navigate(
                        HomeFragmentDirections.actionHomeToSinglePhoto(photoId = event.photoId)
                    )

                    is HomeUiEvent.AddPhotoCamera,
                    is HomeUiEvent.AddPhotoGallery,
                    is HomeUiEvent.AddAudioRecording,
                    is HomeUiEvent.NextRecord,
                    is HomeUiEvent.PreviousRecord -> throw IllegalStateException("unexpected event")
                }
            }
        
        viewDisposables += sharedEvents.ofType<HomeUiEvent.AddAudioRecording>()
            .subscribe { event ->
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeToRecordAudio()
                )
            }

        if (photoTakenDisposable.isDisposed) {
            photoTakenDisposable = CameraHandler.photoTaken
                .subscribe { absolutePath ->
                    val navController = findNavController()
                    navController.navigate(
                        HomeFragmentDirections.actionGlobalAssignPhotos(
                            uris = arrayOf(Uri.fromFile(File(absolutePath))),
                            type = lastCameraEvent?.type ?: Photo.TYPE_BODY,
                            destinationToPopTo = R.id.homeFragment,
                            epochDay = lastCameraEvent?.currentDate?.toEpochDay()?.boxed()
                        )
                    )
                }
        }
    }

    override fun onDetach() {
        viewDisposables.clear()
        super.onDetach()
    }

    override fun onDestroyView() {
        if (resultDisposable.isNotDisposed()) {
            resultDisposable.dispose()
        }
        if (photoTakenDisposable.isNotDisposed()) {
            photoTakenDisposable.dispose()
        }
        super.onDestroyView()
    }
}

val homeResultsToStates = ObservableTransformer<HomeResult, HomeUiState> { results ->
    results.map { result ->
        return@map when (result) {
            is HomeResult.Loading -> HomeUiState.Loading

            is HomeResult.Loaded ->
                HomeUiState.Loaded(
                    result.dayString, result.showPreviousRecord, result.showNextRecord,
                    result.startDate, result.currentDate, result.hasMilestones
                )
        }
    }
}
