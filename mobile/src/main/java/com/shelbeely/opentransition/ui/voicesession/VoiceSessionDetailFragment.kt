/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.voicesession

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.shelbeely.opentransition.database.DatabaseManager
import com.shelbeely.opentransition.database.room.entities.AudioAnalysisEntity
import com.shelbeely.opentransition.database.room.entities.VoiceGoalEntity
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme
import com.shelbeely.opentransition.util.AudioPlayerManager
import com.shelbeely.opentransition.util.settings.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Fragment that hosts [VoiceSessionDetailScreen].
 *
 * Receives the `photoId` of the audio item via SafeArgs, loads the
 * [AudioAnalysisEntity] + matched [VoiceGoalEntity] rows from Room, and passes
 * them to the Compose screen.  Playback progress is forwarded from
 * [AudioPlayerManager] so the cursor on the spectrogram stays in sync.
 */
class VoiceSessionDetailFragment : Fragment() {

    private val args: VoiceSessionDetailFragmentArgs by navArgs()

    private var analysis by mutableStateOf<AudioAnalysisEntity?>(null)
    private var audioFilePath by mutableStateOf("")
    private var goalHits by mutableStateOf<List<VoiceGoalEntity>>(emptyList())
    private var playbackProgress by mutableFloatStateOf(0f)

    private var playerManager: AudioPlayerManager? = null
    private var progressRunnable: Runnable? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).also { cv ->
        cv.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        cv.setContent {
            OpenTransitionTheme(colorVariant = SettingsManager.getResolvedComposeColorVariant()) {
                val a = analysis
                if (a != null) {
                    VoiceSessionDetailScreen(
                        analysis         = a,
                        audioFilePath    = audioFilePath,
                        playbackProgress = playbackProgress,
                        goalHits         = goalHits,
                        onBack           = { findNavController().popBackStack() }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
    }

    private fun loadData() {
        val photoId = args.photoId
        val db = DatabaseManager.getDatabase(requireContext())

        lifecycleScope.launch {
            val entity = withContext(Dispatchers.IO) {
                db.audioAnalysisDao().getAudioAnalysisByPhotoId(photoId)
            } ?: return@launch

            val photoEntity = withContext(Dispatchers.IO) {
                db.photoDao().getPhotoById(photoId)
            }

            analysis      = entity
            audioFilePath = photoEntity?.filePath ?: ""

            // Determine which goals this session hit across all metrics
            val hitGoals = mutableListOf<VoiceGoalEntity>()
            withContext(Dispatchers.IO) {
                val metrics = com.shelbeely.opentransition.util.VoiceMetric.values()
                for (m in metrics) {
                    val goals = db.voiceGoalDao().getGoalsForMetric(m.key)
                    val value = when (m) {
                        com.shelbeely.opentransition.util.VoiceMetric.F0_MEAN ->
                            entity.f0Mean
                        com.shelbeely.opentransition.util.VoiceMetric.PITCH_RANGE_HZ ->
                            entity.pitchRangeHz
                        com.shelbeely.opentransition.util.VoiceMetric.PITCH_STABILITY_SCORE ->
                            entity.pitchStabilityScore
                        com.shelbeely.opentransition.util.VoiceMetric.VOICED_RATIO ->
                            entity.voicedRatio
                        com.shelbeely.opentransition.util.VoiceMetric.INTONATION_MOVEMENT ->
                            entity.intonationMovement
                    }
                    goals.filter { g -> value in g.targetMin..g.targetMax }
                        .forEach { hitGoals.add(it) }
                }
            }
            goalHits = hitGoals
        }
    }

    override fun onDestroyView() {
        progressRunnable?.let { handler.removeCallbacks(it) }
        playerManager?.release()
        super.onDestroyView()
    }
}
