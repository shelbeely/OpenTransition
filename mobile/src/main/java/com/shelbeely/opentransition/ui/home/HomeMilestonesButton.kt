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

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme

@Composable
fun HomeMilestonesButton(
    hasMilestones: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stateDescription = stringResource(
        if (hasMilestones) R.string.selected else R.string.not_selected
    )
    val iconRes = if (hasMilestones) {
        R.drawable.ic_milestone_selected
    } else {
        R.drawable.ic_milestone_unselected
    }

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .semantics { this.stateDescription = stateDescription }
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = stringResource(R.string.milestones),
            tint = Color.Unspecified,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF4DB8FF)
@Composable
private fun HomeMilestonesButtonSelectedPreview() {
    OpenTransitionTheme {
        HomeMilestonesButton(
            hasMilestones = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF4DB8FF)
@Composable
private fun HomeMilestonesButtonUnselectedPreview() {
    OpenTransitionTheme {
        HomeMilestonesButton(
            hasMilestones = false,
            onClick = {}
        )
    }
}
