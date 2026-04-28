/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition

import com.shelbeely.opentransition.data.AudioAnalysis
import com.google.gson.stream.JsonReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.StringReader

class AudioAnalysisSerializationTest {

    private fun readerFor(json: String): JsonReader {
        val reader = JsonReader(StringReader(json))
        reader.beginObject()
        return reader
    }

    // ── toJson ────────────────────────────────────────────────────────────────

    @Test
    fun toJson_containsId() {
        val analysis = AudioAnalysis().apply { id = "audio-id-1" }
        val json = analysis.toJson()
        assertNotNull(json)
        assertEquals("audio-id-1", json!!.get(AudioAnalysis.FIELD_ID).asString)
    }

    @Test
    fun toJson_containsPhotoId() {
        val analysis = AudioAnalysis().apply { photoId = "photo-id-1" }
        val json = analysis.toJson()
        assertNotNull(json)
        assertEquals("photo-id-1", json!!.get(AudioAnalysis.FIELD_PHOTO_ID).asString)
    }

    @Test
    fun toJson_containsF0Mean() {
        val analysis = AudioAnalysis().apply { f0Mean = 150.5f }
        val json = analysis.toJson()
        assertNotNull(json)
        assertEquals(150.5f, json!!.get(AudioAnalysis.FIELD_F0_MEAN).asFloat, 0.001f)
    }

    @Test
    fun toJson_containsF0Min() {
        val analysis = AudioAnalysis().apply { f0Min = 85.0f }
        val json = analysis.toJson()
        assertNotNull(json)
        assertEquals(85.0f, json!!.get(AudioAnalysis.FIELD_F0_MIN).asFloat, 0.001f)
    }

    @Test
    fun toJson_containsF0Max() {
        val analysis = AudioAnalysis().apply { f0Max = 300.0f }
        val json = analysis.toJson()
        assertNotNull(json)
        assertEquals(300.0f, json!!.get(AudioAnalysis.FIELD_F0_MAX).asFloat, 0.001f)
    }

    @Test
    fun toJson_containsFormants() {
        val analysis = AudioAnalysis().apply {
            f1Mean = 500.0f
            f2Mean = 1500.0f
            f3Mean = 2500.0f
            f4Mean = 3500.0f
        }
        val json = analysis.toJson()
        assertNotNull(json)
        assertEquals(500.0f, json!!.get(AudioAnalysis.FIELD_F1_MEAN).asFloat, 0.001f)
        assertEquals(1500.0f, json.get(AudioAnalysis.FIELD_F2_MEAN).asFloat, 0.001f)
        assertEquals(2500.0f, json.get(AudioAnalysis.FIELD_F3_MEAN).asFloat, 0.001f)
        assertEquals(3500.0f, json.get(AudioAnalysis.FIELD_F4_MEAN).asFloat, 0.001f)
    }

    @Test
    fun toJson_containsF0StdDev() {
        val analysis = AudioAnalysis().apply { f0StdDev = 20.3f }
        val json = analysis.toJson()
        assertNotNull(json)
        assertEquals(20.3f, json!!.get(AudioAnalysis.FIELD_F0_STD_DEV).asFloat, 0.001f)
    }

    @Test
    fun toJson_containsPitchConfidenceMean() {
        val analysis = AudioAnalysis().apply { pitchConfidenceMean = 0.85f }
        val json = analysis.toJson()
        assertNotNull(json)
        assertEquals(0.85f, json!!.get(AudioAnalysis.FIELD_PITCH_CONFIDENCE_MEAN).asFloat, 0.001f)
    }

    @Test
    fun toJson_containsVoicedRatio() {
        val analysis = AudioAnalysis().apply { voicedRatio = 0.72f }
        val json = analysis.toJson()
        assertNotNull(json)
        assertEquals(0.72f, json!!.get(AudioAnalysis.FIELD_VOICED_RATIO).asFloat, 0.001f)
    }

    @Test
    fun toJson_containsSessionSummaryText() {
        val analysis = AudioAnalysis().apply { sessionSummaryText = "Test summary." }
        val json = analysis.toJson()
        assertNotNull(json)
        assertEquals("Test summary.", json!!.get(AudioAnalysis.FIELD_SESSION_SUMMARY_TEXT).asString)
    }

    @Test
    fun toJson_containsDurationSeconds() {
        val analysis = AudioAnalysis().apply { durationSeconds = 5.75f }
        val json = analysis.toJson()
        assertNotNull(json)
        assertEquals(5.75f, json!!.get(AudioAnalysis.FIELD_DURATION_SECONDS).asFloat, 0.001f)
    }

    @Test
    fun toJson_containsAnalysisTimestamp() {
        val analysis = AudioAnalysis().apply { analysisTimestamp = 1700000000000L }
        val json = analysis.toJson()
        assertNotNull(json)
        assertEquals(1700000000000L, json!!.get(AudioAnalysis.FIELD_ANALYSIS_TIMESTAMP).asLong)
    }

    // ── fromJson ──────────────────────────────────────────────────────────────

    @Test
    fun fromJson_validJson_returnsNonNull() {
        val json = """
            {
              "id": "aa-bb-cc",
              "photoId": "photo-1",
              "f0Mean": 160.0,
              "f0Min": 90.0,
              "f0Max": 280.0,
              "f1Mean": 480.0,
              "f2Mean": 1400.0,
              "f3Mean": 2600.0,
              "f4Mean": 3400.0,
              "f0StdDev": 15.0,
              "durationSeconds": 4.5,
              "analysisTimestamp": 1686000000000
            }
        """.trimIndent()
        val result = AudioAnalysis.fromJson(readerFor(json))
        assertNotNull(result)
    }

    @Test
    fun fromJson_validJson_parsesId() {
        val json = buildMinimalJson(id = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        val result = AudioAnalysis.fromJson(readerFor(json))
        assertEquals("a1b2c3d4-e5f6-7890-abcd-ef1234567890", result?.id)
    }

    @Test
    fun fromJson_validJson_parsesPhotoId() {
        val json = buildMinimalJson(photoId = "photo-xyz")
        val result = AudioAnalysis.fromJson(readerFor(json))
        assertEquals("photo-xyz", result?.photoId)
    }

    @Test
    fun fromJson_validJson_parsesF0Mean() {
        val json = buildMinimalJson(f0Mean = 175.5)
        val result = AudioAnalysis.fromJson(readerFor(json))
        assertEquals(175.5f, result?.f0Mean ?: 0f, 0.001f)
    }

    @Test
    fun fromJson_validJson_parsesDuration() {
        val json = buildMinimalJson(durationSeconds = 7.0)
        val result = AudioAnalysis.fromJson(readerFor(json))
        assertEquals(7.0f, result?.durationSeconds ?: 0f, 0.001f)
    }

    @Test
    fun fromJson_invalidUUID_usesRandomId() {
        val json = buildMinimalJson(id = "not-a-uuid")
        val result = AudioAnalysis.fromJson(readerFor(json))
        assertNotNull(result)
        assertNotNull(result?.id)
    }

    @Test
    fun fromJson_unknownField_isSkipped() {
        val json = """{"id":"id1","photoId":"","f0Mean":0,"f0Min":0,"f0Max":0,"f1Mean":0,
            "f2Mean":0,"f3Mean":0,"f4Mean":0,"f0StdDev":0,"durationSeconds":0,
            "analysisTimestamp":0,"extra":"ignored"}""".trimIndent()
        val result = AudioAnalysis.fromJson(readerFor(json))
        assertNotNull(result)
    }

    @Test
    fun fromJson_emptyObject_returnsNonNullWithDefaults() {
        val result = AudioAnalysis.fromJson(readerFor("{}"))
        assertNotNull(result)
    }

    // ── round-trip ────────────────────────────────────────────────────────────

    @Test
    fun roundTrip_toJsonThenFromJson_preservesAllFields() {
        val original = AudioAnalysis().apply {
            id = "c0ffee00-dead-beef-cafe-123456789abc"
            photoId = "photo-rt"
            f0Mean = 155.0f
            f0Min = 80.0f
            f0Max = 290.0f
            f1Mean = 490.0f
            f2Mean = 1450.0f
            f3Mean = 2550.0f
            f4Mean = 3450.0f
            f0StdDev = 18.0f
            pitchConfidenceMean = 0.78f
            voicedRatio = 0.65f
            intensityMeanDb = -28.5f
            intensityMaxDb = -12.3f
            pitchRangeHz = 210.0f
            pitchStabilityScore = 0.82f
            intonationMovement = 9.4f
            sessionSummaryText = "Round-trip summary."
            durationSeconds = 6.0f
            analysisTimestamp = 1699999999000L
        }
        val jsonString = original.toJson()!!.toString()
        val reader = JsonReader(StringReader(jsonString))
        reader.beginObject()
        val restored = AudioAnalysis.fromJson(reader)

        assertNotNull(restored)
        assertEquals(original.id, restored?.id)
        assertEquals(original.photoId, restored?.photoId)
        assertEquals(original.f0Mean, restored?.f0Mean ?: 0f, 0.001f)
        assertEquals(original.f0Min, restored?.f0Min ?: 0f, 0.001f)
        assertEquals(original.f0Max, restored?.f0Max ?: 0f, 0.001f)
        assertEquals(original.f1Mean, restored?.f1Mean ?: 0f, 0.001f)
        assertEquals(original.f2Mean, restored?.f2Mean ?: 0f, 0.001f)
        assertEquals(original.f3Mean, restored?.f3Mean ?: 0f, 0.001f)
        assertEquals(original.f4Mean, restored?.f4Mean ?: 0f, 0.001f)
        assertEquals(original.f0StdDev, restored?.f0StdDev ?: 0f, 0.001f)
        assertEquals(original.pitchConfidenceMean, restored?.pitchConfidenceMean ?: 0f, 0.001f)
        assertEquals(original.voicedRatio, restored?.voicedRatio ?: 0f, 0.001f)
        assertEquals(original.intensityMeanDb, restored?.intensityMeanDb ?: 0f, 0.001f)
        assertEquals(original.intensityMaxDb, restored?.intensityMaxDb ?: 0f, 0.001f)
        assertEquals(original.pitchRangeHz, restored?.pitchRangeHz ?: 0f, 0.001f)
        assertEquals(original.pitchStabilityScore, restored?.pitchStabilityScore ?: 0f, 0.001f)
        assertEquals(original.intonationMovement, restored?.intonationMovement ?: 0f, 0.001f)
        assertEquals(original.sessionSummaryText, restored?.sessionSummaryText)
        assertEquals(original.durationSeconds, restored?.durationSeconds ?: 0f, 0.001f)
        assertEquals(original.analysisTimestamp, restored?.analysisTimestamp)
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun buildMinimalJson(
        id: String = "default-id",
        photoId: String = "",
        f0Mean: Double = 0.0,
        f0Min: Double = 0.0,
        f0Max: Double = 0.0,
        f1Mean: Double = 0.0,
        f2Mean: Double = 0.0,
        f3Mean: Double = 0.0,
        f4Mean: Double = 0.0,
        f0StdDev: Double = 0.0,
        durationSeconds: Double = 0.0,
        analysisTimestamp: Long = 0L
    ): String = """{"id":"$id","photoId":"$photoId","f0Mean":$f0Mean,"f0Min":$f0Min,
        |"f0Max":$f0Max,"f1Mean":$f1Mean,"f2Mean":$f2Mean,"f3Mean":$f3Mean,
        |"f4Mean":$f4Mean,"f0StdDev":$f0StdDev,"durationSeconds":$durationSeconds,
        |"analysisTimestamp":$analysisTimestamp}""".trimMargin()
}
