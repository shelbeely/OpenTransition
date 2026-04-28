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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.database.room.entities.AudioAnalysisEntity
import com.shelbeely.opentransition.database.room.entities.VoiceGoalEntity
import com.shelbeely.opentransition.ui.voicegoal.GoalCreationSheet
import com.shelbeely.opentransition.ui.widget.PitchProgressionView
import com.shelbeely.opentransition.util.VoiceMetric
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Progress screen for voice metrics over time.
 *
 * Shows a [PitchProgressionView] plotting one metric across all recordings.
 * Toggle chips above the chart let the user switch between the five supported
 * metrics.  If a [VoiceGoalEntity] exists for the selected metric, data points
 * that fell within the goal range are decorated with a ring.
 *
 * A FAB opens [GoalCreationSheet] in a [ModalBottomSheet] so the user can
 * create a new goal without leaving the screen.
 *
 * No reference lines, zone backgrounds, or gendered defaults are ever shown.
 *
 * @param analyses    All [AudioAnalysisEntity] rows available for charting.
 * @param goals       All [VoiceGoalEntity] rows — filtered to [selectedMetric] inside.
 * @param onSaveGoal  Called when the user saves a new goal from the bottom sheet.
 * @param onBack      Navigate up.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VoiceProgressScreen(
    analyses: List<AudioAnalysisEntity>,
    goals: List<VoiceGoalEntity>,
    onSaveGoal: (name: String, metric: VoiceMetric, min: Float, max: Float) -> Unit,
    onBack: () -> Unit
) {
    var selectedMetric by remember { mutableStateOf(VoiceMetric.F0_MEAN) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            GoalCreationSheet(
                onSave = { name, metric, min, max ->
                    onSaveGoal(name, metric, min, max)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                },
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.voice_progress_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.voice_progress_add_goal)) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // ── Metric toggle chips ───────────────────────────────────────
            Text(
                text = stringResource(R.string.voice_progress_metric_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                VoiceMetric.entries.forEach { metric ->
                    FilterChip(
                        selected = metric == selectedMetric,
                        onClick = { selectedMetric = metric },
                        label = { Text(metric.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Chart ─────────────────────────────────────────────────────
            if (analyses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.voice_progress_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                // Build (LocalDate, entity) pairs once; PitchProgressionView sorts by date.
                val dated: List<Pair<LocalDate, AudioAnalysisEntity>> = remember(analyses) {
                    analyses.mapNotNull { entity ->
                        val ts = entity.analysisTimestamp ?: return@mapNotNull null
                        val date = Instant.ofEpochMilli(ts)
                            .atZone(ZoneId.systemDefault()).toLocalDate()
                        date to entity
                    }
                }
                AndroidView(
                    factory = { ctx -> PitchProgressionView(ctx) },
                    update = { chart ->
                        chart.setMetric(selectedMetric)
                        chart.setData(dated, goals)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                )
            }
        }
    }
}
