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

import com.shelbeely.opentransition.util.Quadruple
import com.shelbeely.opentransition.util.toList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class QuadrupleTest {

    @Test
    fun quadruple_fieldsAreAccessible() {
        val q = Quadruple(1, "two", 3.0, true)
        assertEquals(1, q.first)
        assertEquals("two", q.second)
        assertEquals(3.0, q.third, 0.0)
        assertEquals(true, q.forth)
    }

    @Test
    fun quadruple_toString_containsAllValues() {
        val q = Quadruple("a", "b", "c", "d")
        val str = q.toString()
        assertEquals("(a, b, c, d)", str)
    }

    @Test
    fun quadruple_equality_sameValues_areEqual() {
        val q1 = Quadruple(1, 2, 3, 4)
        val q2 = Quadruple(1, 2, 3, 4)
        assertEquals(q1, q2)
    }

    @Test
    fun quadruple_equality_differentValues_areNotEqual() {
        val q1 = Quadruple(1, 2, 3, 4)
        val q2 = Quadruple(1, 2, 3, 5)
        assertNotEquals(q1, q2)
    }

    @Test
    fun quadruple_hashCode_sameValues_sameHashCode() {
        val q1 = Quadruple("a", "b", "c", "d")
        val q2 = Quadruple("a", "b", "c", "d")
        assertEquals(q1.hashCode(), q2.hashCode())
    }

    @Test
    fun quadruple_copy_createsCopyWithNewValue() {
        val original = Quadruple(1, 2, 3, 4)
        val copy = original.copy(forth = 99)
        assertEquals(99, copy.forth)
        assertEquals(1, copy.first)
    }

    @Test
    fun toList_convertsAllFourElementsToList() {
        val q = Quadruple(10, 20, 30, 40)
        val list = q.toList()
        assertEquals(listOf(10, 20, 30, 40), list)
    }

    @Test
    fun toList_preservesOrder() {
        val q = Quadruple("first", "second", "third", "forth")
        val list = q.toList()
        assertEquals("first", list[0])
        assertEquals("second", list[1])
        assertEquals("third", list[2])
        assertEquals("forth", list[3])
    }

    @Test
    fun toList_hasExactlyFourElements() {
        val q = Quadruple(1, 2, 3, 4)
        assertEquals(4, q.toList().size)
    }

    @Test
    fun quadruple_withNullableTypes_storesNulls() {
        val q = Quadruple<String?, Int?, Double?, Boolean?>(null, null, null, null)
        assertEquals(null, q.first)
        assertEquals(null, q.second)
        assertEquals(null, q.third)
        assertEquals(null, q.forth)
    }
}
