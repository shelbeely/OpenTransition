/*
 * Copyright © 2018-2023 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.util

import android.view.View
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

/**
 * Material Design 3 Expressive Spring Animation Utilities
 * 
 * M3 Expressive uses spring-based physics animations with overshoot and bounce
 * instead of traditional easing curves. This creates a more playful, energetic,
 * and emotionally engaging interface.
 */

/**
 * Extension function to animate a View property using spring physics
 * 
 * @param property The property to animate (SCALE_X, SCALE_Y, TRANSLATION_X, etc.)
 * @param targetValue The target value for the property
 * @param stiffness How tight the spring is (higher = faster, less overshoot)
 * @param dampingRatio How much the spring bounces (lower = more bounce)
 */
fun View.animateWithSpring(
    property: DynamicAnimation.ViewProperty,
    targetValue: Float,
    stiffness: Float = SpringForce.STIFFNESS_MEDIUM,
    dampingRatio: Float = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
): SpringAnimation {
    return SpringAnimation(this, property, targetValue).apply {
        spring.stiffness = stiffness
        spring.dampingRatio = dampingRatio
    }.also { it.start() }
}

/**
 * Add bouncy click behavior to a View with spring animation
 * Following M3 Expressive "Big and Bouncy" principle
 * 
 * @param onClick The action to perform after the bounce animation
 */
fun View.bounceOnClick(onClick: () -> Unit) {
    setOnClickListener {
        // Scale down
        scaleX = 0.92f
        scaleY = 0.92f
        
        // Spring back with overshoot
        animateWithSpring(
            DynamicAnimation.SCALE_X,
            1f,
            SpringForce.STIFFNESS_LOW,
            SpringForce.DAMPING_RATIO_LOW_BOUNCY
        )
        
        animateWithSpring(
            DynamicAnimation.SCALE_Y,
            1f,
            SpringForce.STIFFNESS_LOW,
            SpringForce.DAMPING_RATIO_LOW_BOUNCY
        )
        
        // Perform action after a short delay to let animation start
        postDelayed({ onClick() }, 50)
    }
}

/**
 * Add spring-based elevation effect on press
 * Creates a subtle bounce that makes the UI feel more physical
 * 
 * @param targetElevation The elevation to animate to (in dp)
 */
fun View.springElevationOnPress(targetElevation: Float = 8f) {
    setOnTouchListener { view, event ->
        when (event.action) {
            android.view.MotionEvent.ACTION_DOWN -> {
                view.animateWithSpring(
                    DynamicAnimation.Z,
                    targetElevation,
                    SpringForce.STIFFNESS_MEDIUM,
                    SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                )
            }
            android.view.MotionEvent.ACTION_UP,
            android.view.MotionEvent.ACTION_CANCEL -> {
                view.animateWithSpring(
                    DynamicAnimation.Z,
                    0f,
                    SpringForce.STIFFNESS_MEDIUM,
                    SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                )
            }
        }
        false // Don't consume the event
    }
}

/**
 * Spring-based focus animation for interactive elements
 * Creates a subtle scale-up effect with overshoot
 */
fun View.springOnFocus() {
    setOnFocusChangeListener { view, hasFocus ->
        val targetScale = if (hasFocus) 1.05f else 1f
        
        view.animateWithSpring(
            DynamicAnimation.SCALE_X,
            targetScale,
            SpringForce.STIFFNESS_MEDIUM,
            SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
        )
        
        view.animateWithSpring(
            DynamicAnimation.SCALE_Y,
            targetScale,
            SpringForce.STIFFNESS_MEDIUM,
            SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
        )
    }
}

/**
 * Spring reveal animation - element springs into view
 * Perfect for hero moments and emphasizing important content
 */
fun View.springReveal(delay: Long = 0) {
    alpha = 0f
    scaleX = 0.8f
    scaleY = 0.8f
    
    postDelayed({
        animate().alpha(1f).setDuration(300).start()
        
        animateWithSpring(
            DynamicAnimation.SCALE_X,
            1f,
            SpringForce.STIFFNESS_LOW,
            SpringForce.DAMPING_RATIO_LOW_BOUNCY
        )
        
        animateWithSpring(
            DynamicAnimation.SCALE_Y,
            1f,
            SpringForce.STIFFNESS_LOW,
            SpringForce.DAMPING_RATIO_LOW_BOUNCY
        )
    }, delay)
}
