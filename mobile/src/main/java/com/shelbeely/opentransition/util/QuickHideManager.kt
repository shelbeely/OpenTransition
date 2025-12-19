/*
 * Copyright © 2023-2025 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.util

import android.app.Activity
import android.view.WindowManager
import com.shelbeely.opentransition.util.settings.SettingsManager

/**
 * Provides quick hide functionality to instantly hide sensitive content
 * Replaces UI with a generic screen when triggered
 */
object QuickHideManager {
    
    /**
     * Enable secure screen to prevent screenshots and screen recording
     */
    fun enableSecureMode(activity: Activity) {
        if (SettingsManager.isQuickHideEnabled()) {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
    }
    
    /**
     * Disable secure mode
     */
    fun disableSecureMode(activity: Activity) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
    
    /**
     * Check if quick hide is triggered
     * This could be extended to detect specific gestures or button combinations
     */
    fun isQuickHideTriggered(): Boolean {
        // This is a placeholder - could be extended to detect:
        // - Volume button combinations
        // - Shake gestures
        // - Quick app switching
        return false
    }
    
    /**
     * Apply quick hide - show generic content
     * This should be called from the main activity when quick hide is triggered
     */
    fun applyQuickHide(activity: Activity) {
        if (!SettingsManager.isQuickHideEnabled()) {
            return
        }
        
        // In a real implementation, this would:
        // 1. Clear the current screen
        // 2. Show a generic cover screen (e.g., a calculator, notepad, or blank screen)
        // 3. Prevent back navigation to sensitive content
        
        // For now, we just minimize the app
        activity.moveTaskToBack(true)
    }
}
