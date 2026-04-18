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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme

/**
 * Displays a date-group heading row inside the Gallery RecyclerView.
 *
 * Migrated from `res/layout/gallery_adapter_title_item.xml`:
 * - Root was a single `TextView` with `layout_width="match_parent"`, `layout_height="wrap_content"`
 * - Padding mapped to Compose `Modifier.padding(start=8dp, top=8dp, end=8dp, bottom=4dp)`
 * - `textAppearance="@style/TextAppearance.AppCompat.Title"` → `MaterialTheme.typography.titleLarge`
 * - `textColor="@color/white_text_selector"` is now resolved from `MaterialTheme.colorScheme.primary`
 *   so the Compose island visibly follows the selected app palette.
 *
 * @param title The formatted date string to display (e.g. "10/09/2018 | 20 Days").
 * @param modifier Optional [Modifier] for layout-level customisation by the caller.
 */
@Composable
fun GalleryDateTitleItem(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 4.dp),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleLarge
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF4DB8FF)
@Composable
private fun GalleryDateTitleItemPreview() {
    OpenTransitionTheme {
        GalleryDateTitleItem(title = "10/09/2018 | 20 Days")
    }
}
