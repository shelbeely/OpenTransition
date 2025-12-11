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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for ContributorsUtil
 */
class ContributorsUtilTest {
    
    @Test
    fun getContributors_returnsNonEmptyList() {
        val contributors = ContributorsUtil.getContributors()
        assertTrue("Contributors list should not be empty", contributors.isNotEmpty())
    }
    
    @Test
    fun getContributors_percentagesSumTo100() {
        val contributors = ContributorsUtil.getContributors()
        val totalPercentage = contributors.sumOf { it.percentage }
        assertEquals("Percentages should sum to 100", 100.0, totalPercentage, 0.01)
    }
    
    @Test
    fun getTotalCommits_matchesSumOfIndividualCommits() {
        val totalCommits = ContributorsUtil.getTotalCommits()
        val sumOfCommits = ContributorsUtil.getContributors().sumOf { it.commits }
        assertEquals("Total commits should match sum of individual commits", sumOfCommits, totalCommits)
    }
    
    @Test
    fun contributor_hasValidData() {
        val contributors = ContributorsUtil.getContributors()
        contributors.forEach { contributor ->
            assertTrue("Contributor name should not be empty", contributor.name.isNotEmpty())
            assertTrue("Commits should be positive", contributor.commits > 0)
            assertTrue("Percentage should be between 0 and 100", contributor.percentage >= 0 && contributor.percentage <= 100)
        }
    }
}
