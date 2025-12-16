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

import android.media.MediaRecorder
import java.io.File
import java.io.IOException

/**
 * Utility class for recording audio using MediaRecorder.
 */
class AudioRecorderUtil {
    
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false
    
    /**
     * Starts recording audio to the specified file.
     * 
     * @param file The file where audio will be saved
     * @return true if recording started successfully, false otherwise
     */
    fun startRecording(file: File): Boolean {
        return try {
            outputFile = file
            
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(file.absolutePath)
                
                prepare()
                start()
            }
            
            isRecording = true
            true
        } catch (e: IOException) {
            e.printStackTrace()
            release()
            false
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            release()
            false
        }
    }
    
    /**
     * Stops the current recording.
     * 
     * @return The file containing the recording, or null if there was an error
     */
    fun stopRecording(): File? {
        return try {
            if (isRecording) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                isRecording = false
                outputFile
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            release()
            null
        }
    }
    
    /**
     * Cancels the recording and deletes the output file.
     */
    fun cancelRecording() {
        try {
            if (isRecording) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                isRecording = false
            }
            outputFile?.delete()
            outputFile = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Releases resources associated with the recorder.
     */
    fun release() {
        try {
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Checks if currently recording.
     */
    fun isRecording(): Boolean = isRecording
    
    /**
     * Gets the current output file.
     */
    fun getOutputFile(): File? = outputFile
}
