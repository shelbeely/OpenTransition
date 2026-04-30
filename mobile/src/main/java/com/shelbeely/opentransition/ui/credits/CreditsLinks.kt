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

/**
 * Single source of truth for attribution / credits URLs used in-app.
 *
 * Centralising these constants keeps Settings, the Credits screen, and the
 * Open-Source Licenses screen in sync, and prevents the original-TransTracks
 * archive URL from drifting again (it previously pointed at the wrong repo
 * slug and dead-ended for users who tapped it).
 */
object CreditsLinks {
    /**
     * The archived TransTracks-Android source repository. The original Play
     * Store listing was retired in 2025; the source remains available here
     * under GPL-3.0-or-later. Note the `-Android` suffix — the bare
     * `TransTracks/TransTracks` slug does not exist.
     */
    const val TRANSTRACKS_REPO = "https://github.com/TransTracks/TransTracks-Android"

    /** OpenTransition fork repository. */
    const val OPENTRANSITION_REPO = "https://github.com/shelbeely/OpenTransition"

    /** OpenTransition contributors page. */
    const val OPENTRANSITION_CONTRIBUTORS =
        "https://github.com/shelbeely/OpenTransition/graphs/contributors"

    /** Personal GitHub page for the fork maintainer. */
    const val SHELBEELY_GITHUB = "https://github.com/shelbeely"

    /** Canonical URL for the GPL-3.0 license text. */
    const val GPL_V3_LICENSE = "https://www.gnu.org/licenses/gpl-3.0.html"

    /** OpenTransition documentation site - credits page. */
    const val OPENTRANSITION_CREDITS_DOCS =
        "https://shelbeely.github.io/OpenTransition/credits/"

    /** OpenTransition documentation site - privacy page. */
    const val OPENTRANSITION_PRIVACY_DOCS =
        "https://shelbeely.github.io/OpenTransition/user-guide/privacy/"
}
