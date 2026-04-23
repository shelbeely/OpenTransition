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

import android.media.MediaExtractor
import android.media.MediaFormat
import com.shelbeely.opentransition.data.AudioAnalysis
import java.io.File

/**
 * Utility for analyzing audio files to extract formants and voice characteristics.
 *
 * **Status: Preview / placeholder.** This implementation does **not** perform real
 * formant extraction. It returns typical adult-speaker pitch and formant values
 * regardless of the contents of [analyzeAudioFile]. Real on-device DSP (LPC /
 * cepstrum analysis, e.g. via TarsosDSP) is planned but not yet integrated.
 *
 * Callers must surface this limitation to the user — see the preview disclaimer
 * in `RecordAudioScreen` and the README feature list.
 *
 * See audit-report/07-issues-and-bugs.md ISSUE-005.
 */
object AudioAnalysisUtil {
    
    /**
     * Analyzes an audio file and extracts estimated formant frequencies and pitch information.
     * This is a simplified analysis - for production, consider using LPC (Linear Predictive Coding)
     * for more accurate formant extraction with a DSP library.
     * 
     * @param audioFile The audio file to analyze
     * @return AudioAnalysis object with estimated features, or null if analysis fails
     */
    fun analyzeAudioFile(audioFile: File): AudioAnalysis? {
        if (!audioFile.exists() || audioFile.length() == 0L) {
            return null
        }
        
        return try {
            val extractor = MediaExtractor()
            extractor.setDataSource(audioFile.absolutePath)
            
            // Get audio format information
            var audioFormat: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith("audio/") == true) {
                    audioFormat = format
                    break
                }
            }
            
            if (audioFormat == null) {
                extractor.release()
                return null
            }
            
            // Get duration
            val durationUs = audioFormat.getLong(MediaFormat.KEY_DURATION)
            val durationSeconds = durationUs / 1_000_000f
            
            // Estimate formants based on file metadata
            // This is a simplified approach - real formant analysis requires DSP
            // For now, we'll use typical average values
            // TODO: Integrate proper DSP library for real formant extraction
            val f0Mean = 150f // Typical average pitch
            val f0Min = 120f
            val f0Max = 180f
            val f0StdDev = 20f
            
            // Typical formant values (these are averages, not actual analysis)
            val f1Mean = estimateF1(f0Mean)
            val f2Mean = estimateF2(f0Mean)
            val f3Mean = estimateF3(f0Mean)
            val f4Mean = estimateF4()
            
            extractor.release()
            
            AudioAnalysis().apply {
                this.f0Mean = f0Mean
                this.f0Min = f0Min
                this.f0Max = f0Max
                this.f1Mean = f1Mean
                this.f2Mean = f2Mean
                this.f3Mean = f3Mean
                this.f4Mean = f4Mean
                this.f0StdDev = f0StdDev
                this.durationSeconds = durationSeconds
                this.analysisTimestamp = System.currentTimeMillis()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Estimates first formant (F1) based on fundamental frequency.
     * F1 is related to tongue height and typically ranges 200-1000 Hz.
     * Lower F1 suggests higher tongue position.
     */
    private fun estimateF1(f0: Float): Float {
        // Typical F1 for adult speakers
        // This is a simplified estimation - real F1 extraction requires LPC
        return when {
            f0 < 150 -> 500f  // Lower pitch (typically masculine)
            f0 > 200 -> 700f  // Higher pitch (typically feminine)
            else -> 600f
        }
    }
    
    /**
     * Estimates second formant (F2) based on fundamental frequency.
     * F2 is related to tongue frontness/backness and typically ranges 800-2500 Hz.
     * Higher F2 suggests more forward tongue position.
     */
    private fun estimateF2(f0: Float): Float {
        // Typical F2 for adult speakers
        return when {
            f0 < 150 -> 1200f  // Lower pitch
            f0 > 200 -> 1800f  // Higher pitch
            else -> 1500f
        }
    }
    
    /**
     * Estimates third formant (F3).
     * F3 typically ranges 2000-3500 Hz.
     */
    private fun estimateF3(f0: Float): Float {
        return when {
            f0 < 150 -> 2500f
            f0 > 200 -> 3000f
            else -> 2750f
        }
    }
    
    /**
     * Estimates fourth formant (F4).
     * F4 typically ranges 3000-4500 Hz.
     */
    private fun estimateF4(): Float {
        return 3500f
    }
    
    /**
     * Generates a human-readable report from audio analysis.
     */
    fun generateReport(analysis: AudioAnalysis): String {
        return buildString {
            appendLine("Voice Analysis Report (Preview)")
            appendLine("=".repeat(40))
            appendLine()
            appendLine("⚠ The values below are typical estimates, not measurements")
            appendLine("  of this recording. Real on-device DSP analysis is planned")
            appendLine("  for a future release.")
            appendLine()
            
            appendLine("Pitch Analysis:")
            appendLine("  Average Pitch (F0): ${String.format("%.1f", analysis.f0Mean)} Hz")
            appendLine("  Pitch Range: ${String.format("%.1f", analysis.f0Min)} - ${String.format("%.1f", analysis.f0Max)} Hz")
            appendLine("  Pitch Variability: ${String.format("%.1f", analysis.f0StdDev)} Hz")
            appendLine()
            
            appendLine("Formant Frequencies:")
            appendLine("  F1 (Tongue Height): ${String.format("%.0f", analysis.f1Mean)} Hz")
            appendLine("  F2 (Tongue Position): ${String.format("%.0f", analysis.f2Mean)} Hz")
            appendLine("  F3: ${String.format("%.0f", analysis.f3Mean)} Hz")
            appendLine("  F4: ${String.format("%.0f", analysis.f4Mean)} Hz")
            appendLine()
            
            appendLine("Recording Duration: ${String.format("%.1f", analysis.durationSeconds)} seconds")
            appendLine()
            
            appendLine("Note: Formant values are estimated. For accurate analysis,")
            appendLine("consider using specialized voice analysis software.")
            appendLine()
            
            // Provide interpretation
            appendLine("Interpretation:")
            when {
                analysis.f0Mean < 130 -> appendLine("  - Pitch is in typical masculine range")
                analysis.f0Mean > 180 -> appendLine("  - Pitch is in typical feminine range")
                else -> appendLine("  - Pitch is in androgynous range")
            }
            
            when {
                analysis.f1Mean < 550 -> appendLine("  - F1 suggests higher tongue position")
                analysis.f1Mean > 650 -> appendLine("  - F1 suggests lower tongue position")
            }
            
            when {
                analysis.f2Mean < 1400 -> appendLine("  - F2 suggests back tongue position")
                analysis.f2Mean > 1600 -> appendLine("  - F2 suggests forward tongue position")
            }
        }
    }
}
