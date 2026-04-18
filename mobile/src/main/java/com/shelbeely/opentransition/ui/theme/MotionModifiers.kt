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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

/**
 * M3 Expressive bouncy press interaction.
 *
 * Applies a spring-based scale-down to 0.93 on press and springs back on release.
 * Uses [MotionTokens.BounceSpring] so the scale-back has a playful overshoot.
 *
 * There is intentionally no [androidx.compose.foundation.Indication] ripple so the
 * spring scale *is* the visual feedback; callers should still provide a semantic
 * [androidx.compose.ui.semantics.Role] where appropriate.
 *
 * Replaces [com.shelbeely.opentransition.util.SpringAnimationUtil.bounceOnClick] for
 * all new Compose screens.
 *
 * @param enabled    Whether the interaction is active.
 * @param onClick    Callback invoked on tap-up.
 */
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = MotionTokens.BounceSpring.dampingRatio,
            stiffness = MotionTokens.BounceSpring.stiffness,
        ),
        label = "bouncyClickableScale",
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(enabled, onClick) {
            if (!enabled) return@pointerInput
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                },
                onTap = { onClick() },
            )
        }
}

/**
 * Reveals content with a spring-based scale + fade entrance animation.
 *
 * Wraps [content] in an [AnimatedVisibility] that triggers on first composition,
 * using [MotionTokens.BounceSpring] for the scale and [MotionTokens.SoftSpring] for
 * the fade. Designed for hero moments — day counter, empty-state illustrations, app logo.
 *
 * @param visible   Controls whether to show the content (default `true` triggers on first
 *                  composition).
 */
@Composable
fun SpringReveal(
    visible: Boolean = true,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = MotionTokens.BounceSpring.dampingRatio,
                stiffness = MotionTokens.BounceSpring.stiffness,
            ),
            initialScale = 0.85f,
        ) + fadeIn(
            animationSpec = spring(
                dampingRatio = MotionTokens.SoftSpring.dampingRatio,
                stiffness = MotionTokens.SoftSpring.stiffness,
            ),
        ),
    ) {
        content()
    }
}

/**
 * Convenience variant that auto-triggers [SpringReveal] on the first composition.
 *
 * Internally holds a remembered `visible` flag that flips to `true` after the initial frame,
 * ensuring the animation always plays once when the composable enters the composition.
 */
@Composable
fun AutoSpringReveal(content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    SpringReveal(visible = visible, content = content)
}
