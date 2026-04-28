/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.voicegoal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.util.VoiceMetric
import java.util.Locale

/**
 * Composable content for the "create a voice goal" bottom sheet.
 *
 * Rules:
 * - No presets or suggested values for min/max.
 * - No gendered defaults.
 * - The name field is free text; the user decides what the goal means to them.
 * - Metric picker uses plain labels from [VoiceMetric.label].
 * - Two sliders set min and max; sliders are initialised to the midpoint of the
 *   plausible range so the user must consciously choose their own values.
 *
 * @param onSave    Called with the filled-in fields when the user confirms.
 * @param onDismiss Called when the user cancels.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalCreationSheet(
    onSave: (name: String, metric: VoiceMetric, targetMin: Float, targetMax: Float) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedMetric by remember { mutableStateOf(VoiceMetric.F0_MEAN) }
    var metricMenuExpanded by remember { mutableStateOf(false) }

    // Slider range depends on metric
    val (absMin, absMax) = metricSliderRange(selectedMetric)
    var targetMin by remember(selectedMetric) { mutableFloatStateOf((absMin + absMax) / 2f) }
    var targetMax by remember(selectedMetric) { mutableFloatStateOf((absMin + absMax) / 2f + (absMax - absMin) * 0.1f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.goal_creation_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        // ── Name ──────────────────────────────────────────────────────────
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.goal_name_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        // ── Metric picker ─────────────────────────────────────────────────
        Text(
            text = stringResource(R.string.goal_metric_label),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = metricMenuExpanded,
            onExpandedChange = { metricMenuExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedMetric.label,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = metricMenuExpanded) },
                modifier = Modifier
                    .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = metricMenuExpanded,
                onDismissRequest = { metricMenuExpanded = false }
            ) {
                VoiceMetric.entries.forEach { metric ->
                    DropdownMenuItem(
                        text = { Text(metric.label) },
                        onClick = {
                            selectedMetric = metric
                            metricMenuExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // ── Min slider ────────────────────────────────────────────────────
        Text(
            text = stringResource(
                R.string.goal_target_min_label,
                formatValue(targetMin, selectedMetric)
            ),
            style = MaterialTheme.typography.bodyMedium
        )
        Slider(
            value = targetMin,
            onValueChange = { v -> targetMin = v.coerceAtMost(targetMax) },
            valueRange = absMin..absMax,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // ── Max slider ────────────────────────────────────────────────────
        Text(
            text = stringResource(
                R.string.goal_target_max_label,
                formatValue(targetMax, selectedMetric)
            ),
            style = MaterialTheme.typography.bodyMedium
        )
        Slider(
            value = targetMax,
            onValueChange = { v -> targetMax = v.coerceAtLeast(targetMin) },
            valueRange = absMin..absMax,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        // ── Actions ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.cancel))
            }
            Spacer(modifier = Modifier.weight(0.1f))
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), selectedMetric, targetMin, targetMax)
                    }
                },
                enabled = name.isNotBlank() && targetMin <= targetMax,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

/** Plausible [min, max] for each metric's slider. */
private fun metricSliderRange(metric: VoiceMetric): Pair<Float, Float> = when (metric) {
    VoiceMetric.F0_MEAN               -> 50f to 600f
    VoiceMetric.PITCH_RANGE_HZ        -> 0f to 400f
    VoiceMetric.PITCH_STABILITY_SCORE -> 0f to 1f
    VoiceMetric.VOICED_RATIO          -> 0f to 1f
    VoiceMetric.INTONATION_MOVEMENT   -> 0f to 100f
}

private fun formatValue(value: Float, metric: VoiceMetric): String = when (metric) {
    VoiceMetric.PITCH_STABILITY_SCORE -> String.format(Locale.US, "%.2f", value)
    VoiceMetric.VOICED_RATIO -> "${(value * 100).toInt()}%"
    else -> "${value.toInt()} ${metric.unit}"
}
