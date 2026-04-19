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

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.util.toFullDateString

@Composable
fun HomeScreen(
    state: HomeUiState,
    onTakePhoto: () -> Unit,
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

    Column(
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
        // Top bar: Take photo button (start) + Settings button (end)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onTakePhoto,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_photo_camera_white_24dp),
                    contentDescription = stringResource(R.string.take_photo),
                    tint = Color.White
                )
            }
            IconButton(
                onClick = onSettings,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings_white_24dp),
                    contentDescription = stringResource(R.string.edit_settings),
                    tint = Color.White
                )
            }
        }

        // Day navigation: Previous arrow + Day title + Next arrow
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = onPreviousDay,
                modifier = Modifier.alpha(if (loaded?.showPreviousRecord == true) 1f else 0f)
            ) {
                Icon(
                    painter = painterResource(R.drawable.previous),
                    contentDescription = stringResource(R.string.previous_record),
                    tint = Color.Unspecified
                )
            }
            Text(
                text = loaded?.dayString ?: "",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(
                onClick = onNextDay,
                modifier = Modifier.alpha(if (loaded?.showNextRecord == true) 1f else 0f)
            ) {
                Icon(
                    painter = painterResource(R.drawable.next),
                    contentDescription = stringResource(R.string.next_record),
                    tint = Color.Unspecified
                )
            }
        }

        // Divider
        Image(
            painter = painterResource(R.drawable.fading_line),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .padding(horizontal = 24.dp)
        )

        // Date summary + Milestones button
        if (loaded != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeDateSummaryItem(
                    startDate = context.getString(
                        R.string.start_date,
                        loaded.startDate.toFullDateString(context)
                    ),
                    currentDate = context.getString(
                        R.string.current_date,
                        loaded.currentDate.toFullDateString(context)
                    ),
                    modifier = Modifier.weight(1f, fill = false)
                )
                HomeMilestonesButton(
                    hasMilestones = loaded.hasMilestones,
                    onClick = onMilestonesClick,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.height(56.dp))
        }

        // Divider
        Image(
            painter = painterResource(R.drawable.fading_line),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .padding(horizontal = 24.dp)
        )

        // Face gallery section
        GallerySection(
            label = stringResource(R.string.face_gallery),
            onGalleryClick = onFaceGallery,
            recyclerView = faceRecyclerView,
            modifier = Modifier.weight(1f)
        )

        // Body gallery section
        GallerySection(
            label = stringResource(R.string.body_gallery),
            onGalleryClick = onBodyGallery,
            recyclerView = bodyRecyclerView,
            modifier = Modifier.weight(1f),
            labelTopPadding = 0.dp
        )

        // Audio gallery section
        GallerySection(
            label = stringResource(R.string.audio_gallery),
            onGalleryClick = onAudioGallery,
            recyclerView = audioRecyclerView,
            modifier = Modifier.weight(1f),
            labelTopPadding = 0.dp
        )
    }
}

@Composable
private fun GallerySection(
    label: String,
    onGalleryClick: () -> Unit,
    recyclerView: RecyclerView,
    modifier: Modifier = Modifier,
    labelTopPadding: androidx.compose.ui.unit.Dp = 8.dp,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = onGalleryClick,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = labelTopPadding)
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.25f),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge
            )
        }
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
