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
 * AudioAnalysis data model.
 * Stores formant analysis results for an audio recording.
 * Formants are resonant frequencies of the vocal tract that characterize voice quality.
 * F1 and F2 are particularly important for vowel sounds and voice feminization/masculinization tracking.
 * 
 * **BACKWARDS COMPATIBILITY**: This class extends RealmObject for compatibility with the old app.
 * The new app uses Room database (see AudioAnalysisEntity), but this class is retained to support:
 * - Importing backups from the forked version
 * - Reading legacy Realm database files
 * - Data migration utilities (RealmToRoomMigration, RealmBackupImporter)
 * 
 * Do not use this class for new database operations. Use AudioAnalysisEntity with DatabaseManager instead.
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

    // Extended metrics — added in schema version 2
    var pitchConfidenceMean: Float = 0f   // Mean autocorrelation confidence (0–1)
    var voicedRatio: Float = 0f           // Fraction of frames classified as voiced
    var intensityMeanDb: Float = 0f       // Mean RMS level in dBFS
    var intensityMaxDb: Float = 0f        // Peak RMS level in dBFS
    var pitchRangeHz: Float = 0f          // f0Max − f0Min over voiced frames
    var pitchStabilityScore: Float = 0f   // 1 − normalised stdDev; 1 = very steady
    var intonationMovement: Float = 0f    // Mean |Δf0| between consecutive voiced frames (Hz/frame)

    // Session summary text — added in schema version 2
    var sessionSummaryText: String = ""
    
    // Duration
    var durationSeconds: Float = 0f
    
    // Analysis timestamp
    var analysisTimestamp: Long = 0

    // Speech transcript captured while recording (may be empty if recognition was unavailable)
    var transcript: String = ""
    
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
                addProperty(FIELD_PITCH_CONFIDENCE_MEAN, pitchConfidenceMean)
                addProperty(FIELD_VOICED_RATIO, voicedRatio)
                addProperty(FIELD_INTENSITY_MEAN_DB, intensityMeanDb)
                addProperty(FIELD_INTENSITY_MAX_DB, intensityMaxDb)
                addProperty(FIELD_PITCH_RANGE_HZ, pitchRangeHz)
                addProperty(FIELD_PITCH_STABILITY_SCORE, pitchStabilityScore)
                addProperty(FIELD_INTONATION_MOVEMENT, intonationMovement)
                addProperty(FIELD_SESSION_SUMMARY_TEXT, sessionSummaryText)
                addProperty(FIELD_DURATION_SECONDS, durationSeconds)
                addProperty(FIELD_ANALYSIS_TIMESTAMP, analysisTimestamp)
                addProperty(FIELD_TRANSCRIPT, transcript)
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
        const val FIELD_PITCH_CONFIDENCE_MEAN = "pitchConfidenceMean"
        const val FIELD_VOICED_RATIO = "voicedRatio"
        const val FIELD_INTENSITY_MEAN_DB = "intensityMeanDb"
        const val FIELD_INTENSITY_MAX_DB = "intensityMaxDb"
        const val FIELD_PITCH_RANGE_HZ = "pitchRangeHz"
        const val FIELD_PITCH_STABILITY_SCORE = "pitchStabilityScore"
        const val FIELD_INTONATION_MOVEMENT = "intonationMovement"
        const val FIELD_SESSION_SUMMARY_TEXT = "sessionSummaryText"
        const val FIELD_DURATION_SECONDS = "durationSeconds"
        const val FIELD_ANALYSIS_TIMESTAMP = "analysisTimestamp"
        const val FIELD_TRANSCRIPT = "transcript"
        
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
                            FIELD_PITCH_CONFIDENCE_MEAN -> pitchConfidenceMean = jsonReader.nextDouble().toFloat()
                            FIELD_VOICED_RATIO -> voicedRatio = jsonReader.nextDouble().toFloat()
                            FIELD_INTENSITY_MEAN_DB -> intensityMeanDb = jsonReader.nextDouble().toFloat()
                            FIELD_INTENSITY_MAX_DB -> intensityMaxDb = jsonReader.nextDouble().toFloat()
                            FIELD_PITCH_RANGE_HZ -> pitchRangeHz = jsonReader.nextDouble().toFloat()
                            FIELD_PITCH_STABILITY_SCORE -> pitchStabilityScore = jsonReader.nextDouble().toFloat()
                            FIELD_INTONATION_MOVEMENT -> intonationMovement = jsonReader.nextDouble().toFloat()
                            FIELD_SESSION_SUMMARY_TEXT -> sessionSummaryText = jsonReader.nextString()
                            FIELD_DURATION_SECONDS -> durationSeconds = jsonReader.nextDouble().toFloat()
                            FIELD_ANALYSIS_TIMESTAMP -> analysisTimestamp = jsonReader.nextLong()
                            FIELD_TRANSCRIPT -> transcript = jsonReader.nextString()
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
