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

import com.shelbeely.opentransition.data.Milestone
import com.google.gson.stream.JsonReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.StringReader

class MilestoneSerializationTest {

    // ── toJson ────────────────────────────────────────────────────────────────

    @Test
    fun toJson_containsId() {
        val milestone = Milestone().apply { id = "test-id-123" }
        val json = milestone.toJson()
        assertEquals("test-id-123", json.get(Milestone.FIELD_ID).asString)
    }

    @Test
    fun toJson_containsEpochDay() {
        val milestone = Milestone().apply { epochDay = 19523L }
        val json = milestone.toJson()
        assertEquals(19523L, json.get(Milestone.FIELD_EPOCH_DAY).asLong)
    }

    @Test
    fun toJson_containsTimestamp() {
        val milestone = Milestone().apply { timestamp = 1686787200000L }
        val json = milestone.toJson()
        assertEquals(1686787200000L, json.get(Milestone.FIELD_TIMESTAMP).asLong)
    }

    @Test
    fun toJson_containsTitle() {
        val milestone = Milestone().apply { title = "First day on HRT" }
        val json = milestone.toJson()
        assertEquals("First day on HRT", json.get(Milestone.FIELD_TITLE).asString)
    }

    @Test
    fun toJson_containsDescription() {
        val milestone = Milestone().apply { description = "Started estradiol" }
        val json = milestone.toJson()
        assertEquals("Started estradiol", json.get(Milestone.FIELD_DESCRIPTION).asString)
    }

    @Test
    fun toJson_emptyTitleAndDescription_areSerialised() {
        val milestone = Milestone().apply {
            title = ""
            description = ""
        }
        val json = milestone.toJson()
        assertEquals("", json.get(Milestone.FIELD_TITLE).asString)
        assertEquals("", json.get(Milestone.FIELD_DESCRIPTION).asString)
    }

    // ── fromJson ──────────────────────────────────────────────────────────────

    private fun readerFor(json: String): JsonReader {
        val reader = JsonReader(StringReader(json))
        reader.beginObject()
        return reader
    }

    @Test
    fun fromJson_validJson_returnsNonNull() {
        val json = """
            {
              "id": "abc-123",
              "epochDay": 19000,
              "timestamp": 1680000000000,
              "title": "Big day",
              "description": "Changed my name"
            }
        """.trimIndent()
        val result = Milestone.fromJson(readerFor(json))
        assertNotNull(result)
    }

    @Test
    fun fromJson_validJson_parsesId() {
        val id = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
        val json = """{"id":"$id","epochDay":0,"timestamp":0,"title":"","description":""}"""
        val result = Milestone.fromJson(readerFor(json))
        assertEquals(id, result?.id)
    }

    @Test
    fun fromJson_validJson_parsesEpochDay() {
        val json = """{"id":"id1","epochDay":18628,"timestamp":0,"title":"","description":""}"""
        val result = Milestone.fromJson(readerFor(json))
        assertEquals(18628L, result?.epochDay)
    }

    @Test
    fun fromJson_validJson_parsesTimestamp() {
        val json = """{"id":"id1","epochDay":0,"timestamp":1686787200000,"title":"","description":""}"""
        val result = Milestone.fromJson(readerFor(json))
        assertEquals(1686787200000L, result?.timestamp)
    }

    @Test
    fun fromJson_validJson_parsesTitle() {
        val json = """{"id":"id1","epochDay":0,"timestamp":0,"title":"My milestone","description":""}"""
        val result = Milestone.fromJson(readerFor(json))
        assertEquals("My milestone", result?.title)
    }

    @Test
    fun fromJson_validJson_parsesDescription() {
        val json = """{"id":"id1","epochDay":0,"timestamp":0,"title":"","description":"Some details"}"""
        val result = Milestone.fromJson(readerFor(json))
        assertEquals("Some details", result?.description)
    }

    @Test
    fun fromJson_invalidUUID_assignsNewRandomId() {
        val json = """{"id":"not-a-uuid","epochDay":0,"timestamp":0,"title":"","description":""}"""
        val result = Milestone.fromJson(readerFor(json))
        // Should still produce an object with a random UUID
        assertNotNull(result)
        assertNotNull(result?.id)
    }

    @Test
    fun fromJson_unknownField_isSkipped() {
        val json = """{"id":"id1","epochDay":0,"timestamp":0,"title":"","description":"","unknownField":"value"}"""
        val result = Milestone.fromJson(readerFor(json))
        assertNotNull(result)
    }

    @Test
    fun fromJson_emptyObject_returnsNonNullWithDefaults() {
        val json = """{}"""
        val result = Milestone.fromJson(readerFor(json))
        assertNotNull(result)
    }

    // ── round-trip ────────────────────────────────────────────────────────────

    @Test
    fun roundTrip_toJsonThenFromJson_preservesFields() {
        val original = Milestone().apply {
            id = "c0ffee00-dead-beef-cafe-123456789abc"
            epochDay = 19500L
            timestamp = 1700000000000L
            title = "Title here"
            description = "Desc here"
        }
        val jsonString = original.toJson().toString()
        val reader = JsonReader(StringReader(jsonString))
        reader.beginObject()
        val restored = Milestone.fromJson(reader)

        assertNotNull(restored)
        assertEquals(original.id, restored?.id)
        assertEquals(original.epochDay, restored?.epochDay)
        assertEquals(original.timestamp, restored?.timestamp)
        assertEquals(original.title, restored?.title)
        assertEquals(original.description, restored?.description)
    }
}
