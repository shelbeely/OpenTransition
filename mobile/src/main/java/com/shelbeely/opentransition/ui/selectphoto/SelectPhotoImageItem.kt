/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.selectphoto

import android.net.Uri
import android.widget.ImageView
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.shelbeely.opentransition.R
import com.squareup.picasso.Picasso

/**
 * Grid cell for a photo item in the select-photo screen.
 *
 * Replaces `res/layout/select_photo_adapter_item.xml`:
 * - Root `SquareConstraintLayout` is owned by the ViewHolder, not this composable.
 * - `ImageView` (match_parent × match_parent, CENTER_CROP) loaded via Picasso from [uri].
 * - Selection indicator (top-end) shown only when [selectionMode] is true.
 *
 * @param uri           Content URI of the image.
 * @param isSelected    Whether this item is currently selected.
 * @param selectionMode Whether the adapter is in multi-select mode.
 * @param onClick       Called on single tap.
 * @param onLongClick   Called on long press.
 * @param modifier      Optional [Modifier] for the caller.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectPhotoImageItem(
    uri: Uri,
    isSelected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        AndroidView(
            factory = { ctx ->
                ImageView(ctx).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
            },
            update = { imageView ->
                Picasso.get()
                    .load(uri)
                    .fit()
                    .centerCrop()
                    .into(imageView)
            },
            modifier = Modifier.fillMaxSize()
        )

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
