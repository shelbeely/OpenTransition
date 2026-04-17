/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Minimal Compose theme wrapper for OpenTransition. Delegates to [MaterialTheme] so that
 * composables can use Material3 typography tokens while the app's full visual theming
 * continues to be applied via the XML [BaseAppTheme] on the host Activity/Fragment.
 *
 * As more screens are migrated to Compose the color scheme and typography can be
 * populated here to match the XML theme.
 */
@Composable
fun OpenTransitionTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
