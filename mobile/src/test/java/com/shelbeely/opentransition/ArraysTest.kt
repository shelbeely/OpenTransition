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

import com.shelbeely.opentransition.util.nullAllElements
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class ArraysTest {

    @Test
    fun nullAllElements_allElementsBecomNull() {
        val array = arrayOf<String?>("a", "b", "c")
        array.nullAllElements()
        array.forEach { assertNull(it) }
    }

    @Test
    fun nullAllElements_singleElement_becomesNull() {
        val array = arrayOf<String?>("only")
        array.nullAllElements()
        assertNull(array[0])
    }

    @Test
    fun nullAllElements_emptyArray_doesNotThrow() {
        val array = arrayOf<String?>()
        array.nullAllElements() // must not throw
        assertEquals(0, array.size)
    }

    @Test
    fun nullAllElements_alreadyNullElements_remainsNull() {
        val array = arrayOf<String?>(null, null)
        array.nullAllElements()
        array.forEach { assertNull(it) }
    }

    @Test
    fun nullAllElements_mixedNullAndNonNull_allBecomeNull() {
        val array = arrayOf<String?>("a", null, "c", null)
        array.nullAllElements()
        array.forEach { assertNull(it) }
    }

    @Test
    fun nullAllElements_integerArray_allBecomeNull() {
        val array = arrayOf<Int?>(1, 2, 3)
        array.nullAllElements()
        array.forEach { assertNull(it) }
    }
}
