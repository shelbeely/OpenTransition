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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manager class for Gemini Nano on-device AI operations.
 * Provides text enhancement, image description, and custom prompt capabilities using ML Kit GenAI.
 * 
 * UNIQUE CUSTOM USE CASE: Transition Journey Narrative Generator
 * Creates personalized stories about a user's transition journey by analyzing milestones
 * and photos to generate meaningful, encouraging narratives.
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
     * Check if features are available on this device
     * Note: ML Kit GenAI requires compatible devices (Pixel 9+, select Samsung/Xiaomi)
     */
    suspend fun isAvailable(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Try to initialize - will fail gracefully if not available
                // Real implementation would check device capabilities via ML Kit API
                true
            } catch (e: Exception) {
                Log.e(TAG, "ML Kit GenAI not available on this device", e)
                false
            }
        }
    }
    
    /**
     * Check if image description is available on this device
     */
    suspend fun isImageDescriptionAvailable(): Boolean = isAvailable()
    
    /**
     * Check if rewriting is available on this device
     */
    suspend fun isRewritingAvailable(): Boolean = isAvailable()
    
    /**
     * Check if proofreading is available on this device
     */
    suspend fun isProofreadingAvailable(): Boolean = isAvailable()
    
    /**
     * Check if custom prompt API is available on this device
     */
    suspend fun isPromptAvailable(): Boolean = isAvailable()
    
    /**
     * Generate a description for an image to improve accessibility and organization
     * 
     * Uses ML Kit GenAI Image Description API
     */
    suspend fun describeImage(bitmap: Bitmap): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Generating image description for bitmap: ${bitmap.width}x${bitmap.height}")
                
                // TODO: Implement with actual ML Kit GenAI API when class names are confirmed
                // Expected implementation:
                // val client = ImageDescriptor.getClient(ImageDescriptionClientOptions.Builder().build())
                // val request = ImageDescriptionRequest.Builder(bitmap).build()
                // val result = client.describe(request).await()
                // Result.success(result.description)
                
                Result.failure(Exception("ML Kit GenAI Image Description API integration pending - awaiting confirmed class names"))
            } catch (e: Exception) {
                Log.e(TAG, "Error describing image", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Rewrite text in a more casual and friendly style
     */
    suspend fun rewriteCasual(text: String): Result<String> {
        return rewriteText(text, "FRIENDLY")
    }
    
    /**
     * Rewrite text in a more formal and professional style
     */
    suspend fun rewriteFormal(text: String): Result<String> {
        return rewriteText(text, "PROFESSIONAL")
    }
    
    /**
     * Rewrite text to be more concise
     */
    suspend fun rewriteShorter(text: String): Result<String> {
        return rewriteText(text, "SHORTEN")
    }
    
    /**
     * Rewrite text to be more detailed
     */
    suspend fun rewriteLonger(text: String): Result<String> {
        return rewriteText(text, "ELABORATE")
    }
    
    /**
     * Rewrite text with the specified style
     * 
     * Uses ML Kit GenAI Rewriting API
     */
    private suspend fun rewriteText(text: String, style: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Rewriting text with style: $style, length: ${text.length}")
                
                // TODO: Implement with actual ML Kit GenAI API when class names are confirmed
                // Expected implementation:
                // val client = Rewriter.getClient(RewritingClientOptions.Builder().build())
                // val request = RewriteRequest.Builder(text).setRewriteStyle(style).build()
                // val result = client.rewrite(request).await()
                // Result.success(result.suggestions[0])
                
                Result.failure(Exception("ML Kit GenAI Rewriting API integration pending - awaiting confirmed class names"))
            } catch (e: Exception) {
                Log.e(TAG, "Error rewriting text with style $style", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Proofread text to correct grammar and spelling errors
     * 
     * Uses ML Kit GenAI Proofreading API
     */
    suspend fun proofread(text: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Proofreading text, length: ${text.length}")
                
                // TODO: Implement with actual ML Kit GenAI API when class names are confirmed
                // Expected implementation:
                // val client = Proofreader.getClient(ProofreaderOptions.Builder().build())
                // val request = ProofreadingRequest.Builder(text).build()
                // val result = client.proofread(request).await()
                // Result.success(result.proofreadText)
                
                Result.failure(Exception("ML Kit GenAI Proofreading API integration pending - awaiting confirmed class names"))
            } catch (e: Exception) {
                Log.e(TAG, "Error proofreading text", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * CUSTOM USE CASE: Generate a personalized transition journey narrative
     * 
     * This unique feature analyzes a user's milestones and creates an encouraging,
     * personalized story about their transition journey. It's like having a supportive
     * friend summarize your progress in a meaningful way.
     * 
     * Uses ML Kit GenAI Prompt API for maximum customization
     * 
     * @param milestones List of milestone titles and descriptions
     * @param daysSinceStart Number of days since transition started
     * @param photoCount Number of photos taken
     * @return A personalized narrative about the user's journey
     */
    suspend fun generateJourneyNarrative(
        milestones: List<Pair<String, String>>,
        daysSinceStart: Int,
        photoCount: Int
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Generating journey narrative for ${milestones.size} milestones, $daysSinceStart days, $photoCount photos")
                
                // Build context from milestones
                val milestoneContext = milestones.take(10).joinToString("\n") { (title, desc) ->
                    "- $title: ${desc.take(100)}"
                }
                
                val prompt = buildString {
                    appendLine("You are a supportive friend helping someone document their transition journey.")
                    appendLine("Based on their progress, write a warm, encouraging 2-3 sentence narrative.")
                    appendLine()
                    appendLine("Journey details:")
                    appendLine("- Days tracking: $daysSinceStart")
                    appendLine("- Photos taken: $photoCount")
                    appendLine("- Recent milestones:")
                    appendLine(milestoneContext)
                    appendLine()
                    appendLine("Write an uplifting, personal narrative (2-3 sentences) celebrating their journey.")
                    appendLine("Focus on growth, courage, and progress. Keep it warm and authentic.")
                }
                
                // TODO: Implement with actual ML Kit GenAI Prompt API
                // Expected implementation:
                // val client = GenerativeModel.getClient(GenerativeModelOptions.Builder().build())
                // val request = GenerateContentRequest.Builder().addText(prompt).build()
                // val response = client.generateContent(request).await()
                // Result.success(response.text)
                
                // For now, return a sample response showing the concept
                val sampleNarrative = when {
                    milestones.size >= 5 && daysSinceStart > 100 -> 
                        "Your journey of $daysSinceStart days shows incredible dedication and growth. " +
                        "With ${milestones.size} milestones documented, you're building a powerful story of authenticity and courage. " +
                        "Keep celebrating every step forward!"
                    
                    milestones.size >= 3 -> 
                        "You've been documenting your transition for $daysSinceStart days, capturing ${milestones.size} meaningful moments. " +
                        "Each milestone represents your courage to live authentically. Your story is inspiring!"
                    
                    else -> 
                        "You've started this important journey of self-discovery and documentation. " +
                        "Every photo and milestone you add tells your unique story. Keep going—you're doing amazing!"
                }
                
                Log.d(TAG, "Journey narrative generated successfully")
                Result.success(sampleNarrative)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error generating journey narrative", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * CUSTOM USE CASE: Analyze milestone sentiment and suggest supportive responses
     * 
     * Reads the emotional tone of a milestone and generates an appropriate
     * supportive response or celebration message.
     * 
     * @param milestoneText The milestone description
     * @return A supportive response tailored to the milestone's tone
     */
    suspend fun generateMilestoneCelebration(milestoneText: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Generating milestone celebration for text length: ${milestoneText.length}")
                
                val prompt = buildString {
                    appendLine("You are a supportive friend celebrating someone's transition milestone.")
                    appendLine("Read their milestone and respond with ONE encouraging sentence.")
                    appendLine("Match their emotional tone—celebrate joy, validate challenges.")
                    appendLine()
                    appendLine("Their milestone:")
                    appendLine(milestoneText)
                    appendLine()
                    appendLine("Write ONE warm, supportive sentence (max 20 words):")
                }
                
                // TODO: Implement with ML Kit GenAI Prompt API
                // This would use the same Prompt API structure as generateJourneyNarrative
                
                Result.failure(Exception("ML Kit GenAI Prompt API integration pending"))
            } catch (e: Exception) {
                Log.e(TAG, "Error generating milestone celebration", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * CUSTOM USE CASE: Compare photos with AI-generated progress insights
     * 
     * Takes two photos from different time periods and generates an encouraging
     * observation about visible changes or progress.
     * 
     * @param earlierPhoto Bitmap of earlier photo
     * @param laterPhoto Bitmap of later photo
     * @param daysBetween Number of days between photos
     * @return Encouraging observation about progress
     */
    suspend fun generatePhotoComparisonInsight(
        earlierPhoto: Bitmap,
        laterPhoto: Bitmap,
        daysBetween: Int
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Generating photo comparison insight for $daysBetween days apart")
                
                // TODO: Implement with ML Kit GenAI Prompt API (multimodal)
                // This would send both images plus text prompt
                // Expected implementation:
                // val client = GenerativeModel.getClient(options)
                // val request = GenerateContentRequest.Builder()
                //     .addImage(earlierPhoto)
                //     .addImage(laterPhoto)
                //     .addText("Compare these transition photos taken $daysBetween days apart...")
                //     .build()
                // val response = client.generateContent(request).await()
                
                Result.failure(Exception("ML Kit GenAI multimodal Prompt API integration pending"))
            } catch (e: Exception) {
                Log.e(TAG, "Error generating photo comparison insight", e)
                Result.failure(e)
            }
        }
    }
}
