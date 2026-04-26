/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.selectphoto.singlealbum

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.shelbeely.opentransition.R
import java.io.FileNotFoundException

/**
 * Grid cell for a photo item in the single-album screen.
 *
 * Replaces `res/layout/single_album_adapter_image_item.xml`:
 * - Root `SquareConstraintLayout` is owned by the ViewHolder, not this composable.
 * - `ImageView` (match_parent × match_parent, CENTER_CROP) loaded via Picasso from [uri].
 *   If loading raises [FileNotFoundException], [onFileNotFound] is invoked so the caller
 *   can remove the stale entry from the gallery and refresh the adapter.
 * - Selection indicator (top-end) shown only when [selectionMode] is true.
 *
 * @param uri             Content URI of the image.
 * @param isSelected      Whether this item is currently selected.
 * @param selectionMode   Whether the adapter is in multi-select mode.
 * @param onClick         Called on single tap.
 * @param onLongClick     Called on long press.
 * @param onFileNotFound  Called when Picasso raises a [FileNotFoundException].
 * @param modifier        Optional [Modifier] for the caller.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SingleAlbumImageItem(
    uri: Uri,
    isSelected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFileNotFound: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        SubcomposeAsyncImage(
            model = uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        ) {
            when (val s = painter.state) {
                is AsyncImagePainter.State.Error -> {
                    if (s.result.throwable is FileNotFoundException) {
                        onFileNotFound()
                    }
                }
                else -> SubcomposeAsyncImageContent()
            }
        }

        if (selectionMode) {
            Image(
                painter = painterResource(
                    if (isSelected) R.drawable.ic_selected_primary_36dp
                    else R.drawable.ic_unselected_primary_36dp
                ),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(36.dp)
            )
        }
    }
}
