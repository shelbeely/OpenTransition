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

import android.widget.ImageButton
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.ui.widget.WaveformView
import java.io.File

@Composable
fun HomeGalleryAudioItem(
    photoId: String,
    audioFilePath: String,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(
            factory = { context ->
                ImageButton(context).apply {
                    setBackgroundResource(R.drawable.rounded_transparent_button)
                    contentDescription = context.getString(R.string.play_audio)
                    setOnClickListener { onPlayPause() }
                }
            },
            update = { button ->
                button.setImageResource(
                    if (isPlaying) android.R.drawable.ic_media_pause
                    else android.R.drawable.ic_media_play
                )
            },
            modifier = Modifier
                .size(40.dp)
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
        AndroidView(
            factory = { context -> WaveformView(context) },
            update = { waveformView ->
                val file = File(audioFilePath)
                if (file.exists()) {
                    waveformView.setAudioFile(file)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        )
    }
}
