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

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.shelbeely.opentransition.R

/**
 * Arvo is delivered via the Google Fonts provider (res/font/arvo.xml).
 * We reference it through [Font] with the XML resource so Compose picks up the
 * downloadable font at runtime; a system-serif fallback is used while the font
 * loads or on offline devices.
 */
val ArvoFontFamily = FontFamily(
    Font(R.font.arvo, weight = FontWeight.Normal),
    Font(R.font.arvo, weight = FontWeight.Bold),
)

/**
 * Compose Typography translated from styles_typography.xml, following the
 * M3 Expressive editorial hierarchy:
 *  - Display/Headline roles use Bold for maximum visual impact
 *  - Body roles use Normal weight for comfortable reading
 *  - Label roles use Bold with tight tracking for UI chrome
 */
val AppTypography = Typography(
    // 64sp bold, −0.02em tracking — hero day-counter moment
    displayLarge = TextStyle(
        fontFamily = ArvoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 64.sp,
        lineHeight = 72.sp,
        letterSpacing = (-0.02).em,
    ),
    // 52sp bold
    displayMedium = TextStyle(
        fontFamily = ArvoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 52.sp,
        lineHeight = 60.sp,
        letterSpacing = (-0.01).em,
    ),
    displaySmall = TextStyle(
        fontFamily = ArvoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.em,
    ),
    // 36sp bold — section headers with editorial impact
    headlineLarge = TextStyle(
        fontFamily = ArvoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.em,
    ),
    headlineMedium = TextStyle(
        fontFamily = ArvoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.em,
    ),
    headlineSmall = TextStyle(
        fontFamily = ArvoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.em,
    ),
    // 22sp bold — emphasized UI elements
    titleLarge = TextStyle(
        fontFamily = ArvoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.em,
    ),
    titleMedium = TextStyle(
        fontFamily = ArvoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.01.em,
    ),
    titleSmall = TextStyle(
        fontFamily = ArvoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.01.em,
    ),
    // 18sp normal — comfortable body reading with extra line height
    bodyLarge = TextStyle(
        fontFamily = ArvoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 26.sp,   // +4sp line height from XML
        letterSpacing = 0.em,
    ),
    bodyMedium = TextStyle(
        fontFamily = ArvoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,   // +2sp line height from XML
        letterSpacing = 0.em,
    ),
    bodySmall = TextStyle(
        fontFamily = ArvoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.em,
    ),
    // 16sp bold, +0.02em tracking — button text and UI labels
    labelLarge = TextStyle(
        fontFamily = ArvoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.02.em,
    ),
    labelMedium = TextStyle(
        fontFamily = ArvoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.01.em,
    ),
    labelSmall = TextStyle(
        fontFamily = ArvoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.01.em,
    ),
)
