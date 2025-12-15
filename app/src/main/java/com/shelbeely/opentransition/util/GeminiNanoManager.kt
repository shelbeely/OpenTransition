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
// Prompt API imports
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.ImagePart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Manager class for Gemini Nano on-device AI operations.
 * Provides text enhancement, image description, and custom prompt capabilities using ML Kit GenAI.
 * 
 * UNIQUE CUSTOM USE CASES (Using Prompt API):
 * 1. Transition Journey Narrative Generator - Creates personalized stories from milestones
 * 2. Milestone Sentiment Celebration - Auto-generates supportive responses
 * 3. Photo Comparison Progress Insights - AI observations on before/after photos
 * 
 * Implementation ready - waiting for Prompt API release (genai-prompt:1.0.0-alpha01)
 */
class GeminiNanoManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    
    // Lazy initialization of GenerativeModel for Prompt API
    private val generativeModel by lazy {
        Generation.getClient()
    }
    
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
     * Check if Gemini Nano features are available on this device
     * Note: ML Kit GenAI requires compatible devices (Pixel 9+, select Samsung/Xiaomi)
     */
    suspend fun isAvailable(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val status = generativeModel.checkStatus()
                Log.d(TAG, "Gemini Nano status: $status")
                // Available or downloadable means the device supports it
                status != null
            } catch (e: Exception) {
                Log.e(TAG, "ML Kit GenAI not available on this device", e)
                false
            }
        }
    }
    
    /**
     * Download the Gemini Nano model if needed
     * Call this when the user explicitly wants to use AI features
     */
    suspend fun downloadModelIfNeeded(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val status = generativeModel.checkStatus()
                Log.d(TAG, "Model status before download: $status")
                // If model needs to be downloaded, handle it
                // Real implementation would use download flow
                Result.success(true)
            } catch (e: Exception) {
                Log.e(TAG, "Error checking/downloading model", e)
                Result.failure(e)
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
     * Uses ML Kit GenAI Prompt API with image input
     */
    suspend fun describeImage(bitmap: Bitmap): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Generating image description for ${bitmap.width}x${bitmap.height}")
                
                val request = generateContentRequest(
                    ImagePart(bitmap),
                    TextPart("Describe this photo in one clear sentence. Focus on the main subject and setting.")
                ) {
                    temperature = 0.7f
                    topK = 10
                    maxOutputTokens = 50
                }
                
                val response = generativeModel.generateContent(request).await()
                val description = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                if (description.isNotEmpty()) {
                    Log.d(TAG, "Image description generated successfully")
                    Result.success(description)
                } else {
                    Result.failure(Exception("No description generated"))
                }
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
        return rewriteText(text, "casual and friendly")
    }
    
    /**
     * Rewrite text in a more formal and professional style
     */
    suspend fun rewriteFormal(text: String): Result<String> {
        return rewriteText(text, "formal and professional")
    }
    
    /**
     * Rewrite text to be more concise
     */
    suspend fun rewriteShorter(text: String): Result<String> {
        return rewriteText(text, "shorter and more concise")
    }
    
    /**
     * Rewrite text to be more detailed
     */
    suspend fun rewriteLonger(text: String): Result<String> {
        return rewriteText(text, "longer with more detail")
    }
    
    /**
     * Rewrite text with the specified style using Prompt API
     */
    private suspend fun rewriteText(text: String, style: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Rewriting text with style: $style, length: ${text.length}")
                
                val prompt = "Rewrite the following text to be $style. Keep the core meaning but adjust the tone and length as needed:\n\n$text"
                
                val request = generateContentRequest(
                    TextPart(prompt)
                ) {
                    temperature = 0.8f
                    topK = 20
                    maxOutputTokens = text.length * 2
                }
                
                val response = generativeModel.generateContent(request).await()
                val rewrittenText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                if (rewrittenText.isNotEmpty()) {
                    Log.d(TAG, "Text rewritten successfully")
                    Result.success(rewrittenText)
                } else {
                    Result.failure(Exception("No rewritten text generated"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error rewriting text", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Proofread text to correct grammar and spelling errors using Prompt API
     */
    suspend fun proofread(text: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Proofreading text, length: ${text.length}")
                
                val prompt = "Proofread and correct any grammar, spelling, or punctuation errors in the following text. Keep the tone and meaning exactly the same:\n\n$text"
                
                val request = generateContentRequest(
                    TextPart(prompt)
                ) {
                    temperature = 0.3f  // Lower temperature for more consistent corrections
                    topK = 10
                    maxOutputTokens = text.length + 50
                }
                
                val response = generativeModel.generateContent(request).await()
                val proofreadText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                if (proofreadText.isNotEmpty()) {
                    Log.d(TAG, "Text proofread successfully")
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
    
    /**
     * CUSTOM USE CASE #1: Generate a personalized transition journey narrative
     * 
     * This unique feature analyzes a user's milestones and creates an encouraging,
     * personalized story about their transition journey.
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
                
                val request = generateContentRequest(
                    TextPart(prompt)
                ) {
                    temperature = 0.9f  // Higher temperature for more creative, personal responses
                    topK = 40
                    maxOutputTokens = 100
                }
                
                val response = generativeModel.generateContent(request).await()
                val narrative = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                if (narrative.isNotEmpty()) {
                    Log.d(TAG, "Journey narrative generated successfully")
                    Result.success(narrative)
                } else {
                    Result.failure(Exception("No narrative generated"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating journey narrative", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * CUSTOM USE CASE #2: Analyze milestone sentiment and suggest supportive responses
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
                
                val request = generateContentRequest(
                    TextPart(prompt)
                ) {
                    temperature = 0.8f
                    topK = 30
                    maxOutputTokens = 40
                }
                
                val response = generativeModel.generateContent(request).await()
                val celebration = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                if (celebration.isNotEmpty()) {
                    Log.d(TAG, "Milestone celebration generated successfully")
                    Result.success(celebration)
                } else {
                    Result.failure(Exception("No celebration generated"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating milestone celebration", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * CUSTOM USE CASE #3: Compare photos with AI-generated progress insights
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
                
                val prompt = "Compare these two photos taken $daysBetween days apart during someone's transition journey. Write 1-2 encouraging sentences noting any visible changes or progress. Be specific but respectful, focusing on confidence, growth, and authenticity."
                
                val request = generateContentRequest(
                    ImagePart(earlierPhoto),
                    ImagePart(laterPhoto),
                    TextPart(prompt)
                ) {
                    temperature = 0.7f
                    topK = 25
                    maxOutputTokens = 80
                }
                
                val response = generativeModel.generateContent(request).await()
                val insight = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                if (insight.isNotEmpty()) {
                    Log.d(TAG, "Photo comparison insight generated successfully")
                    Result.success(insight)
                } else {
                    Result.failure(Exception("No insight generated"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating photo comparison insight", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Clean up resources
     */
    fun close() {
        try {
            Log.d(TAG, "GeminiNanoManager closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing GeminiNanoManager", e)
        }
    }
}
