/*
 * Copyright © 2025 OpenTransition. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.wear.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Typography

// ─────────────────────────────────────────────────────────────────────────────
// Wear colour palettes — mirror the mobile static palettes at Wear-appropriate
// roles.  No FloatingToolbar / ButtonGroup — Wear-specific components only.
// ─────────────────────────────────────────────────────────────────────────────

private val WearPinkColors = Colors(
    primary = Color(0xFFFF85D4),          // pinkColorPrimary
    primaryVariant = Color(0xFFE665B8),    // pinkColorPrimaryDark
    secondary = Color(0xFF70C8FF),         // pinkColorAccent
    secondaryVariant = Color(0xFF70C8FF),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF313033),
    error = Color(0xFFCF6679),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color(0xFFE6E1E5),
    onError = Color.Black,
)

private val WearBlueColors = Colors(
    primary = Color(0xFF4DB8FF),           // blueColorPrimary
    primaryVariant = Color(0xFF2A9FE6),    // blueColorPrimaryDark
    secondary = Color(0xFFFF85D4),         // blueColorAccent → pink
    secondaryVariant = Color(0xFFFF85D4),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF313033),
    error = Color(0xFFCF6679),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color(0xFFE6E1E5),
    onError = Color.Black,
)

private val WearPurpleColors = Colors(
    primary = Color(0xFFA875FF),           // purpleColorPrimary
    primaryVariant = Color(0xFF8956E0),    // purpleColorPrimaryDark
    secondary = Color(0xFF70C8FF),         // purpleColorAccent
    secondaryVariant = Color(0xFF70C8FF),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF313033),
    error = Color(0xFFCF6679),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color(0xFFE6E1E5),
    onError = Color.Black,
)

private val WearGreenColors = Colors(
    primary = Color(0xFF2DD36F),           // greenColorPrimary
    primaryVariant = Color(0xFF1AB759),    // greenColorPrimaryDark
    secondary = Color(0xFF4DB8FF),         // greenColorAccent → blue
    secondaryVariant = Color(0xFF4DB8FF),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF313033),
    error = Color(0xFFCF6679),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color(0xFFE6E1E5),
    onError = Color.Black,
)

// ─────────────────────────────────────────────────────────────────────────────
// Wear typography — condensed scale appropriate for small round/square displays.
// Based on the Arvo editorial scale from the mobile app, but reduced for Wear.
// ─────────────────────────────────────────────────────────────────────────────

private val WearTypography = Typography(
    // Hero moment — day counter (equivalent to mobile DisplayLarge, reduced for wrist)
    display1 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        letterSpacing = (-0.02).sp,
    ),
    display2 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
    ),
    display3 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
    ),
    title1 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
    ),
    title2 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
    ),
    title3 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
    ),
    body1 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp,
    ),
    body2 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    button = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        letterSpacing = 0.02.sp,
    ),
    caption1 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
    ),
    caption2 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
    ),
)

/**
 * OpenTransition Wear OS theme.
 *
 * Mirrors the mobile colour system but adapts it for the Wear OS component set
 * ([androidx.wear.compose.material]).  No [FloatingToolbar], [ButtonGroup], or
 * [WideNavigationRail] — instead use Wear-appropriate alternatives such as
 * [androidx.wear.compose.material.SwipeToDismissBox] and
 * [androidx.wear.compose.foundation.CurvedText].
 *
 * @param colorVariant  Which static palette to apply (string name matches mobile
 *                      [com.shelbeely.opentransition.util.settings.AppColorVariant]).
 *                      Dynamic/Material You is not available on Wear OS — defaults to pink.
 * @param content       The composable content.
 */
@Composable
fun WearTheme(
    colorVariant: WearColorVariant = WearColorVariant.Pink,
    content: @Composable () -> Unit,
) {
    val colors = when (colorVariant) {
        WearColorVariant.Pink -> WearPinkColors
        WearColorVariant.Blue -> WearBlueColors
        WearColorVariant.Purple -> WearPurpleColors
        WearColorVariant.Green -> WearGreenColors
    }

    MaterialTheme(
        colors = colors,
        typography = WearTypography,
        content = content,
    )
}

/**
 * Colour variant selector for [WearTheme].
 *
 * Maps to the mobile [com.shelbeely.opentransition.util.settings.AppColorVariant]
 * (excluding `Dynamic` since Material You is not available on Wear OS).
 */
enum class WearColorVariant { Pink, Blue, Purple, Green }
