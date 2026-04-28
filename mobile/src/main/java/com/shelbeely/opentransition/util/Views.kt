/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.NonNull
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.shelbeely.opentransition.R

@ColorInt
fun View.getColor(@ColorRes colorRes: Int): Int = context.getColor(colorRes)

fun View.getIdName() = resources.getResourceEntryName(id)

fun View.getString(@StringRes resId: Int) = context.getString(resId)

fun View.getString(@StringRes resId: Int, @NonNull vararg formatArgs: Any) =
    context.getString(resId, *formatArgs)

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.visible() {
    visibility = View.VISIBLE
}

@Suppress("LiftReturnOrAssignment") //Lifting it out wouldn't look as clean
fun View.setVisibleOrGone(show: Boolean) = when (show) {
    true -> visibility = View.VISIBLE
    false -> visibility = View.GONE
}

@Suppress("LiftReturnOrAssignment") //Lifting it out wouldn't look as clean
fun View.setVisibleOrInvisible(show: Boolean) = when (show) {
    true -> visibility = View.VISIBLE
    false -> visibility = View.INVISIBLE
}

fun View.showKeyboard() {
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        .showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.hideKeyboard() {
    (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
        .hideSoftInputFromWindow(windowToken, 0)
}

fun setVisible(vararg views: View) = views.forEach { it.visibility = View.VISIBLE }

fun setGone(vararg views: View) = views.forEach { it.visibility = View.GONE }

/**
 * Apply window insets to add padding for system bars (status bar, navigation bar).
 * This ensures content is not covered by system UI elements.
 * 
 * @param left Whether to apply left insets (default: false)
 * @param top Whether to apply top insets (default: true)
 * @param right Whether to apply right insets (default: false)
 * @param bottom Whether to apply bottom insets (default: true)
 * @param applyPadding Whether to apply insets as padding (default: true)
 */
fun View.applySystemBarInsets(
    left: Boolean = false,
    top: Boolean = true,
    right: Boolean = false,
    bottom: Boolean = true,
    applyPadding: Boolean = true
) {
    // Store initial padding in view tag if not already stored
    val initialPadding = getTag(R.id.initial_padding_tag) as? InitialPadding
        ?: recordInitialPadding().also { setTag(R.id.initial_padding_tag, it) }
    
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        
        if (applyPadding) {
            view.updatePadding(
                left = initialPadding.left + if (left) insets.left else 0,
                top = initialPadding.top + if (top) insets.top else 0,
                right = initialPadding.right + if (right) insets.right else 0,
                bottom = initialPadding.bottom + if (bottom) insets.bottom else 0
            )
        }
        
        windowInsets
    }
    
    // Request that insets be dispatched
    ViewCompat.requestApplyInsets(this)
}

/**
 * Record the initial padding of a view before applying insets.
 * This allows us to preserve existing padding when adding system bar insets.
 */
private fun View.recordInitialPadding(): InitialPadding {
    return InitialPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
}

/**
 * Data class to store initial padding values.
 */
private data class InitialPadding(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)
