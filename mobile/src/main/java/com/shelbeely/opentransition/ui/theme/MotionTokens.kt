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

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * Named spring presets aligned with M3 Expressive motion guidance.
 *
 * All motion in OpenTransition is purposeful and uses one of these tokens rather than
 * ad-hoc animation specs, ensuring visual coherence across screens.
 *
 * Usage pattern:
 * ```kotlin
 * animateFloatAsState(targetValue = scale, animationSpec = MotionTokens.BounceSpring)
 * ```
 */
object MotionTokens {

    /**
     * Subtle emphasis — used for hover states, soft scale-ups, and gentle reveals.
     *
     * Damping 0.8 (very slightly under-damped) with medium-low stiffness gives a
     * barely-perceptible overshoot that reads as "alive" without distracting.
     */
    val SoftSpring: SpringSpec<Float> = spring(
        dampingRatio = 0.8f,
        stiffness = 200f,
    )

    /**
     * Playful bounce — used for FABs, primary CTAs, and the Add-media toolbar.
     *
     * Low stiffness + low-bouncy damping produces the joyful spring that M3 Expressive
     * associates with key interactive moments.
     */
    val BounceSpring: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow,
    )

    /**
     * Crisp, no-nonsense — used for navigation transitions and structural layout changes.
     *
     * No bounce + medium stiffness means the transition completes quickly and precisely,
     * communicating hierarchy without theatrical flair.
     */
    val CrispSpring: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )
}
