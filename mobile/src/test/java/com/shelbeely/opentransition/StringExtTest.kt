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

import com.shelbeely.opentransition.util.simpleIsEmail
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StringExtTest {

    @Test
    fun simpleIsEmail_standardEmail_returnsTrue() {
        assertTrue("user@example.com".simpleIsEmail())
    }

    @Test
    fun simpleIsEmail_emailWithSubdomain_returnsTrue() {
        assertTrue("user@mail.example.com".simpleIsEmail())
    }

    @Test
    fun simpleIsEmail_emailWithPlusAlias_returnsTrue() {
        assertTrue("user+alias@example.com".simpleIsEmail())
    }

    @Test
    fun simpleIsEmail_emailWithDots_returnsTrue() {
        assertTrue("first.last@example.com".simpleIsEmail())
    }

    @Test
    fun simpleIsEmail_singleCharBeforeAndAfterAt_returnsTrue() {
        assertTrue("a@b".simpleIsEmail())
    }

    @Test
    fun simpleIsEmail_emptyString_returnsFalse() {
        assertFalse("".simpleIsEmail())
    }

    @Test
    fun simpleIsEmail_noAtSign_returnsFalse() {
        assertFalse("userexample.com".simpleIsEmail())
    }

    @Test
    fun simpleIsEmail_atSignOnly_returnsFalse() {
        assertFalse("@".simpleIsEmail())
    }

    @Test
    fun simpleIsEmail_nothingBeforeAtSign_returnsFalse() {
        assertFalse("@example.com".simpleIsEmail())
    }

    @Test
    fun simpleIsEmail_nothingAfterAtSign_returnsFalse() {
        assertFalse("user@".simpleIsEmail())
    }

    @Test
    fun simpleIsEmail_multipleAtSigns_returnsTrue() {
        // The regex ^.+@.+$ is greedy, so the first `.+` can consume `user@other`
        // leaving the second `@` as the separator — this passes the simple check.
        assertTrue("user@other@example.com".simpleIsEmail())
    }

    @Test
    fun simpleIsEmail_whitespaceOnly_returnsFalse() {
        assertFalse("   ".simpleIsEmail())
    }

    @Test
    fun simpleIsEmail_spaceAroundAt_returnsTrue() {
        // Spaces are matched by `.` in the regex — the check is intentionally simple.
        assertTrue("user @example.com".simpleIsEmail())
    }
}
