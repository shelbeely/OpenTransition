/*
 * Copyright © 2025 OpenTransition. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.util.security

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.shelbeely.opentransition.util.settings.LockDelay
import com.shelbeely.opentransition.util.settings.LockType
import com.shelbeely.opentransition.util.settings.SettingsManager
import java.lang.ref.WeakReference

/**
 * Manages quick hiding of app content for security.
 * Provides instant content hiding and auto-lock when app is backgrounded.
 */
object QuickHideManager : DefaultLifecycleObserver {
    
    private var activityRef: WeakReference<Activity>? = null
    private var hideOverlay: View? = null
    private var lastPausedTime: Long = 0
    private var isInitialized = false
    
    /**
     * Initializes the QuickHideManager with lifecycle observation.
     */
    fun initialize() {
        if (!isInitialized) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
            isInitialized = true
        }
    }
    
    /**
     * Registers an activity for quick hide functionality.
     */
    fun registerActivity(activity: Activity) {
        activityRef = WeakReference(activity)
    }
    
    /**
     * Unregisters the current activity.
     */
    fun unregisterActivity() {
        hideOverlay = null
        activityRef = null
    }
    
    /**
     * Shows the hide overlay immediately.
     */
    fun showHideOverlay() {
        val activity = activityRef?.get() ?: return
        
        // Create or show overlay
        if (hideOverlay == null) {
            hideOverlay = createHideOverlay(activity)
            val rootView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
            rootView.addView(hideOverlay)
        } else {
            hideOverlay?.visibility = View.VISIBLE
        }
    }
    
    /**
     * Hides the hide overlay.
     */
    fun hideHideOverlay() {
        hideOverlay?.visibility = View.GONE
    }
    
    /**
     * Removes the hide overlay completely.
     */
    fun removeHideOverlay() {
        hideOverlay?.let { overlay ->
            val parent = overlay.parent as? ViewGroup
            parent?.removeView(overlay)
        }
        hideOverlay = null
    }
    
    /**
     * Creates the hide overlay view that shows when content is hidden.
     * This shows a generic/innocuous screen to protect privacy.
     */
    private fun createHideOverlay(activity: Activity): View {
        return View(activity).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(0xFFFFFFFF.toInt()) // White background
            elevation = 100f // Ensure it's on top
            isClickable = true
            isFocusable = true
        }
    }
    
    /**
     * Checks if the app should show lock screen based on time elapsed since last pause.
     */
    fun shouldShowLock(): Boolean {
        if (SettingsManager.getLockType() == LockType.off) {
            return false
        }
        
        val lockDelay = SettingsManager.getLockDelay()
        val delayMillis = lockDelay.getMilli()
        val timeSinceLastPause = System.currentTimeMillis() - lastPausedTime
        
        return timeSinceLastPause >= delayMillis
    }
    
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        // App came to foreground
        hideHideOverlay()
    }
    
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // App went to background
        lastPausedTime = System.currentTimeMillis()
        
        // Auto-lock when backgrounded if lock is enabled
        if (SettingsManager.getLockType() != LockType.off) {
            showHideOverlay()
        }
    }
}
