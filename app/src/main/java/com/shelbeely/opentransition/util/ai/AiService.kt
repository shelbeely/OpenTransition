/*
 * Copyright © 2025 Shelbeely and OpenTransition contributors. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.util.ai

import com.shelbeely.opentransition.util.settings.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Service for interacting with OpenAI-compatible APIs
 */
object AiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Check if AI features are properly configured
     */
    fun isConfigured(): Boolean {
        return SettingsManager.getEnableAiFeatures() && 
               SettingsManager.getAiApiKey().isNotEmpty()
    }

    /**
     * Make a chat completion request to the AI API
     * @param systemPrompt The system message to set context
     * @param userPrompt The user's message/question
     * @param temperature Control randomness (0.0 to 2.0, default 0.7)
     * @return The AI's response text
     * @throws IOException if the request fails
     * @throws IllegalStateException if AI is not configured
     */
    suspend fun chatCompletion(
        systemPrompt: String,
        userPrompt: String,
        temperature: Double = 0.7
    ): String = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            throw IllegalStateException("AI features are not configured")
        }

        val apiKey = SettingsManager.getAiApiKey()
        val baseUrl = SettingsManager.getAiApiBaseUrl()
        val model = SettingsManager.getAiModel()

        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", userPrompt)
            })
        }

        val requestBody = JSONObject().apply {
            put("model", model)
            put("messages", messages)
            put("temperature", temperature)
            put("max_tokens", 500)
        }

        val request = Request.Builder()
            .url("$baseUrl/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("API request failed: ${response.code} ${response.message}")
            }

            val responseBody = response.body?.string() 
                ?: throw IOException("Empty response body")

            val jsonResponse = JSONObject(responseBody)
            val choices = jsonResponse.getJSONArray("choices")
            
            if (choices.length() == 0) {
                throw IOException("No response choices returned")
            }

            return@withContext choices.getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        }
    }

    /**
     * Generate a description for a photo based on its metadata
     */
    suspend fun generatePhotoDescription(
        photoType: String,
        date: String,
        daysSinceStart: Int
    ): String {
        val systemPrompt = """
            You are a supportive and empathetic assistant helping someone document their transition journey.
            Generate concise, encouraging descriptions for their progress photos.
            Focus on acknowledging their courage and progress. Keep responses under 100 words.
        """.trimIndent()

        val userPrompt = """
            Generate a supportive description for a $photoType photo taken on $date, 
            which is $daysSinceStart days into their transition journey.
        """.trimIndent()

        return chatCompletion(systemPrompt, userPrompt, temperature = 0.8)
    }

    /**
     * Generate progress insights based on the user's journey
     */
    suspend fun generateProgressInsights(
        daysSinceStart: Int,
        totalPhotos: Int,
        totalMilestones: Int,
        recentMilestones: List<String>
    ): String {
        val systemPrompt = """
            You are a supportive assistant analyzing someone's transition journey.
            Provide encouraging insights about their progress, celebrating their milestones and consistency.
            Be specific, personal, and affirming. Keep responses under 200 words.
        """.trimIndent()

        val milestonesText = if (recentMilestones.isNotEmpty()) {
            recentMilestones.joinToString(", ")
        } else {
            "None yet"
        }

        val userPrompt = """
            Analyze this transition journey:
            - Days since start: $daysSinceStart
            - Total photos: $totalPhotos
            - Total milestones: $totalMilestones
            - Recent milestones: $milestonesText
            
            Provide encouraging insights about their progress and journey.
        """.trimIndent()

        return chatCompletion(systemPrompt, userPrompt, temperature = 0.7)
    }

    /**
     * Suggest milestones based on the current timeline
     */
    suspend fun generateMilestoneSuggestions(
        daysSinceStart: Int,
        existingMilestones: List<String>
    ): String {
        val systemPrompt = """
            You are a knowledgeable assistant helping someone track their transition journey.
            Suggest relevant milestones they might want to document based on their timeline.
            Be inclusive and recognize that every journey is unique. Provide 3-5 suggestions.
            Keep the entire response under 200 words.
        """.trimIndent()

        val existingText = if (existingMilestones.isNotEmpty()) {
            existingMilestones.joinToString(", ")
        } else {
            "None yet"
        }

        val userPrompt = """
            For someone $daysSinceStart days into their transition journey who has recorded these milestones: $existingText
            
            Suggest other meaningful milestones they might want to document. Be specific and supportive.
        """.trimIndent()

        return chatCompletion(systemPrompt, userPrompt, temperature = 0.8)
    }

    /**
     * Compare photos and provide supportive analysis
     */
    suspend fun comparePhotos(
        photoType: String,
        date1: String,
        date2: String,
        daysBetween: Int
    ): String {
        val systemPrompt = """
            You are a supportive assistant helping someone track their transition journey.
            When comparing photos, focus on acknowledging the passage of time and their commitment 
            to documenting their journey. Be encouraging and affirming.
            Note: You cannot actually see the photos, so focus on the timeline and commitment aspect.
            Keep responses under 150 words.
        """.trimIndent()

        val userPrompt = """
            Someone is comparing their $photoType photos:
            - First photo: $date1
            - Second photo: $date2
            - Time between photos: $daysBetween days
            
            Provide encouraging thoughts about their journey and progress documentation.
        """.trimIndent()

        return chatCompletion(systemPrompt, userPrompt, temperature = 0.7)
    }
}
