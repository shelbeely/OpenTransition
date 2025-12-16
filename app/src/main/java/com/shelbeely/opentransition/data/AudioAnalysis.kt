/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.data

import com.google.gson.JsonObject
import com.google.gson.stream.JsonReader
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

/**
 * Stores formant analysis results for an audio recording.
 * Formants are resonant frequencies of the vocal tract that characterize voice quality.
 * F1 and F2 are particularly important for vowel sounds and voice feminization/masculinization tracking.
 */
class AudioAnalysis : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    
    // Reference to the associated Photo (audio recording)
    var photoId: String = ""
    
    // Formant frequencies in Hz
    var f0Mean: Float = 0f  // Fundamental frequency (pitch) - average
    var f0Min: Float = 0f   // Minimum pitch
    var f0Max: Float = 0f   // Maximum pitch
    
    var f1Mean: Float = 0f  // First formant - average
    var f2Mean: Float = 0f  // Second formant - average
    var f3Mean: Float = 0f  // Third formant - average
    var f4Mean: Float = 0f  // Fourth formant - average
    
    // Statistical measures
    var f0StdDev: Float = 0f  // Pitch variability
    
    // Duration
    var durationSeconds: Float = 0f
    
    // Analysis timestamp
    var analysisTimestamp: Long = 0
    
    fun toJson(): JsonObject? {
        return try {
            JsonObject().apply {
                addProperty(FIELD_ID, id)
                addProperty(FIELD_PHOTO_ID, photoId)
                addProperty(FIELD_F0_MEAN, f0Mean)
                addProperty(FIELD_F0_MIN, f0Min)
                addProperty(FIELD_F0_MAX, f0Max)
                addProperty(FIELD_F1_MEAN, f1Mean)
                addProperty(FIELD_F2_MEAN, f2Mean)
                addProperty(FIELD_F3_MEAN, f3Mean)
                addProperty(FIELD_F4_MEAN, f4Mean)
                addProperty(FIELD_F0_STD_DEV, f0StdDev)
                addProperty(FIELD_DURATION_SECONDS, durationSeconds)
                addProperty(FIELD_ANALYSIS_TIMESTAMP, analysisTimestamp)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    companion object {
        const val FIELD_ID = "id"
        const val FIELD_PHOTO_ID = "photoId"
        const val FIELD_F0_MEAN = "f0Mean"
        const val FIELD_F0_MIN = "f0Min"
        const val FIELD_F0_MAX = "f0Max"
        const val FIELD_F1_MEAN = "f1Mean"
        const val FIELD_F2_MEAN = "f2Mean"
        const val FIELD_F3_MEAN = "f3Mean"
        const val FIELD_F4_MEAN = "f4Mean"
        const val FIELD_F0_STD_DEV = "f0StdDev"
        const val FIELD_DURATION_SECONDS = "durationSeconds"
        const val FIELD_ANALYSIS_TIMESTAMP = "analysisTimestamp"
        
        fun fromJson(jsonReader: JsonReader): AudioAnalysis? {
            return try {
                AudioAnalysis().apply {
                    while (jsonReader.hasNext()) {
                        when (jsonReader.nextName()) {
                            FIELD_ID -> {
                                id = try {
                                    UUID.fromString(jsonReader.nextString())
                                } catch (e: IllegalArgumentException) {
                                    e.printStackTrace()
                                    UUID.randomUUID()
                                }.toString()
                            }
                            FIELD_PHOTO_ID -> photoId = jsonReader.nextString()
                            FIELD_F0_MEAN -> f0Mean = jsonReader.nextDouble().toFloat()
                            FIELD_F0_MIN -> f0Min = jsonReader.nextDouble().toFloat()
                            FIELD_F0_MAX -> f0Max = jsonReader.nextDouble().toFloat()
                            FIELD_F1_MEAN -> f1Mean = jsonReader.nextDouble().toFloat()
                            FIELD_F2_MEAN -> f2Mean = jsonReader.nextDouble().toFloat()
                            FIELD_F3_MEAN -> f3Mean = jsonReader.nextDouble().toFloat()
                            FIELD_F4_MEAN -> f4Mean = jsonReader.nextDouble().toFloat()
                            FIELD_F0_STD_DEV -> f0StdDev = jsonReader.nextDouble().toFloat()
                            FIELD_DURATION_SECONDS -> durationSeconds = jsonReader.nextDouble().toFloat()
                            FIELD_ANALYSIS_TIMESTAMP -> analysisTimestamp = jsonReader.nextLong()
                            else -> jsonReader.skipValue()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
