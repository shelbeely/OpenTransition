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

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme
import com.shelbeely.opentransition.ui.widget.AdapterSpanSizeLookup
import com.shelbeely.opentransition.ui.widget.CursorRecyclerViewAdapter
import com.shelbeely.opentransition.ui.widget.SquareConstraintLayout
import com.shelbeely.opentransition.util.FileUtil
import com.shelbeely.opentransition.util.settings.SettingsManager
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import java.lang.ref.WeakReference

class SingleAlbumAdapter(
    context: Context, bucketId: String
) : CursorRecyclerViewAdapter<SingleAlbumAdapter.BaseHolder>(
    getSingleAlbumCursor(context, bucketId)
), AdapterSpanSizeLookup.Interface {
    private val eventRelay: PublishRelay<SingleAlbumUiEvent> = PublishRelay.create()
    val events: Observable<SingleAlbumUiEvent> = eventRelay

    var selectionMode: Boolean = false
        set(value) {
            val didChange = field != value
            field = value

            if (!field) {
                selectedUris.clear()
            }

            if (didChange) {
                notifyDataSetChanged()
            }
        }

    private var selectedUris = ArrayList<Uri>()

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            (itemCount - 1) -> TYPE_COUNT

            else -> TYPE_IMAGE
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1 //The last item is the photo count
    }

    override fun getSpanSize(position: Int): Int = when (position) {
        itemCount - 1 -> SingleAlbumView.GRID_SPAN
        else -> 1
    }

    fun getItemPosition(uri: Uri): Int {
        val localCursor = cursor ?: return RecyclerView.NO_POSITION

        for (i in 0 until localCursor.count) {
            if (uri.path == getUri(i)?.path) {
                return i
            }
        }

        return RecyclerView.NO_POSITION
    }

    fun getSelectedUris(): ArrayList<Uri> {
        return ArrayList(selectedUris)
    }

    fun getUri(position: Int): Uri? {
        val localCursor = cursor ?: return null

        if (!localCursor.moveToPosition(position)) {
            throw IllegalStateException("couldn't move cursor to position $position")
        }

        return getUri(localCursor)
    }

    private fun getUri(cursor: Cursor): Uri {
        val idIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
        if (idIndex < 0) {
            throw IllegalStateException("couldn't find the column index of 'MediaStore.Images.Media._ID'")
        }
        val id = cursor.getLong(idIndex)
        return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder {
        return when (viewType) {
            TYPE_COUNT -> {
                val composeView = ComposeView(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setViewCompositionStrategy(
                        ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
                    )
                }
                CountHolder(composeView)
            }

            else -> {
                val context = parent.context
                val density = context.resources.displayMetrics.density
                val margin = (2 * density).toInt()

                val composeView = ComposeView(context).apply {
                    layoutParams = ConstraintLayout.LayoutParams(0, 0).apply {
                        startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                        topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                        endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                        bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                    }
                    setViewCompositionStrategy(
                        ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
                    )
                }

                val itemView = SquareConstraintLayout(context).apply {
                    layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(margin, margin, margin, margin)
                    }
                    orientation = 1
                    addView(composeView)
                }

                ImageHolder(itemView, composeView, this)
            }
        }
    }

    override fun onBindViewHolder(viewHolder: BaseHolder, position: Int) {
        when (viewHolder) {
            is ImageHolder -> {
                //Will call the bellow bind function with the correct cursor position
                super.onBindViewHolder(viewHolder, position)
            }

            is CountHolder -> {
                //Calling the super to avoid including the count view being counted
                viewHolder.bind(super.getItemCount())
            }
        }
    }

    override fun onBindViewHolder(viewHolder: BaseHolder, cursor: Cursor) {
        when (viewHolder) {
            is ImageHolder -> {
                val uri = getUri(cursor)
                viewHolder.bind(uri, selectionMode, selectedUris.contains(uri))
            }
        }
    }

    fun updateSelectedUris(newSelectedUris: ArrayList<Uri>) {
        selectedUris = newSelectedUris
        notifyDataSetChanged()
    }

    fun refreshCountItem() {
        notifyItemChanged(itemCount - 1)
    }

    open class BaseHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class ImageHolder(
        itemView: View,
        private val composeView: ComposeView,
        creatingAdapter: SingleAlbumAdapter
    ) : BaseHolder(itemView) {
        private val adapterRef = WeakReference(creatingAdapter)

        fun bind(uri: Uri, selectionMode: Boolean, isSelected: Boolean) {
            val adapter = adapterRef.get() ?: return
            composeView.setContent {
                OpenTransitionTheme(
                    colorVariant = SettingsManager.getResolvedComposeColorVariant()
                ) {
                    SingleAlbumImageItem(
                        uri = uri,
                        isSelected = isSelected,
                        selectionMode = selectionMode,
                        onClick = {
                            val event: SingleAlbumUiEvent = when (adapter.selectionMode) {
                                true -> {
                                    if (adapter.selectedUris.contains(uri)) {
                                        adapter.selectedUris.remove(uri)
                                    } else {
                                        adapter.selectedUris.add(uri)
                                    }
                                    SingleAlbumUiEvent.SelectionUpdate(adapter.getSelectedUris())
                                }
                                false -> SingleAlbumUiEvent.SelectPhoto(uri)
                            }
                            adapter.eventRelay.accept(event)
                        },
                        onLongClick = {
                            if (!adapter.selectionMode) {
                                adapter.eventRelay.accept(
                                    SingleAlbumUiEvent.SelectionUpdate(arrayListOf(uri))
                                )
                            } else {
                                itemView.performClick()
                            }
                        },
                        onFileNotFound = {
                            uri.path?.let { FileUtil.removeImageFromGallery(it) }
                            adapter.notifyDataSetChanged()
                        }
                    )
                }
            }
        }
    }

    class CountHolder(private val composeView: ComposeView) : BaseHolder(composeView) {
        fun bind(count: Int) {
            composeView.setContent {
                OpenTransitionTheme(
                    colorVariant = SettingsManager.getResolvedComposeColorVariant()
                ) {
                    SingleAlbumPhotoCountItem(count = count)
                }
            }
        }
    }

    companion object {
        private const val TYPE_IMAGE = 0
        private const val TYPE_COUNT = 1

        fun getSingleAlbumCursor(context: Context, bucketId: String): Cursor {
            return context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Images.Media._ID),
                "${MediaStore.Images.Media.BUCKET_ID}=?",
                arrayOf(bucketId),
                MediaStore.Images.Media._ID + " DESC"
            )!!
        }
    }
}
