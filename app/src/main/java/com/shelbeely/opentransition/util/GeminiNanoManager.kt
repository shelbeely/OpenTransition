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

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.genai.imagedescription.ImageDescription
import com.google.mlkit.genai.imagedescription.ImageDescriptionOptions
import com.google.mlkit.genai.proofreading.Proofreading
import com.google.mlkit.genai.proofreading.ProofreadingOptions
import com.google.mlkit.genai.rewriting.Rewriting
import com.google.mlkit.genai.rewriting.RewritingOptions
import com.google.mlkit.genai.rewriting.RewritingStyle
import kotlinx.coroutines.tasks.await

/**
 * Manager class for Gemini Nano on-device AI operations.
 * Provides text enhancement and image description capabilities using ML Kit GenAI.
 */
class GeminiNanoManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    
    companion object {
        private const val TAG = "GeminiNanoManager"
        
        @Volatile
        private var instance: GeminiNanoManager? = null
        
        fun getInstance(context: Context): GeminiNanoManager {
            return instance ?: synchronized(this) {
                instance ?: GeminiNanoManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Check if image description is available on this device
     */
    suspend fun isImageDescriptionAvailable(): Boolean {
        return try {
            val client = ImageDescription.getClient(
                ImageDescriptionOptions.builder().build()
            )
            client.isAvailable().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking image description availability", e)
            false
        }
    }
    
    /**
     * Check if rewriting is available on this device
     */
    suspend fun isRewritingAvailable(): Boolean {
        return try {
            val client = Rewriting.getClient(
                RewritingOptions.builder().build()
            )
            client.isAvailable().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking rewriting availability", e)
            false
        }
    }
    
    /**
     * Check if proofreading is available on this device
     */
    suspend fun isProofreadingAvailable(): Boolean {
        return try {
            val client = Proofreading.getClient(
                ProofreadingOptions.builder().build()
            )
            client.isAvailable().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking proofreading availability", e)
            false
        }
    }
    
    /**
     * Generate a description for an image to improve accessibility and organization
     */
    suspend fun describeImage(bitmap: Bitmap): Result<String> {
        return try {
            val client = ImageDescription.getClient(
                ImageDescriptionOptions.builder().build()
            )
            
            if (!client.isAvailable().await()) {
                return Result.failure(Exception("Image description not available on this device"))
            }
            
            val description = client.process(bitmap).await()
            
            if (description.isNotEmpty()) {
                Result.success(description)
            } else {
                Result.failure(Exception("No description generated"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error describing image", e)
            Result.failure(e)
        }
    }
    
    /**
     * Rewrite text in a more casual and friendly style
     */
    suspend fun rewriteCasual(text: String): Result<String> {
        return rewriteText(text, RewritingStyle.CASUAL)
    }
    
    /**
     * Rewrite text in a more formal and professional style
     */
    suspend fun rewriteFormal(text: String): Result<String> {
        return rewriteText(text, RewritingStyle.FORMAL)
    }
    
    /**
     * Rewrite text to be more concise
     */
    suspend fun rewriteShorter(text: String): Result<String> {
        return rewriteText(text, RewritingStyle.SHORTER)
    }
    
    /**
     * Rewrite text to be more detailed
     */
    suspend fun rewriteLonger(text: String): Result<String> {
        return rewriteText(text, RewritingStyle.LONGER)
    }
    
    /**
     * Rewrite text with the specified style
     */
    private suspend fun rewriteText(text: String, style: RewritingStyle): Result<String> {
        return try {
            val client = Rewriting.getClient(
                RewritingOptions.builder().setStyle(style).build()
            )
            
            if (!client.isAvailable().await()) {
                return Result.failure(Exception("Rewriting not available on this device"))
            }
            
            val rewrittenText = client.process(text).await()
            
            if (rewrittenText.isNotEmpty()) {
                Result.success(rewrittenText)
            } else {
                Result.failure(Exception("No rewritten text generated"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error rewriting text with style $style", e)
            Result.failure(e)
        }
    }
    
    /**
     * Proofread text to correct grammar and spelling errors
     */
    suspend fun proofread(text: String): Result<String> {
        return try {
            val client = Proofreading.getClient(
                ProofreadingOptions.builder().build()
            )
            
            if (!client.isAvailable().await()) {
                return Result.failure(Exception("Proofreading not available on this device"))
            }
            
            val proofreadText = client.process(text).await()
            
            if (proofreadText.isNotEmpty()) {
                Result.success(proofreadText)
            } else {
                Result.failure(Exception("No proofread text generated"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error proofreading text", e)
            Result.failure(e)
        }
    }
}
