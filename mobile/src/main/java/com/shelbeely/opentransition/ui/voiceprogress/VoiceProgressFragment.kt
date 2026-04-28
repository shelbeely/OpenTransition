/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.voiceprogress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.shelbeely.opentransition.database.DatabaseManager
import com.shelbeely.opentransition.database.room.entities.AudioAnalysisEntity
import com.shelbeely.opentransition.database.room.entities.VoiceGoalEntity
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme
import com.shelbeely.opentransition.util.VoiceMetric
import com.shelbeely.opentransition.util.settings.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Fragment that hosts [VoiceProgressScreen].
 *
 * Loads all audio analyses and user-defined goals from Room on startup.
 * Exposes a "save goal" callback that persists via [VoiceGoalRepository]
 * and immediately refreshes the goal list so the chart decorations update.
 */
class VoiceProgressFragment : Fragment() {

    private var analyses by mutableStateOf<List<AudioAnalysisEntity>>(emptyList())
    private var goals    by mutableStateOf<List<VoiceGoalEntity>>(emptyList())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).also { cv ->
        cv.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        cv.setContent {
            OpenTransitionTheme(colorVariant = SettingsManager.getResolvedComposeColorVariant()) {
                VoiceProgressScreen(
                    analyses    = analyses,
                    goals       = goals,
                    onSaveGoal  = { name, metric, min, max -> saveGoal(name, metric, min, max) },
                    onBack      = { findNavController().popBackStack() }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
    }

    private fun loadData() {
        val db = DatabaseManager.getDatabase(requireContext())
        lifecycleScope.launch {
            analyses = withContext(Dispatchers.IO) { db.audioAnalysisDao().getAllAudioAnalysesList() }
            goals    = withContext(Dispatchers.IO) { db.voiceGoalDao().getAllGoalsList() }
        }
    }

    private fun saveGoal(name: String, metric: VoiceMetric, min: Float, max: Float) {
        val db = DatabaseManager.getDatabase(requireContext())
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                db.voiceGoalDao().insertGoal(
                    VoiceGoalEntity(
                        id         = java.util.UUID.randomUUID().toString(),
                        name       = name,
                        metricKey  = metric.key,
                        targetMin  = min,
                        targetMax  = max,
                        createdAt  = System.currentTimeMillis()
                    )
                )
            }
            // Refresh goals so chart decorations update immediately
            goals = withContext(Dispatchers.IO) { db.voiceGoalDao().getAllGoalsList() }
        }
    }
}
