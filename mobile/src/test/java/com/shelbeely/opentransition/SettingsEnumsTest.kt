/*
 * Copyright © 2019-2022 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition

import com.shelbeely.opentransition.util.settings.LockDelay
import com.shelbeely.opentransition.util.settings.LockType
import com.shelbeely.opentransition.util.settings.Theme
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsEnumsTest {

    // ── LockDelay.getMilli() ──────────────────────────────────────────────────

    @Test
    fun lockDelay_instant_getMilliIsZero() {
        assertEquals(0L, LockDelay.instant.getMilli())
    }

    @Test
    fun lockDelay_oneMinute_getMilliIs60000() {
        assertEquals(60_000L, LockDelay.oneMinute.getMilli())
    }

    @Test
    fun lockDelay_twoMinutes_getMilliIs120000() {
        assertEquals(120_000L, LockDelay.twoMinutes.getMilli())
    }

    @Test
    fun lockDelay_fiveMinutes_getMilliIs300000() {
        assertEquals(300_000L, LockDelay.fiveMinutes.getMilli())
    }

    @Test
    fun lockDelay_fifteenMinutes_getMilliIs900000() {
        assertEquals(900_000L, LockDelay.fifteenMinutes.getMilli())
    }

    // ── LockDelay defaults ───────────────────────────────────────────────────

    @Test
    fun lockDelay_default_isInstant() {
        assertEquals(LockDelay.instant, LockDelay.default())
    }

    @Test
    fun lockDelay_allValues_areEnumerable() {
        val values = LockDelay.values()
        assertEquals(5, values.size)
    }

    // ── LockType ─────────────────────────────────────────────────────────────

    @Test
    fun lockType_default_isOff() {
        assertEquals(LockType.off, LockType.default())
    }

    @Test
    fun lockType_allValues_areEnumerable() {
        val values = LockType.values()
        assertEquals(4, values.size)
    }

    @Test
    fun lockType_valueOf_off_works() {
        assertEquals(LockType.off, LockType.valueOf("off"))
    }

    @Test
    fun lockType_valueOf_normal_works() {
        assertEquals(LockType.normal, LockType.valueOf("normal"))
    }

    @Test
    fun lockType_valueOf_trains_works() {
        assertEquals(LockType.trains, LockType.valueOf("trains"))
    }

    @Test
    fun lockType_valueOf_biometric_works() {
        assertEquals(LockType.biometric, LockType.valueOf("biometric"))
    }

    // ── Theme ─────────────────────────────────────────────────────────────────

    @Test
    fun theme_default_isPink() {
        assertEquals(Theme.pink, Theme.default())
    }

    @Test
    fun theme_allValues_areEnumerable() {
        val values = Theme.values()
        assertEquals(4, values.size)
    }

    @Test
    fun theme_valueOf_pink_works() {
        assertEquals(Theme.pink, Theme.valueOf("pink"))
    }

    @Test
    fun theme_valueOf_blue_works() {
        assertEquals(Theme.blue, Theme.valueOf("blue"))
    }

    @Test
    fun theme_valueOf_purple_works() {
        assertEquals(Theme.purple, Theme.valueOf("purple"))
    }

    @Test
    fun theme_valueOf_green_works() {
        assertEquals(Theme.green, Theme.valueOf("green"))
    }
}
