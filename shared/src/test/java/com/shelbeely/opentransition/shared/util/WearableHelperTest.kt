/*
 * Copyright © 2025-2026 Shelbeely and OpenTransition contributors.
 *
 * Part of OpenTransition, a fork of TransTracks (© 2018-2021 TransTracks),
 * licensed under GPL-3.0-or-later. See the NOTICE and AUTHORS files for
 * full attribution.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.shared.util

import com.google.gson.Gson
import com.shelbeely.opentransition.shared.models.MilestoneData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [WearableHelper].
 *
 * `syncMilestones` requires a real `DataClient` (Google Play Services), so we
 * cover the JSON serialization round-trip — the part that determines whether
 * milestone payloads survive the trip from mobile → DataItem → wear — by using
 * Gson directly and then parsing back through [WearableHelper.parseMilestones].
 */
class WearableHelperTest {

    private val gson = Gson()

    @Test
    fun parseMilestones_roundTrips_emptyList() {
        val json = gson.toJson(emptyList<MilestoneData>())
        val result = WearableHelper.parseMilestones(json)
        assertTrue(result.isEmpty())
    }

    @Test
    fun parseMilestones_roundTrips_singleEntry() {
        val original = listOf(
            MilestoneData(
                id = "m1",
                title = "Started HRT",
                description = "First dose today",
                date = 1_700_000_000_000L,
                type = "general"
            )
        )
        val result = WearableHelper.parseMilestones(gson.toJson(original))
        assertEquals(original, result)
    }

    @Test
    fun parseMilestones_roundTrips_maxSizeList() {
        val maxSize = 100
        val original = (1..maxSize).map { i ->
            MilestoneData(
                id = "milestone_$i",
                title = "Milestone $i",
                description = "Description for milestone $i",
                date = 1_700_000_000_000L + i * 86_400_000L,
                type = "general"
            )
        }
        val result = WearableHelper.parseMilestones(gson.toJson(original))
        assertEquals(maxSize, result.size)
        assertEquals(original, result)
    }

    @Test
    fun parseMilestones_roundTrips_unicodeTitles() {
        val original = listOf(
            MilestoneData("u1", "💉 First HRT dose", "🎉 yay", 1L, "general"),
            MilestoneData("u2", "髪を切った", "新しい髪型", 2L, "general"),
            MilestoneData("u3", "Café ☕ — déjà vu", null, 3L, "general"),
            MilestoneData("u4", "👗 New outfit", "résumé", 4L, "general")
        )
        val result = WearableHelper.parseMilestones(gson.toJson(original))
        assertEquals(original, result)
    }

    @Test
    fun parseMilestones_returnsEmpty_onMalformedJson() {
        val result = WearableHelper.parseMilestones("not valid json {{{")
        assertTrue(result.isEmpty())
    }

    @Test
    fun parseMilestones_handlesNullDescription() {
        val original = listOf(
            MilestoneData("n1", "Title", null, 100L, "general")
        )
        val result = WearableHelper.parseMilestones(gson.toJson(original))
        assertEquals(original, result)
    }
}
