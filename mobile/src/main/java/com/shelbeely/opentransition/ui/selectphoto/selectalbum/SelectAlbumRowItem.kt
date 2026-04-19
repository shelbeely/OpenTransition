/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.selectphoto.selectalbum

import android.widget.ImageView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.ui.selectphoto.selectalbum.SelectAlbumAdapter.Album
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme
import com.shelbeely.opentransition.ui.widget.SquareImageView
import com.squareup.picasso.Picasso

@Composable
fun SelectAlbumRowItem(
    album: Album,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = colorResource(R.color.white)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 2.dp, end = 16.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AndroidView(
            factory = { context ->
                SquareImageView(context).apply {
                    orientation = 1
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
            },
            update = { imageView ->
                Picasso.get()
                    .load(album.uri)
                    .fit()
                    .centerCrop()
                    .into(imageView)
            },
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = album.name,
            modifier = Modifier.weight(1f),
            color = textColor,
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = album.count.toString(),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF4DB8FF)
@Composable
private fun SelectAlbumRowItemPreview() {
    OpenTransitionTheme {
        SelectAlbumRowItem(
            album = Album(
                bucketId = "preview",
                uri = android.net.Uri.EMPTY,
                name = "Camera",
                count = 256
            ),
            onClick = {}
        )
    }
}
