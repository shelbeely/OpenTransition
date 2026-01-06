/*
 * Copyright © 2025 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.util

import android.content.Context
import com.shelbeely.opentransition.appsearch.AppSearchManager
import com.shelbeely.opentransition.database.room.entities.MilestoneEntity
import com.shelbeely.opentransition.database.room.entities.PhotoEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Utility for managing content indexing for Android's AppSearch.
 * Provides simple methods to index photos and milestones for system-wide search.
 */
object SearchIndexingUtil {
    /**
     * Index a photo for system-wide search.
     * This should be called whenever a photo is created or updated.
     */
    fun indexPhoto(context: Context, photo: PhotoEntity, typeName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppSearchManager.getInstance(context).indexPhoto(photo, typeName)
            } catch (e: Exception) {
                // Silent failure - indexing is optional functionality
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Index a milestone for system-wide search.
     * This should be called whenever a milestone is created or updated.
     */
    fun indexMilestone(context: Context, milestone: MilestoneEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppSearchManager.getInstance(context).indexMilestone(milestone)
            } catch (e: Exception) {
                // Silent failure - indexing is optional functionality
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Remove a photo from the search index.
     * This should be called when a photo is deleted.
     */
    fun removePhoto(context: Context, photoId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppSearchManager.getInstance(context).removePhoto(photoId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Remove a milestone from the search index.
     * This should be called when a milestone is deleted.
     */
    fun removeMilestone(context: Context, milestoneId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppSearchManager.getInstance(context).removeMilestone(milestoneId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
