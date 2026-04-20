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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme

@Composable
fun SelectPhotoAddItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentDescription = stringResource(R.string.take_photo)
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { this.contentDescription = contentDescription }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add_circle_white_48dp),
            contentDescription = null,
            tint = Color.Unspecified
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF4DB8FF)
@Composable
private fun SelectPhotoAddItemPreview() {
    OpenTransitionTheme {
        SelectPhotoAddItem(onClick = {})
    }
}
