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

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * M3 Expressive organic/blob shape system translated from styles_shapes.xml.
 *
 * Shapes are systematic — not mixed arbitrarily per screen:
 *  - [Shapes.extraSmall]  → 4dp uniform corners         — inputs, badges
 *  - [Shapes.small]       → 50% pill                    — chips, date buttons
 *  - [Shapes.medium]      → asymmetric organic (24/8)   — cards mid-level
 *  - [Shapes.large]       → asymmetric organic (32/16)  — elevated cards, sheets
 *  - [Shapes.extraLarge]  → blob (48/32/36/44)          — hero cards, FABs
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(percent = 50),          // pill — PillSmall
    medium = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 8.dp,
        bottomStart = 8.dp,
        bottomEnd = 24.dp,
    ),
    large = RoundedCornerShape(
        topStart = 32.dp,
        topEnd = 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 32.dp,
    ),
    extraLarge = RoundedCornerShape(
        topStart = 48.dp,
        topEnd = 32.dp,
        bottomStart = 36.dp,
        bottomEnd = 44.dp,
    ),
)
