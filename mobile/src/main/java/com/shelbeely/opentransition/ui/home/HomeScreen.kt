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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.util.toFullDateString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onAddFacePhoto: () -> Unit,
    onAddBodyPhoto: () -> Unit,
    onAddAudio: () -> Unit,
    onSettings: () -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onFaceGallery: () -> Unit,
    onBodyGallery: () -> Unit,
    onAudioGallery: () -> Unit,
    faceRecyclerView: RecyclerView,
    bodyRecyclerView: RecyclerView,
    audioRecyclerView: RecyclerView,
    onMilestonesClick: () -> Unit,
) {
    val context = LocalContext.current
    val loaded = state as? HomeUiState.Loaded

    var speedDialExpanded by remember { mutableStateOf(false) }
    val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        exitDirection = FloatingToolbarExitDirection.Top
    )
    val toolbarExpanded = scrollBehavior.state.offset == 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(loaded?.showPreviousRecord, loaded?.showNextRecord) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { _ -> totalDrag = 0f },
                    onDragEnd = {
                        if (totalDrag < -100f && loaded?.showPreviousRecord == true) {
                            onPreviousDay()
                        } else if (totalDrag > 100f && loaded?.showNextRecord == true) {
                            onNextDay()
                        }
                    }
                ) { change, dragAmount ->
                    change.consume()
                    totalDrag += dragAmount
                }
            }
    ) {
        // Main scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior)
        ) {
            // Reserve space so content starts below the floating toolbar
            Spacer(modifier = Modifier.height(80.dp))

            // Date summary (visible below toolbar)
            if (loaded != null) {
                HomeDateSummaryItem(
                    startDate = context.getString(
                        R.string.start_date,
                        loaded.startDate.toFullDateString(context)
                    ),
                    currentDate = context.getString(
                        R.string.current_date,
                        loaded.currentDate.toFullDateString(context)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Spacer(modifier = Modifier.height(56.dp))
            }

            // Gallery sections
            GallerySection(
                label = stringResource(R.string.face_gallery),
                onGalleryClick = onFaceGallery,
                recyclerView = faceRecyclerView,
                modifier = Modifier.weight(1f)
            )
            GallerySection(
                label = stringResource(R.string.body_gallery),
                onGalleryClick = onBodyGallery,
                recyclerView = bodyRecyclerView,
                modifier = Modifier.weight(1f),
                labelTopPadding = 0.dp
            )
            GallerySection(
                label = stringResource(R.string.audio_gallery),
                onGalleryClick = onAudioGallery,
                recyclerView = audioRecyclerView,
                modifier = Modifier.weight(1f),
                labelTopPadding = 0.dp
            )
        }

        // Floating toolbar — sits on top of content at the top-center
        HorizontalFloatingToolbar(
            expanded = toolbarExpanded,
            floatingActionButton = {
                SmallFloatingActionButton(
                    onClick = { scrollBehavior.state.offset = 0f }
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = stringResource(R.string.navigate_days)
                    )
                }
            },
            scrollBehavior = scrollBehavior,
            colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp, start = 16.dp, end = 16.dp)
        ) {
            IconButton(
                onClick = onPreviousDay,
                modifier = Modifier.alpha(if (loaded?.showPreviousRecord == true) 1f else 0f)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_left_white_64dp),
                    contentDescription = stringResource(R.string.previous_record),
                    tint = Color.Unspecified
                )
            }
            Text(
                text = loaded?.dayString ?: "",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(
                onClick = onNextDay,
                modifier = Modifier.alpha(if (loaded?.showNextRecord == true) 1f else 0f)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right_white_64dp),
                    contentDescription = stringResource(R.string.next_record),
                    tint = Color.Unspecified
                )
            }
            HomeMilestonesButton(
                hasMilestones = loaded?.hasMilestones == true,
                onClick = onMilestonesClick
            )
            IconButton(onClick = onSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.edit_settings)
                )
            }
        }

        // Speed-dial backdrop — dismisses the menu when tapping outside
        if (speedDialExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { speedDialExpanded = false }
            )
        }

        // Speed-dial FAB (bottom-end)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(
                visible = speedDialExpanded,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpeedDialItem(
                        label = stringResource(R.string.add_audio_recording),
                        icon = { Icon(Icons.Filled.Mic, contentDescription = null) },
                        onClick = { speedDialExpanded = false; onAddAudio() }
                    )
                    SpeedDialItem(
                        label = stringResource(R.string.add_body_photo),
                        icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        onClick = { speedDialExpanded = false; onAddBodyPhoto() }
                    )
                    SpeedDialItem(
                        label = stringResource(R.string.add_face_photo),
                        icon = { Icon(Icons.Filled.Face, contentDescription = null) },
                        onClick = { speedDialExpanded = false; onAddFacePhoto() }
                    )
                }
            }
            FloatingActionButton(
                onClick = { speedDialExpanded = !speedDialExpanded }
            ) {
                Icon(
                    imageVector = if (speedDialExpanded) Icons.Filled.Close else Icons.Filled.Add,
                    contentDescription = if (speedDialExpanded) {
                        stringResource(R.string.collapse_add_menu)
                    } else {
                        stringResource(R.string.add)
                    }
                )
            }
        }
    }
}

@Composable
private fun SpeedDialItem(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 2.dp
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }
        SmallFloatingActionButton(onClick = onClick) { icon() }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GallerySection(
    label: String,
    onGalleryClick: () -> Unit,
    recyclerView: RecyclerView,
    modifier: Modifier = Modifier,
    labelTopPadding: Dp = 8.dp,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AssistChip(
            onClick = onGalleryClick,
            label = { Text(label, style = MaterialTheme.typography.titleMedium) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null
                )
            },
            modifier = Modifier.padding(top = labelTopPadding, start = 8.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            AndroidView(
                factory = { recyclerView },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

