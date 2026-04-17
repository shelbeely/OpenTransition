/*
 * Copyright © 2023 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition

import com.shelbeely.opentransition.util.BoxedLong
import com.shelbeely.opentransition.util.boxed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BoxedLongTest {

    @Test
    fun boxedLong_storesValue() {
        val boxed = BoxedLong(42L)
        assertEquals(42L, boxed.value)
    }

    @Test
    fun boxedLong_invoke_returnsValue() {
        val boxed = BoxedLong(100L)
        assertEquals(100L, boxed())
    }

    @Test
    fun boxedLong_zero_storesCorrectly() {
        val boxed = BoxedLong(0L)
        assertEquals(0L, boxed.value)
        assertEquals(0L, boxed())
    }

    @Test
    fun boxedLong_negativeValue_storesCorrectly() {
        val boxed = BoxedLong(-1L)
        assertEquals(-1L, boxed.value)
    }

    @Test
    fun boxedLong_maxValue_storesCorrectly() {
        val boxed = BoxedLong(Long.MAX_VALUE)
        assertEquals(Long.MAX_VALUE, boxed.value)
    }

    @Test
    fun boxedLong_minValue_storesCorrectly() {
        val boxed = BoxedLong(Long.MIN_VALUE)
        assertEquals(Long.MIN_VALUE, boxed.value)
    }

    @Test
    fun boxedLong_equality_sameValue_areEqual() {
        val b1 = BoxedLong(7L)
        val b2 = BoxedLong(7L)
        assertEquals(b1, b2)
    }

    @Test
    fun boxedLong_equality_differentValues_areNotEqual() {
        val b1 = BoxedLong(7L)
        val b2 = BoxedLong(8L)
        assertNotEquals(b1, b2)
    }

    @Test
    fun boxedLong_hashCode_sameValues_sameHashCode() {
        val b1 = BoxedLong(42L)
        val b2 = BoxedLong(42L)
        assertEquals(b1.hashCode(), b2.hashCode())
    }

    @Test
    fun longBoxed_extensionCreatesBoxedLong() {
        val value = 99L
        val boxed = value.boxed()
        assertEquals(BoxedLong(99L), boxed)
    }

    @Test
    fun longBoxed_extensionValue_matchesOriginal() {
        val value = 123456789L
        val boxed = value.boxed()
        assertEquals(value, boxed.value)
        assertEquals(value, boxed())
    }

    @Test
    fun longBoxed_zero_createsBoxedLongWithZero() {
        val boxed = 0L.boxed()
        assertEquals(BoxedLong(0L), boxed)
    }
}
