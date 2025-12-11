/*
 * Copyright © 2018-2023 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.util

/**
 * Data class representing a contributor's statistics
 */
data class Contributor(
    val name: String,
    val commits: Int,
    val percentage: Double
)

/**
 * Utility object to manage contributor information
 * 
 * Note: This data should be updated periodically to reflect current contribution statistics.
 * To update, run: git shortlog -sn --all
 */
object ContributorsUtil {
    
    /**
     * Returns the list of contributors sorted by contribution percentage (descending)
     */
    fun getContributors(): List<Contributor> {
        val contributors = listOf(
            Contributor("Shelbee Johnson", 1, 50.0),
            Contributor("copilot-swe-agent[bot]", 1, 50.0)
        )
        return contributors
    }
    
    /**
     * Returns the total number of commits across all contributors
     */
    fun getTotalCommits(): Int {
        return getContributors().sumOf { it.commits }
    }
}
