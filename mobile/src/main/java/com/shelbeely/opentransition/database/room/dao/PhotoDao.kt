/*
 * Copyright © 2023-2025 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.database.room.dao

import androidx.room.*
import com.shelbeely.opentransition.database.room.entities.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos ORDER BY epochDay DESC, timestamp DESC")
    fun getAllPhotos(): Flow<List<PhotoEntity>>
    
    @Query("SELECT * FROM photos WHERE type = :type ORDER BY epochDay DESC, timestamp DESC")
    fun getPhotosByType(type: Int): Flow<List<PhotoEntity>>
    
    @Query("SELECT * FROM photos WHERE id = :id")
    suspend fun getPhotoById(id: String): PhotoEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)
    
    @Update
    suspend fun updatePhoto(photo: PhotoEntity)
    
    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)
    
    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deletePhotoById(id: String)
    
    @Query("DELETE FROM photos")
    suspend fun deleteAllPhotos()
}
