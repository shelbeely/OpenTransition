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

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme
import com.shelbeely.opentransition.ui.widget.SquareConstraintLayout
import com.shelbeely.opentransition.ui.widget.CursorRecyclerViewAdapter
import com.shelbeely.opentransition.util.getString
import com.shelbeely.opentransition.util.setVisibleOrGone
import com.jakewharton.rxrelay3.PublishRelay
import com.squareup.picasso.Picasso
import io.reactivex.rxjava3.core.Observable
import kotterknife.bindView
import java.lang.ref.WeakReference

class SelectPhotoAdapter(
    context: Context
) : CursorRecyclerViewAdapter<SelectPhotoAdapter.BaseHolder>(getGalleryCursor(context)) {

    private val eventRelay: PublishRelay<SelectPhotoUiEvent> = PublishRelay.create()
    val events: Observable<SelectPhotoUiEvent> = eventRelay

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

    override fun getItemCount(): Int = super.getItemCount() + 1

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> TYPE_ADD
        else -> TYPE_IMAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder {
        return when (viewType) {
            TYPE_ADD -> {
                val context = parent.context
                val density = context.resources.displayMetrics.density
                val margin = (2 * density).toInt()
                val padding = (16 * density).toInt()

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
                    setPadding(padding, padding, padding, padding)
                    addView(composeView)
                }

                TakePhotoHolder(itemView, composeView, eventRelay)
            }

            else -> ImageHolder(
                android.view.LayoutInflater.from(parent.context).inflate(
                    R.layout.select_photo_adapter_item, parent, false
                ),
                this
            )
        }
    }

    override fun onBindViewHolder(viewHolder: BaseHolder, position: Int) {
        if (position != 0) {
            //Adjust the bind call for the position ignoring the Take Photo item we have added
            super.onBindViewHolder(viewHolder, position - 1)
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

    fun updateSelectedUris(newSelectedUris: ArrayList<Uri>) {
        selectedUris = newSelectedUris
        notifyDataSetChanged()
    }

    open class BaseHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class TakePhotoHolder(
        itemView: View,
        private val composeView: ComposeView,
        itemClickRelay: PublishRelay<SelectPhotoUiEvent>
    ) : BaseHolder(itemView) {
        init {
            composeView.setContent {
                OpenTransitionTheme {
                    SelectPhotoAddItem(
                        onClick = { itemClickRelay.accept(SelectPhotoUiEvent.TakePhoto) }
                    )
                }
            }
        }
    }

    class ImageHolder(itemView: View, creatingAdapter: SelectPhotoAdapter?) : BaseHolder(itemView) {
        private val image: ImageView by bindView(R.id.select_photo_adapter_item_image)
        private val selection: ImageView by bindView(R.id.select_photo_adapter_item_selection)

        private val adapterRef = WeakReference(creatingAdapter)

        private var currentUri: Uri? = null

        init {
            //Avoiding subscription so we don't need to dispose it
            itemView.setOnClickListener {
                val uri = currentUri ?: return@setOnClickListener
                val adapter = adapterRef.get() ?: return@setOnClickListener

                val event: SelectPhotoUiEvent = when (adapter.selectionMode) {
                    true -> {
                        if (adapter.selectedUris.contains(uri)) {
                            adapter.selectedUris.remove(uri)
                        } else {
                            adapter.selectedUris.add(uri)
                        }

                        SelectPhotoUiEvent.SelectionUpdate(adapter.getSelectedUris())
                    }

                    false -> SelectPhotoUiEvent.PhotoSelected(uri)
                }

                adapter.eventRelay.accept(event)
            }

            itemView.setOnLongClickListener {
                val uri = currentUri ?: return@setOnLongClickListener false
                val adapter = adapterRef.get() ?: return@setOnLongClickListener false

                if (!adapter.selectionMode) {
                    adapter.eventRelay.accept(SelectPhotoUiEvent.SelectionUpdate(arrayListOf(uri)))
                } else {
                    itemView.performClick()
                }

                return@setOnLongClickListener true
            }
        }

        fun bind(uri: Uri, selectionMode: Boolean, isSelected: Boolean) {
            currentUri = uri
            Picasso.get()
                .load(uri)
                .fit()
                .centerCrop()
                .into(image)

            selection.setVisibleOrGone(selectionMode)

            if (selectionMode) {
                val selectionRes = when (isSelected) {
                    true -> R.drawable.ic_selected_primary_36dp
                    false -> R.drawable.ic_unselected_primary_36dp
                }
                selection.setImageResource(selectionRes)

                selection.contentDescription = when (isSelected) {
                    true -> itemView.getString(R.string.selected)
                    false -> itemView.getString(R.string.not_selected)
                }
            }
        }
    }

    companion object {
        private const val TYPE_ADD = 0
        private const val TYPE_IMAGE = 1

        fun getGalleryCursor(context: Context): Cursor {
            return context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Images.Media._ID),
                null,
                null,
                MediaStore.Images.Media._ID + " DESC"
            )!!
        }
    }
}
