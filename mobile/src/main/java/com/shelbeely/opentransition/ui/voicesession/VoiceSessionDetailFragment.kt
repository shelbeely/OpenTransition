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
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.shelbeely.opentransition.util.VoiceMetric
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
 * them to the Compose screen.  Playback progress is polled at ~30 fps via a
 * [Handler] runnable so the spectrogram cursor stays in sync.
 */
class VoiceSessionDetailFragment : Fragment() {

    private val args: VoiceSessionDetailFragmentArgs by navArgs()

    private var analysis         by mutableStateOf<AudioAnalysisEntity?>(null)
    private var isLoading        by mutableStateOf(true)
    private var audioFilePath    by mutableStateOf("")
    private var goalHits         by mutableStateOf<List<VoiceGoalEntity>>(emptyList())
    private var isPlaying        by mutableStateOf(false)
    private var playbackProgress by mutableFloatStateOf(0f)

    private val handler = Handler(Looper.getMainLooper())
    private val progressRunnable = object : Runnable {
        override fun run() {
            val audioId = args.photoId
            val playing = AudioPlayerManager.isPlaying(audioId)
            isPlaying = playing
            if (playing) {
                playbackProgress = AudioPlayerManager.getProgress(audioId)
                handler.postDelayed(this, 33L)   // ~30 fps
            }
        }
    }

    private val playbackListener = object : AudioPlayerManager.PlaybackListener {
        override fun onPlaybackStateChanged(audioId: String, playing: Boolean) {
            if (audioId != args.photoId) return
            isPlaying = playing
            if (playing) handler.post(progressRunnable)
            else handler.removeCallbacks(progressRunnable)
        }
        override fun onPlaybackProgress(audioId: String, position: Int, duration: Int) { /* polled */ }
        override fun onPlaybackCompleted(audioId: String) {
            if (audioId != args.photoId) return
            isPlaying = false
            playbackProgress = 0f
            handler.removeCallbacks(progressRunnable)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).also { cv ->
        cv.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        cv.setContent {
            OpenTransitionTheme(colorVariant = SettingsManager.getResolvedComposeColorVariant()) {
                val a = analysis
                when {
                    isLoading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    a != null -> VoiceSessionDetailScreen(
                        analysis          = a,
                        audioFilePath     = audioFilePath,
                        playbackProgress  = playbackProgress,
                        isPlaying         = isPlaying,
                        goalHits          = goalHits,
                        onPlayPause       = { togglePlayback() },
                        onBack            = { findNavController().popBackStack() }
                    )
                }
            }
        }
    }

    private fun togglePlayback() {
        val file = File(audioFilePath)
        if (!file.exists()) return
        AudioPlayerManager.togglePlayback(args.photoId, file)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AudioPlayerManager.setPlaybackListener(playbackListener)
        isPlaying = AudioPlayerManager.isPlaying(args.photoId)
        if (isPlaying) handler.post(progressRunnable)
        loadData()
    }

    private fun loadData() {
        val photoId = args.photoId
        val db = DatabaseManager.getDatabase(requireContext())

        lifecycleScope.launch {
            val entity = withContext(Dispatchers.IO) {
                db.audioAnalysisDao().getAudioAnalysisByPhotoId(photoId)
            }
            val photoEntity = withContext(Dispatchers.IO) {
                db.photoDao().getPhotoById(photoId)
            }

            analysis      = entity
            audioFilePath = photoEntity?.filePath ?: ""

            // Determine which goals this session hit across all metrics
            val hitGoals = mutableListOf<VoiceGoalEntity>()
            if (entity != null) {
                withContext(Dispatchers.IO) {
                    for (m in VoiceMetric.entries) {
                        val goals = db.voiceGoalDao().getGoalsForMetric(m.key)
                        val value = when (m) {
                            VoiceMetric.F0_MEAN               -> entity.f0Mean
                            VoiceMetric.PITCH_RANGE_HZ        -> entity.pitchRangeHz
                            VoiceMetric.PITCH_STABILITY_SCORE -> entity.pitchStabilityScore
                            VoiceMetric.VOICED_RATIO          -> entity.voicedRatio
                            VoiceMetric.INTONATION_MOVEMENT   -> entity.intonationMovement
                        }
                        goals.filter { g -> value in g.targetMin..g.targetMax }
                            .forEach { hitGoals.add(it) }
                    }
                }
            }
            goalHits  = hitGoals
            isLoading = false
        }
    }

    override fun onDestroyView() {
        handler.removeCallbacks(progressRunnable)
        AudioPlayerManager.setPlaybackListener(null)
        super.onDestroyView()
    }
}
