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

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.database.getStringOrNull
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.ui.selectphoto.selectalbum.SelectAlbumAdapter.Album
import com.shelbeely.opentransition.ui.selectphoto.selectalbum.SelectAlbumAdapter.Holder
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable

class SelectAlbumAdapter() : ListAdapter<Album, Holder>(DiffCallback) {
    private val itemClickRelay: PublishRelay<String> = PublishRelay.create<String>()
    val itemClick: Observable<String> = itemClickRelay

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val composeView = ComposeView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
            )
        }
        return Holder(composeView, itemClickRelay)
    }

    override fun onBindViewHolder(viewHolder: Holder, position: Int) {
        viewHolder.bind(getItem(position))
    }

    fun fetchData(context: Context) {
        val folders = LinkedHashMap<String, Album>()
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
            ),
            null,
            null,
            "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} ASC"
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                val nameIndex = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

                do {
                    val id = cursor.getLong(idIndex)
                    val bucketId = cursor.getString(bucketIdIndex)

                    val album = folders[bucketId]
                        ?: Album(
                            bucketId = bucketId,
                            uri = ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                id
                            ),
                            name = cursor.getStringOrNull(nameIndex)
                                ?: context.getString(R.string.untitled_album),
                            count = 0
                        )
                    album.count += 1

                    folders[bucketId] = album
                } while (cursor.moveToNext())
            }
        }
        submitList(folders.values.toList())
    }

    class Holder(
        private val composeView: ComposeView,
        private val itemClickRelay: PublishRelay<String>
    ) : RecyclerView.ViewHolder(composeView) {
        fun bind(album: Album) {
            composeView.setContent {
                OpenTransitionTheme {
                    SelectAlbumRowItem(
                        album = album,
                        onClick = { itemClickRelay.accept(album.bucketId) }
                    )
                }
            }
        }
    }

    data class Album(val bucketId: String, val uri: Uri, val name: String, var count: Int)

    private object DiffCallback : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean =
            oldItem.bucketId == newItem.bucketId

        override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean =
            oldItem == newItem
    }
}
