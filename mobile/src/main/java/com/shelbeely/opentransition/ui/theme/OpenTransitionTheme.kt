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

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.shelbeely.opentransition.util.settings.AppColorVariant

/**
 * [CompositionLocal] that provides the resolved [ColorScheme] to all descendants,
 * including any XML-hosted [androidx.compose.ui.platform.ComposeView] islands that
 * wrap this theme.  Defaults to a minimal fallback; always consumed through
 * [MaterialTheme.colorScheme] inside Compose UI.
 */
val LocalAppColorScheme = staticCompositionLocalOf<ColorScheme> {
    PinkLightColorScheme
}

/**
 * Full M3 Expressive theme for OpenTransition.
 *
 * Color selection priority:
 *  1. [AppColorVariant.dynamic] on API 31+ → Material You wallpaper colours.
 *  2. [AppColorVariant.dynamic] on API < 31 → falls back to [AppColorVariant.pink].
 *  3. Any other [AppColorVariant] → corresponding hand-crafted tonal palette.
 *
 * @param colorVariant   Which colour palette to use (read from [SettingsManager] by the caller).
 * @param darkTheme      Whether to apply the dark variant; defaults to system setting.
 * @param content        Composable subtree that inherits this theme.
 */
@Composable
fun OpenTransitionTheme(
    colorVariant: AppColorVariant = AppColorVariant.dynamic,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    val colorScheme: ColorScheme = when {
        colorVariant == AppColorVariant.dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // dynamic requested but API < 31 — fall through to pink palette
        colorVariant == AppColorVariant.dynamic || colorVariant == AppColorVariant.pink -> {
            if (darkTheme) PinkDarkColorScheme else PinkLightColorScheme
        }
        colorVariant == AppColorVariant.blue -> {
            if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
        }
        colorVariant == AppColorVariant.purple -> {
            if (darkTheme) PurpleDarkColorScheme else PurpleLightColorScheme
        }
        else -> { // green
            if (darkTheme) GreenDarkColorScheme else GreenLightColorScheme
        }
    }

    CompositionLocalProvider(LocalAppColorScheme provides colorScheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

