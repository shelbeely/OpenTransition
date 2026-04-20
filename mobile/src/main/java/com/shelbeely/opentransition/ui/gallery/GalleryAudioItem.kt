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

import android.widget.ImageButton
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.ui.widget.WaveformView
import java.io.File

/**
 * Gallery grid cell for an audio item.
 *
 * Migrated from `res/layout/gallery_adapter_audio_item.xml`:
 * - Root was `ConstraintLayout` with `@drawable/rounded_transparent_button` background and 16dp padding
 *   → reproduced as a [Row] with `Modifier.background(Color(0x40FFFFFF), RoundedCornerShape(24dp))`
 * - `ImageButton` play/pause (56×56dp, left side) → `AndroidView { ImageButton }`
 * - `TextView` date, pitch, formants (right of play button) → `Text` composables
 * - `WaveformView` (0dp × 80dp, below formants) → `AndroidView { WaveformView }`
 * - `CheckBox` selection (end, visibility=gone in non-selection mode) → `Checkbox` composable
 *
 * @param photoId          Unique ID for this audio item (used for playback tracking).
 * @param audioFilePath    Absolute path to the audio file.
 * @param dateText         Formatted date string.
 * @param pitchText        Formatted pitch string (e.g. "Pitch: 180 Hz").
 * @param formantsText     Formatted formants string (e.g. "F1: 700 Hz | F2: 1800 Hz").
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
    pitchText: String,
    formantsText: String,
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
        AndroidView(
            factory = { ctx ->
                ImageButton(ctx).apply {
                    setBackgroundResource(R.drawable.rounded_transparent_button)
                    contentDescription = ctx.getString(R.string.play_audio)
                    setOnClickListener { onPlayPause() }
                }
            },
            update = { button ->
                button.setImageResource(
                    if (isPlaying) android.R.drawable.ic_media_pause
                    else android.R.drawable.ic_media_play
                )
            },
            modifier = Modifier.size(56.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = pitchText,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = formantsText,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp)
            )
            AndroidView(
                factory = { ctx -> WaveformView(ctx) },
                update = { waveformView ->
                    val file = File(audioFilePath)
                    if (file.exists()) {
                        waveformView.setAudioFile(file)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(top = 8.dp, bottom = 8.dp, end = 8.dp)
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
