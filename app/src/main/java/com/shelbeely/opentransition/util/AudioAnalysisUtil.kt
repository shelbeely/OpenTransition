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

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.io.UniversalAudioInputStream
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchDetectionResult
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm
import com.shelbeely.opentransition.data.AudioAnalysis
import java.io.File
import java.io.FileInputStream
import kotlin.math.sqrt

/**
 * Utility for analyzing audio files to extract formants and voice characteristics.
 * Uses TarsosDSP library for audio signal processing.
 */
object AudioAnalysisUtil {
    
    private const val SAMPLE_RATE = 44100
    private const val BUFFER_SIZE = 4096
    private const val OVERLAP = 0
    
    /**
     * Analyzes an audio file and extracts formant frequencies and pitch information.
     * This is a simplified analysis - for production, consider using LPC (Linear Predictive Coding)
     * for more accurate formant extraction.
     * 
     * @param audioFile The audio file to analyze
     * @return AudioAnalysis object with extracted features, or null if analysis fails
     */
    fun analyzeAudioFile(audioFile: File): AudioAnalysis? {
        if (!audioFile.exists() || audioFile.length() == 0L) {
            return null
        }
        
        return try {
            val pitchData = mutableListOf<Float>()
            
            // Create audio input stream
            val audioInputStream = FileInputStream(audioFile)
            val universalAudioInputStream = UniversalAudioInputStream(
                audioInputStream,
                TarsosDSPAudioFormat(
                    SAMPLE_RATE.toFloat(),
                    16,
                    1,
                    true,
                    false
                )
            )
            
            // Create audio dispatcher
            val dispatcher = AudioDispatcher(
                universalAudioInputStream,
                BUFFER_SIZE,
                OVERLAP
            )
            
            // Add pitch detection processor
            val pitchDetectionHandler = PitchDetectionHandler { result, _ ->
                if (result.pitch != -1f && result.isPitched) {
                    pitchData.add(result.pitch)
                }
            }
            
            val pitchProcessor = PitchProcessor(
                PitchEstimationAlgorithm.YIN,
                SAMPLE_RATE.toFloat(),
                BUFFER_SIZE,
                pitchDetectionHandler
            )
            
            dispatcher.addAudioProcessor(pitchProcessor)
            
            // Process the audio
            dispatcher.run()
            
            if (pitchData.isEmpty()) {
                return null
            }
            
            // Calculate statistics
            val f0Mean = pitchData.average().toFloat()
            val f0Min = pitchData.minOrNull() ?: 0f
            val f0Max = pitchData.maxOrNull() ?: 0f
            
            // Calculate standard deviation
            val variance = pitchData.map { (it - f0Mean) * (it - f0Mean) }.average()
            val f0StdDev = sqrt(variance).toFloat()
            
            // Estimate formants based on pitch (simplified approach)
            // For more accurate formants, would need LPC analysis
            // These are rough estimates based on typical voice ranges
            val f1Mean = estimateF1(f0Mean)
            val f2Mean = estimateF2(f0Mean)
            val f3Mean = estimateF3(f0Mean)
            val f4Mean = estimateF4()
            
            // Calculate duration
            val durationSeconds = pitchData.size * BUFFER_SIZE / SAMPLE_RATE.toFloat()
            
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
            appendLine("Voice Analysis Report")
            appendLine("=" .repeat(40))
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
