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
import com.shelbeely.opentransition.database.room.entities.PhotoEntity

/**
 * AppSearch document for Photo entities.
 * Enables system-wide search for photos by type and date.
 */
@Document
data class PhotoDocument(
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
    
    @StringProperty
    val type: String,
    
    @StringProperty
    val typeName: String,
    
    @StringProperty
    val filePath: String
) {
    companion object {
        const val NAMESPACE = "photos"
        
        fun fromEntity(entity: PhotoEntity, typeName: String): PhotoDocument {
            return PhotoDocument(
                namespace = NAMESPACE,
                id = entity.id,
                score = 1,
                timestamp = entity.timestamp,
                epochDay = entity.epochDay,
                type = entity.type.toString(),
                typeName = typeName,
                filePath = entity.filePath
            )
        }
    }
}
