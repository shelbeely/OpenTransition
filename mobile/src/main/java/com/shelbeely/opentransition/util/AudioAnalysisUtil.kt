/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.util

import com.shelbeely.opentransition.data.AudioAnalysis
import java.io.File
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Utility for analysing audio files to extract real on-device voice metrics.
 *
 * Metrics are derived by decoding the audio to PCM ([AudioDecoder]) then running
 * the normalised-autocorrelation pitch tracker ([PitchTracker]) over overlapping
 * frames.  All returned values are actual measurements of the provided file — no
 * hardcoded constants or faked "typical" values.
 *
 * The legacy formant fields (f1Mean … f4Mean) are retained in the data model for
 * backwards-compatibility with backups, but are zeroed out here because LPC-based
 * formant extraction is not yet implemented.
 */
object AudioAnalysisUtil {

    /**
     * Analyses [audioFile] and returns an [AudioAnalysis] with the following
     * real measurements:
     *
     * - `f0Mean`, `f0Min`, `f0Max`, `f0StdDev` — pitch statistics over voiced frames
     * - `pitchConfidenceMean` — mean autocorrelation confidence of voiced frames
     * - `voicedRatio` — fraction of frames classified as voiced (0–1)
     * - `intensityMeanDb` / `intensityMaxDb` — RMS levels across all frames
     * - `pitchRangeHz` — f0Max − f0Min
     * - `pitchStabilityScore` — 1 − normalised std-dev; 1 = very steady
     * - `intonationMovement` — mean abs frame-to-frame F0 delta (voiced frames)
     * - `durationSeconds` — recording duration
     *
     * Returns `null` if the file cannot be decoded or yields no voiced frames.
     */
    fun analyzeAudioFile(audioFile: File): AudioAnalysis? {
        if (!audioFile.exists() || audioFile.length() == 0L) return null

        val (samples, sampleRate) = AudioDecoder.decodeAudioToPcm(audioFile) ?: return null
        val frames = PitchTracker.analyzeFrames(samples, sampleRate)
        if (frames.isEmpty()) return null

        val durationSeconds = samples.size.toFloat() / sampleRate

        // ── Voiced frames ─────────────────────────────────────────────────
        val voicedFrames = frames.filter { it.isVoiced && it.f0Hz > 0f }
        val voicedRatio = frames.size.takeIf { it > 0 }
            ?.let { voicedFrames.size.toFloat() / it } ?: 0f

        // ── Pitch statistics (voiced only) ────────────────────────────────
        val f0Mean: Float
        val f0Min: Float
        val f0Max: Float
        val f0StdDev: Float
        val pitchConfidenceMean: Float
        val pitchRangeHz: Float
        val pitchStabilityScore: Float
        val intonationMovement: Float

        if (voicedFrames.isEmpty()) {
            f0Mean = 0f; f0Min = 0f; f0Max = 0f; f0StdDev = 0f
            pitchConfidenceMean = 0f; pitchRangeHz = 0f
            pitchStabilityScore = 0f; intonationMovement = 0f
        } else {
            f0Mean = voicedFrames.map { it.f0Hz }.average().toFloat()
            f0Min  = voicedFrames.minOf { it.f0Hz }
            f0Max  = voicedFrames.maxOf { it.f0Hz }

            val variance = voicedFrames.map { (it.f0Hz - f0Mean) * (it.f0Hz - f0Mean) }.average()
            f0StdDev = sqrt(variance).toFloat()

            pitchConfidenceMean = voicedFrames.map { it.confidence }.average().toFloat()
            pitchRangeHz = f0Max - f0Min

            // Stability: 1 − (stdDev / mean); clamped to [0, 1].
            // A perfectly steady voice has stdDev ≈ 0 → score ≈ 1.
            pitchStabilityScore = if (f0Mean > 0f)
                (1f - (f0StdDev / f0Mean)).coerceIn(0f, 1f) else 0f

            // Intonation movement: mean |Δf0| between consecutive voiced frames
            val voicedF0s = voicedFrames.map { it.f0Hz }
            intonationMovement = if (voicedF0s.size >= 2) {
                voicedF0s.zipWithNext { a, b -> abs(b - a) }.average().toFloat()
            } else 0f
        }

        // ── Intensity statistics (all frames) ─────────────────────────────
        val intensityMeanDb = frames.map { it.rmsDb }.average().toFloat()
        val intensityMaxDb  = frames.maxOf { it.rmsDb }

        return AudioAnalysis().apply {
            this.f0Mean               = f0Mean
            this.f0Min                = f0Min
            this.f0Max                = f0Max
            this.f0StdDev             = f0StdDev
            this.pitchConfidenceMean  = pitchConfidenceMean
            this.voicedRatio          = voicedRatio
            this.intensityMeanDb      = intensityMeanDb
            this.intensityMaxDb       = intensityMaxDb
            this.pitchRangeHz         = pitchRangeHz
            this.pitchStabilityScore  = pitchStabilityScore
            this.intonationMovement   = intonationMovement
            this.durationSeconds      = durationSeconds
            this.analysisTimestamp    = System.currentTimeMillis()
            // Formant fields left at 0 — LPC extraction not yet implemented
        }
    }

    /**
     * Generates a human-readable voice analysis report from real measurements.
     */
    fun generateReport(analysis: AudioAnalysis): String {
        return buildString {
            appendLine("Voice Analysis Report")
            appendLine("=".repeat(40))
            appendLine()

            appendLine("Pitch (F0):")
            appendLine("  Average: ${String.format(Locale.US, "%.1f", analysis.f0Mean)} Hz")
            appendLine(
                "  Range: ${String.format(Locale.US, "%.1f", analysis.f0Min)}" +
                "–${String.format(Locale.US, "%.1f", analysis.f0Max)} Hz" +
                " (${String.format(Locale.US, "%.1f", analysis.pitchRangeHz)} Hz span)"
            )
            appendLine("  Variability (std dev): ${String.format(Locale.US, "%.1f", analysis.f0StdDev)} Hz")
            appendLine("  Stability score: ${String.format(Locale.US, "%.2f", analysis.pitchStabilityScore)} (0–1)")
            appendLine("  Intonation movement: ${String.format(Locale.US, "%.1f", analysis.intonationMovement)} Hz/frame")
            appendLine("  Confidence: ${String.format(Locale.US, "%.2f", analysis.pitchConfidenceMean)}")
            appendLine()

            appendLine("Voice activity:")
            appendLine("  Voiced: ${String.format(Locale.US, "%.0f", analysis.voicedRatio * 100f)}% of recording")
            appendLine()

            appendLine("Intensity:")
            appendLine("  Mean: ${String.format(Locale.US, "%.1f", analysis.intensityMeanDb)} dBFS")
            appendLine("  Peak: ${String.format(Locale.US, "%.1f", analysis.intensityMaxDb)} dBFS")
            appendLine()

            appendLine("Recording Duration: ${String.format(Locale.US, "%.1f", analysis.durationSeconds)} seconds")
        }
    }
}
