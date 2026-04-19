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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.data.Photo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    state: GalleryUiState,
    isEmpty: Boolean,
    onBack: () -> Unit,
    onAddPhoto: () -> Unit,
    recyclerView: RecyclerView,
) {
    val type = GalleryUiState.getType(state)
    val titleRes = when (type) {
        Photo.TYPE_FACE -> R.string.face_gallery
        Photo.TYPE_BODY -> R.string.body_gallery
        Photo.TYPE_AUDIO -> R.string.audio_gallery
        else -> R.string.face_gallery
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(titleRes),
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAddPhoto) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add_white_24dp),
                            contentDescription = stringResource(R.string.add)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AndroidView(
                factory = { recyclerView },
                modifier = Modifier.fillMaxSize()
            )
            if (isEmpty) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                ) {
                    Text(
                        text = stringResource(R.string.gallery_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                    IconButton(onClick = onAddPhoto) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add_circle_white_48dp),
                            contentDescription = stringResource(R.string.add)
                        )
                    }
                }
            }
        }
    }
}
