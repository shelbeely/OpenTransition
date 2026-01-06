/*
 * Copyright © 2025 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.appsearch

import androidx.appsearch.annotation.Document
import androidx.appsearch.annotation.Document.Id
import androidx.appsearch.annotation.Document.Namespace
import androidx.appsearch.annotation.Document.Score
import androidx.appsearch.annotation.Document.StringProperty
import androidx.appsearch.annotation.Document.LongProperty
import com.shelbeely.opentransition.database.room.entities.MilestoneEntity

/**
 * AppSearch document for Milestone entities.
 * Enables system-wide search for milestones by title and description.
 */
@Document
data class MilestoneDocument(
    @Namespace
    val namespace: String,
    
    @Id
    val id: String,
    
    @Score
    val score: Int,
    
    @LongProperty
    val timestamp: Long,
    
    @LongProperty
    val epochDay: Long,
    
    @StringProperty(indexingType = StringProperty.INDEXING_TYPE_PREFIXES)
    val title: String,
    
    @StringProperty(indexingType = StringProperty.INDEXING_TYPE_PREFIXES)
    val description: String
) {
    companion object {
        const val NAMESPACE = "milestones"
        
        fun fromEntity(entity: MilestoneEntity): MilestoneDocument {
            return MilestoneDocument(
                namespace = NAMESPACE,
                id = entity.id,
                score = 2, // Higher score for milestones as they're more important
                timestamp = entity.timestamp,
                epochDay = entity.epochDay,
                title = entity.title,
                description = entity.description
            )
        }
    }
}
