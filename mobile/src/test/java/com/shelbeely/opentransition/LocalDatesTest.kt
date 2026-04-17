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

import com.shelbeely.opentransition.util.localDateFromEpochMilli
import com.shelbeely.opentransition.util.toFileDateFormat
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class LocalDatesTest {

    @Test
    fun localDateFromEpochMilli_epoch0_returnsJan1970() {
        // Epoch 0 = 1970-01-01 in UTC. The result depends on the system timezone, but
        // we can use the same conversion to produce the expected value.
        val expected = java.time.Instant.ofEpochMilli(0L)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val result = localDateFromEpochMilli(0L)
        assertEquals(expected, result)
    }

    @Test
    fun localDateFromEpochMilli_knownTimestamp_returnsCorrectDate() {
        // 2023-06-15 00:00:00 UTC in milliseconds = 1686787200000
        val millis = 1686787200000L
        val expected = java.time.Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val result = localDateFromEpochMilli(millis)
        assertEquals(expected, result)
    }

    @Test
    fun localDateFromEpochMilli_returnsLocalDate() {
        val result = localDateFromEpochMilli(System.currentTimeMillis())
        // Just verify it returns a non-null LocalDate; type check is sufficient.
        assertEquals(LocalDate::class.java, result.javaClass)
    }

    @Test
    fun toFileDateFormat_knownDate_returnsIsoFormat() {
        val date = LocalDate.of(2023, 6, 15)
        val result = date.toFileDateFormat()
        assertEquals("2023-06-15", result)
    }

    @Test
    fun toFileDateFormat_jan1_returnsCorrectIsoString() {
        val date = LocalDate.of(2020, 1, 1)
        val result = date.toFileDateFormat()
        assertEquals("2020-01-01", result)
    }

    @Test
    fun toFileDateFormat_dec31_returnsCorrectIsoString() {
        val date = LocalDate.of(1999, 12, 31)
        val result = date.toFileDateFormat()
        assertEquals("1999-12-31", result)
    }

    @Test
    fun toFileDateFormat_singleDigitMonthAndDay_padded() {
        val date = LocalDate.of(2021, 3, 5)
        val result = date.toFileDateFormat()
        assertEquals("2021-03-05", result)
    }

    @Test
    fun toFileDateFormat_roundTrip_fromEpochMilli() {
        // Build a LocalDate, convert to epoch millis, convert back, and check the file date
        val original = LocalDate.of(2024, 11, 22)
        val epochMillis = original.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val recovered = localDateFromEpochMilli(epochMillis)
        assertEquals(original.toFileDateFormat(), recovered.toFileDateFormat())
    }
}
