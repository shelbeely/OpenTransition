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

import com.shelbeely.opentransition.data.Photo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for [Photo.toJson].
 *
 * Note: [Photo.fromJson] is not tested here because it calls [com.shelbeely.opentransition.util.FileUtil.getMediaFile],
 * which requires an Android application context. Those paths are covered by instrumented tests.
 */
class PhotoSerializationTest {

    @Test
    fun toJson_containsId() {
        val photo = Photo().apply { id = "photo-id-abc" }
        val json = photo.toJson()
        assertNotNull(json)
        assertEquals("photo-id-abc", json!!.get(Photo.FIELD_ID).asString)
    }

    @Test
    fun toJson_containsEpochDay() {
        val photo = Photo().apply { epochDay = 19523L }
        val json = photo.toJson()
        assertNotNull(json)
        assertEquals(19523L, json!!.get(Photo.FIELD_EPOCH_DAY).asLong)
    }

    @Test
    fun toJson_containsTimestamp() {
        val photo = Photo().apply { timestamp = 1686787200000L }
        val json = photo.toJson()
        assertNotNull(json)
        assertEquals(1686787200000L, json!!.get(Photo.FIELD_TIMESTAMP).asLong)
    }

    @Test
    fun toJson_containsFileNameOnly_notFullPath() {
        val photo = Photo().apply { filePath = "/data/user/0/com.shelbeely.opentransition/files/photos/photo_2023-06-15_imported_20230615_120000.jpg" }
        val json = photo.toJson()
        assertNotNull(json)
        assertEquals(
            "photo_2023-06-15_imported_20230615_120000.jpg",
            json!!.get(Photo.FIELD_FILE_NAME).asString
        )
    }

    @Test
    fun toJson_typeFace_serialisedCorrectly() {
        val photo = Photo().apply { type = Photo.TYPE_FACE }
        val json = photo.toJson()
        assertNotNull(json)
        assertEquals(Photo.TYPE_FACE, json!!.get(Photo.FIELD_TYPE).asInt)
    }

    @Test
    fun toJson_typeBody_serialisedCorrectly() {
        val photo = Photo().apply { type = Photo.TYPE_BODY }
        val json = photo.toJson()
        assertNotNull(json)
        assertEquals(Photo.TYPE_BODY, json!!.get(Photo.FIELD_TYPE).asInt)
    }

    @Test
    fun toJson_typeAudio_serialisedCorrectly() {
        val photo = Photo().apply { type = Photo.TYPE_AUDIO }
        val json = photo.toJson()
        assertNotNull(json)
        assertEquals(Photo.TYPE_AUDIO, json!!.get(Photo.FIELD_TYPE).asInt)
    }

    @Test
    fun toJson_allFieldsPresent() {
        val photo = Photo().apply {
            id = "full-photo-id"
            epochDay = 18000L
            timestamp = 1554940800000L
            filePath = "/some/path/photo.jpg"
            type = Photo.TYPE_FACE
        }
        val json = photo.toJson()
        assertNotNull(json)
        assertNotNull(json!!.get(Photo.FIELD_ID))
        assertNotNull(json.get(Photo.FIELD_EPOCH_DAY))
        assertNotNull(json.get(Photo.FIELD_TIMESTAMP))
        assertNotNull(json.get(Photo.FIELD_FILE_NAME))
        assertNotNull(json.get(Photo.FIELD_TYPE))
    }

    @Test
    fun toJson_emptyFilePath_usesEmptyFileName() {
        val photo = Photo().apply { filePath = "" }
        val json = photo.toJson()
        assertNotNull(json)
        assertEquals("", json!!.get(Photo.FIELD_FILE_NAME).asString)
    }

    @Test
    fun photoTypeConstants_haveExpectedValues() {
        assertEquals(0, Photo.TYPE_FACE)
        assertEquals(1, Photo.TYPE_BODY)
        assertEquals(2, Photo.TYPE_AUDIO)
    }
}
