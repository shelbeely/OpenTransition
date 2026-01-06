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

import android.content.Context
import androidx.appsearch.app.AppSearchSession
import androidx.appsearch.app.PutDocumentsRequest
import androidx.appsearch.app.RemoveByDocumentIdRequest
import androidx.appsearch.app.SearchSpec
import androidx.appsearch.app.SetSchemaRequest
import androidx.appsearch.localstorage.LocalStorage
import com.shelbeely.opentransition.database.room.entities.MilestoneEntity
import com.shelbeely.opentransition.database.room.entities.PhotoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.withContext

/**
 * Manager for AppSearch operations.
 * Handles indexing of photos and milestones for system-wide search.
 */
class AppSearchManager private constructor(private val context: Context) {
    private var session: AppSearchSession? = null
    
    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            if (session != null) return@withContext
            
            val sessionFuture = LocalStorage.createSearchSessionAsync(
                LocalStorage.SearchContext.Builder(context, "opentransition_db")
                    .build()
            )
            
            val newSession = sessionFuture.await()
            
            // Set schema
            val setSchemaRequest = SetSchemaRequest.Builder()
                .addDocumentClasses(PhotoDocument::class.java, MilestoneDocument::class.java)
                .setForceOverride(false)
                .build()
            
            newSession.setSchemaAsync(setSchemaRequest).await()
            session = newSession
        }
    }
    
    suspend fun indexPhoto(entity: PhotoEntity, typeName: String) {
        withContext(Dispatchers.IO) {
            val session = ensureSession()
            val document = PhotoDocument.fromEntity(entity, typeName)
            
            val request = PutDocumentsRequest.Builder()
                .addDocuments(document)
                .build()
            
            session.putAsync(request).await()
        }
    }
    
    suspend fun indexMilestone(entity: MilestoneEntity) {
        withContext(Dispatchers.IO) {
            val session = ensureSession()
            val document = MilestoneDocument.fromEntity(entity)
            
            val request = PutDocumentsRequest.Builder()
                .addDocuments(document)
                .build()
            
            session.putAsync(request).await()
        }
    }
    
    suspend fun removePhoto(id: String) {
        withContext(Dispatchers.IO) {
            val session = ensureSession()
            val request = RemoveByDocumentIdRequest.Builder(PhotoDocument.NAMESPACE)
                .addIds(id)
                .build()
            
            session.removeAsync(request).await()
        }
    }
    
    suspend fun removeMilestone(id: String) {
        withContext(Dispatchers.IO) {
            val session = ensureSession()
            val request = RemoveByDocumentIdRequest.Builder(MilestoneDocument.NAMESPACE)
                .addIds(id)
                .build()
            
            session.removeAsync(request).await()
        }
    }
    
    suspend fun searchAll(query: String): List<SearchResult> {
        return withContext(Dispatchers.IO) {
            val session = ensureSession()
            val searchSpec = SearchSpec.Builder()
                .setRankingStrategy(SearchSpec.RANKING_STRATEGY_RELEVANCE_SCORE)
                .setResultCountPerPage(20)
                .build()
            
            val searchResults = session.search(query, searchSpec).await()
            val results = mutableListOf<SearchResult>()
            
            while (searchResults.hasNext()) {
                val result = searchResults.next().await()
                val document = result.genericDocument
                
                when (document.namespace) {
                    PhotoDocument.NAMESPACE -> {
                        results.add(SearchResult.Photo(
                            id = document.id,
                            typeName = document.getPropertyString("typeName") ?: "",
                            timestamp = document.getPropertyLong("timestamp")
                        ))
                    }
                    MilestoneDocument.NAMESPACE -> {
                        results.add(SearchResult.Milestone(
                            id = document.id,
                            title = document.getPropertyString("title") ?: "",
                            timestamp = document.getPropertyLong("timestamp")
                        ))
                    }
                }
            }
            
            results
        }
    }
    
    private suspend fun ensureSession(): AppSearchSession {
        if (session == null) {
            initialize()
        }
        return session ?: throw IllegalStateException("Failed to initialize AppSearch session")
    }
    
    suspend fun close() {
        withContext(Dispatchers.IO) {
            session?.close()
            session = null
        }
    }
    
    sealed class SearchResult {
        data class Photo(val id: String, val typeName: String, val timestamp: Long) : SearchResult()
        data class Milestone(val id: String, val title: String, val timestamp: Long) : SearchResult()
    }
    
    companion object {
        @Volatile
        private var instance: AppSearchManager? = null
        
        fun getInstance(context: Context): AppSearchManager {
            return instance ?: synchronized(this) {
                instance ?: AppSearchManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
