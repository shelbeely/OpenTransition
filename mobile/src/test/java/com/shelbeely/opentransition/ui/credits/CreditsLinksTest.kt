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

package com.shelbeely.opentransition.ui.credits

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guard rails for the centralised attribution URLs.
 *
 * The most important assertion here is that the TransTracks repository URL
 * keeps the `-Android` suffix. The pre-fork value (without the suffix) was a
 * dead link, so users tapping the in-app credit landed on a 404 instead of
 * the archived original work. Keep these tests passing.
 */
class CreditsLinksTest {

    @Test
    fun `TransTracks repo URL points at the archived -Android repository`() {
        // The original TransTracks source lives at
        // github.com/TransTracks/TransTracks-Android — the bare
        // TransTracks/TransTracks slug 404s.
        assertEquals(
            "https://github.com/TransTracks/TransTracks-Android",
            CreditsLinks.TRANSTRACKS_REPO
        )
    }

    @Test
    fun `all credits URLs use https`() {
        listOf(
            CreditsLinks.TRANSTRACKS_REPO,
            CreditsLinks.OPENTRANSITION_REPO,
            CreditsLinks.OPENTRANSITION_CONTRIBUTORS,
            CreditsLinks.SHELBEELY_GITHUB,
            CreditsLinks.GPL_V3_LICENSE,
            CreditsLinks.OPENTRANSITION_CREDITS_DOCS,
            CreditsLinks.OPENTRANSITION_PRIVACY_DOCS,
        ).forEach { url ->
            assertTrue("Expected https URL but got: $url", url.startsWith("https://"))
        }
    }

    @Test
    fun `OpenTransition contributors URL points at the contributors graph`() {
        assertEquals(
            "https://github.com/shelbeely/OpenTransition/graphs/contributors",
            CreditsLinks.OPENTRANSITION_CONTRIBUTORS
        )
    }

    @Test
    fun `GPL v3 license URL is the canonical GNU URL`() {
        assertEquals("https://www.gnu.org/licenses/gpl-3.0.html", CreditsLinks.GPL_V3_LICENSE)
    }

    @Test
    fun `no URL is blank`() {
        listOf(
            CreditsLinks.TRANSTRACKS_REPO,
            CreditsLinks.OPENTRANSITION_REPO,
            CreditsLinks.OPENTRANSITION_CONTRIBUTORS,
            CreditsLinks.SHELBEELY_GITHUB,
            CreditsLinks.GPL_V3_LICENSE,
            CreditsLinks.OPENTRANSITION_CREDITS_DOCS,
            CreditsLinks.OPENTRANSITION_PRIVACY_DOCS,
        ).forEach { url ->
            assertNotNull(url)
            assertTrue("URL should not be blank", url.isNotBlank())
        }
    }
}
