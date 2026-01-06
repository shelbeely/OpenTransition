/*
 * Copyright © 2025 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.shortcuts

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.database.room.entities.MilestoneEntity
import com.shelbeely.opentransition.database.room.entities.PhotoEntity
import com.shelbeely.opentransition.ui.MainActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manager for app shortcuts.
 * Handles creation and management of dynamic shortcuts for recent content.
 */
object ShortcutManagerUtil {
    private const val MAX_DYNAMIC_SHORTCUTS = 3
    
    /**
     * Update dynamic shortcuts with recent photos.
     */
    fun updateRecentPhotos(context: Context, recentPhotos: List<PhotoEntity>) {
        val shortcuts = recentPhotos
            .take(MAX_DYNAMIC_SHORTCUTS)
            .mapIndexed { index, photo ->
                val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(Date(photo.timestamp))
                
                val typeName = when (photo.type) {
                    0 -> "Face"
                    1 -> "Body"
                    2 -> "Audio"
                    else -> "Photo"
                }
                
                val intent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    putExtra("shortcut_action", "view_photo")
                    putExtra("photo_id", photo.id)
                }
                
                ShortcutInfoCompat.Builder(context, "recent_photo_${photo.id}")
                    .setShortLabel("$typeName $date")
                    .setLongLabel("$typeName photo from $date")
                    .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_camera))
                    .setIntent(intent)
                    .setRank(index)
                    .build()
            }
        
        ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
    }
    
    /**
     * Update dynamic shortcuts with recent milestones.
     */
    fun updateRecentMilestones(context: Context, recentMilestones: List<MilestoneEntity>) {
        val shortcuts = recentMilestones
            .take(MAX_DYNAMIC_SHORTCUTS)
            .mapIndexed { index, milestone ->
                val intent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    putExtra("shortcut_action", "view_milestone")
                    putExtra("milestone_id", milestone.id)
                }
                
                ShortcutInfoCompat.Builder(context, "recent_milestone_${milestone.id}")
                    .setShortLabel(milestone.title.take(20))
                    .setLongLabel(milestone.title)
                    .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_milestones))
                    .setIntent(intent)
                    .setRank(index)
                    .build()
            }
        
        ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
    }
    
    /**
     * Report shortcut usage to boost its ranking.
     */
    fun reportShortcutUsed(context: Context, shortcutId: String) {
        ShortcutManagerCompat.reportShortcutUsed(context, shortcutId)
    }
    
    /**
     * Remove all dynamic shortcuts.
     */
    fun removeAllDynamicShortcuts(context: Context) {
        ShortcutManagerCompat.removeAllDynamicShortcuts(context)
    }
}
