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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.ui.widget.SpectrogramView
import java.io.File

/**
 * Gallery grid cell for an audio item.
 *
 * Shows the recording date, a spectrogram of the actual audio content (the
 * primary ear-training visual), an optional transcript, and a play/pause
 * button.  Numeric pitch/formant values are intentionally omitted — the
 * spectrogram is a neutral representation that helps users correlate what
 * they hear with what they see, without providing "targets" to fixate on.
 *
 * @param photoId          Unique ID for this audio item (used for playback tracking).
 * @param audioFilePath    Absolute path to the audio file.
 * @param dateText         Formatted date string.
 * @param transcriptText   Speech transcript captured during recording, or empty.
 * @param isPlaying        Whether this audio is currently playing.
 * @param isSelected       Whether this item is currently selected.
 * @param selectionMode    Whether the gallery is in multi-select mode.
 * @param onPlayPause      Called when the play/pause button is tapped.
 * @param onClick          Called on single tap of the item.
 * @param onLongClick      Called on long press of the item.
 * @param modifier         Optional [Modifier] for the caller.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryAudioItem(
    photoId: String,
    audioFilePath: String,
    dateText: String,
    transcriptText: String = "",
    isPlaying: Boolean,
    isSelected: Boolean,
    selectionMode: Boolean,
    onPlayPause: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 140.dp)
            .background(Color(0x40FFFFFF), RoundedCornerShape(24.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.IconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(56.dp)
        ) {
            androidx.compose.material3.Icon(
                painter = androidx.compose.ui.res.painterResource(
                    if (isPlaying) android.R.drawable.ic_media_pause
                    else android.R.drawable.ic_media_play
                ),
                contentDescription = if (isPlaying)
                    androidx.compose.ui.res.stringResource(R.string.stop_recording)
                else
                    androidx.compose.ui.res.stringResource(R.string.play_audio),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyMedium
            )
            if (transcriptText.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = transcriptText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // Spectrogram: shows actual frequency content over time. Paired with
            // playback this is the primary ear-training tool — users correlate
            // what they hear with what they see, across recordings over time.
            AndroidView(
                factory = { ctx -> SpectrogramView(ctx) },
                update = { spectrogramView ->
                    val file = File(audioFilePath)
                    if (file.exists()) spectrogramView.setAudioFile(file)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(top = 8.dp, end = 8.dp)
            )
        }

        if (selectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
