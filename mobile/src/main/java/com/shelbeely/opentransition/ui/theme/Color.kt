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

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// Raw tonal palette seeds (derived from colors.xml hex values)
// ─────────────────────────────────────────────────────────────────────────────

// Pink
private val Pink10 = Color(0xFF3D0026)
private val Pink20 = Color(0xFF6B0043)
private val Pink40 = Color(0xFFE665B8)   // pinkColorPrimaryDark
private val Pink50 = Color(0xFFFF85D4)   // pinkColorPrimary
private val Pink80 = Color(0xFFFFB3E5)   // pinkColorPrimaryLight
private val Pink90 = Color(0xFFFFD6F3)
private val PinkAccent40 = Color(0xFF70C8FF) // pinkColorAccent
private val PinkAccent80 = Color(0xFFBFE8FF)

// Blue
private val Blue10 = Color(0xFF001D33)
private val Blue20 = Color(0xFF003A5C)
private val Blue40 = Color(0xFF2A9FE6)   // blueColorPrimaryDark
private val Blue50 = Color(0xFF4DB8FF)   // blueColorPrimary
private val Blue80 = Color(0xFF85CDFF)   // blueColorPrimaryLight
private val Blue90 = Color(0xFFBFE4FF)
private val BlueAccent40 = Color(0xFFE665B8)
private val BlueAccent80 = Color(0xFFFFB3E5)

// Purple
private val Purple10 = Color(0xFF1F0066)
private val Purple20 = Color(0xFF3B00AD)
private val Purple40 = Color(0xFF8956E0)  // purpleColorPrimaryDark
private val Purple50 = Color(0xFFA875FF)  // purpleColorPrimary
private val Purple80 = Color(0xFFC7A3FF)  // purpleColorPrimaryLight
private val Purple90 = Color(0xFFE3D0FF)
private val PurpleAccent40 = Color(0xFF70C8FF)
private val PurpleAccent80 = Color(0xFFBFE8FF)

// Green
private val Green10 = Color(0xFF00391A)
private val Green20 = Color(0xFF00522A)
private val Green40 = Color(0xFF1AB759)   // greenColorPrimaryDark
private val Green50 = Color(0xFF2DD36F)   // greenColorPrimary
private val Green80 = Color(0xFF5FE694)   // greenColorPrimaryLight
private val Green90 = Color(0xFFB7F5CE)
private val GreenAccent40 = Color(0xFF4DB8FF)
private val GreenAccent80 = Color(0xFFBFE4FF)

// Neutral surfaces (shared across all palettes)
private val Neutral10 = Color(0xFF1C1B1F)
private val Neutral20 = Color(0xFF313033)
private val Neutral90 = Color(0xFFE6E1E5)
private val Neutral95 = Color(0xFFF4EFF4)
private val Neutral99 = Color(0xFFFFFBFE)

// ─────────────────────────────────────────────────────────────────────────────
// Pink colour scheme
// ─────────────────────────────────────────────────────────────────────────────

val PinkLightColorScheme = lightColorScheme(
    primary = Pink40,
    onPrimary = Color.White,
    primaryContainer = Pink80,
    onPrimaryContainer = Pink10,
    secondary = PinkAccent40,
    onSecondary = Color.White,
    secondaryContainer = PinkAccent80,
    onSecondaryContainer = Blue10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = Neutral95,
    onSurfaceVariant = Neutral20,
    outline = Color(0xFF79747E),
)

val PinkDarkColorScheme = darkColorScheme(
    primary = Pink80,
    onPrimary = Pink20,
    primaryContainer = Pink40,
    onPrimaryContainer = Pink90,
    secondary = PinkAccent80,
    onSecondary = Blue20,
    secondaryContainer = PinkAccent40,
    onSecondaryContainer = PinkAccent80,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = Neutral20,
    onSurfaceVariant = Neutral90,
    outline = Color(0xFF938F99),
)

// ─────────────────────────────────────────────────────────────────────────────
// Blue colour scheme
// ─────────────────────────────────────────────────────────────────────────────

val BlueLightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = Color.White,
    primaryContainer = Blue80,
    onPrimaryContainer = Blue10,
    secondary = BlueAccent40,
    onSecondary = Color.White,
    secondaryContainer = BlueAccent80,
    onSecondaryContainer = Pink10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = Neutral95,
    onSurfaceVariant = Neutral20,
    outline = Color(0xFF79747E),
)

val BlueDarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Blue20,
    primaryContainer = Blue40,
    onPrimaryContainer = Blue90,
    secondary = BlueAccent80,
    onSecondary = Pink20,
    secondaryContainer = BlueAccent40,
    onSecondaryContainer = BlueAccent80,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = Neutral20,
    onSurfaceVariant = Neutral90,
    outline = Color(0xFF938F99),
)

// ─────────────────────────────────────────────────────────────────────────────
// Purple colour scheme
// ─────────────────────────────────────────────────────────────────────────────

val PurpleLightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Color.White,
    primaryContainer = Purple80,
    onPrimaryContainer = Purple10,
    secondary = PurpleAccent40,
    onSecondary = Color.White,
    secondaryContainer = PurpleAccent80,
    onSecondaryContainer = Blue10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = Neutral95,
    onSurfaceVariant = Neutral20,
    outline = Color(0xFF79747E),
)

val PurpleDarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = Purple20,
    primaryContainer = Purple40,
    onPrimaryContainer = Purple90,
    secondary = PurpleAccent80,
    onSecondary = Blue20,
    secondaryContainer = PurpleAccent40,
    onSecondaryContainer = PurpleAccent80,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = Neutral20,
    onSurfaceVariant = Neutral90,
    outline = Color(0xFF938F99),
)

// ─────────────────────────────────────────────────────────────────────────────
// Green colour scheme
// ─────────────────────────────────────────────────────────────────────────────

val GreenLightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    primaryContainer = Green80,
    onPrimaryContainer = Green10,
    secondary = GreenAccent40,
    onSecondary = Color.White,
    secondaryContainer = GreenAccent80,
    onSecondaryContainer = Blue10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = Neutral95,
    onSurfaceVariant = Neutral20,
    outline = Color(0xFF79747E),
)

val GreenDarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = Green20,
    primaryContainer = Green40,
    onPrimaryContainer = Green90,
    secondary = GreenAccent80,
    onSecondary = Blue20,
    secondaryContainer = GreenAccent40,
    onSecondaryContainer = GreenAccent80,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = Neutral20,
    onSurfaceVariant = Neutral90,
    outline = Color(0xFF938F99),
)
