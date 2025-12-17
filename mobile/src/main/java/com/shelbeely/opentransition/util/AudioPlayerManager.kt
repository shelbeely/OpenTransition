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

import android.media.MediaPlayer
import java.io.File

/**
 * Singleton manager for audio playback using MediaPlayer.
 * Ensures only one audio file plays at a time.
 */
object AudioPlayerManager {
    
    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingId: String? = null
    private var playbackListener: PlaybackListener? = null
    
    interface PlaybackListener {
        fun onPlaybackStateChanged(audioId: String, isPlaying: Boolean)
        fun onPlaybackProgress(audioId: String, position: Int, duration: Int)
        fun onPlaybackCompleted(audioId: String)
    }
    
    /**
     * Sets a listener for playback events.
     */
    fun setPlaybackListener(listener: PlaybackListener?) {
        playbackListener = listener
    }
    
    /**
     * Plays an audio file or pauses if already playing.
     * 
     * @param audioId Unique identifier for the audio
     * @param audioFile The audio file to play
     * @return true if playback started, false if paused
     */
    fun togglePlayback(audioId: String, audioFile: File): Boolean {
        // If this audio is currently playing, pause it
        if (currentPlayingId == audioId && mediaPlayer?.isPlaying == true) {
            pausePlayback()
            return false
        }
        
        // If a different audio is playing, stop it
        if (currentPlayingId != audioId && mediaPlayer?.isPlaying == true) {
            stopPlayback()
        }
        
        // Start or resume playback
        if (currentPlayingId == audioId && mediaPlayer != null) {
            // Resume existing MediaPlayer
            mediaPlayer?.start()
            playbackListener?.onPlaybackStateChanged(audioId, true)
            return true
        } else {
            // Create new MediaPlayer
            return startNewPlayback(audioId, audioFile)
        }
    }
    
    /**
     * Starts playback of a new audio file.
     */
    private fun startNewPlayback(audioId: String, audioFile: File): Boolean {
        return try {
            releaseMediaPlayer()
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFile.absolutePath)
                setOnCompletionListener {
                    playbackListener?.onPlaybackCompleted(audioId)
                    playbackListener?.onPlaybackStateChanged(audioId, false)
                    currentPlayingId = null
                }
                setOnErrorListener { _, what, extra ->
                    releaseMediaPlayer()
                    currentPlayingId = null
                    playbackListener?.onPlaybackStateChanged(audioId, false)
                    true
                }
                prepare()
                start()
            }
            
            currentPlayingId = audioId
            playbackListener?.onPlaybackStateChanged(audioId, true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            releaseMediaPlayer()
            false
        }
    }
    
    /**
     * Pauses the current playback.
     */
    fun pausePlayback() {
        try {
            mediaPlayer?.pause()
            currentPlayingId?.let { id ->
                playbackListener?.onPlaybackStateChanged(id, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Stops the current playback and releases resources.
     */
    fun stopPlayback() {
        val wasPlayingId = currentPlayingId
        releaseMediaPlayer()
        currentPlayingId = null
        wasPlayingId?.let { id ->
            playbackListener?.onPlaybackStateChanged(id, false)
        }
    }
    
    /**
     * Checks if a specific audio is currently playing.
     */
    fun isPlaying(audioId: String): Boolean {
        return currentPlayingId == audioId && mediaPlayer?.isPlaying == true
    }
    
    /**
     * Gets the current playback position in milliseconds.
     */
    fun getCurrentPosition(): Int {
        return try {
            mediaPlayer?.currentPosition ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Gets the total duration in milliseconds.
     */
    fun getDuration(): Int {
        return try {
            mediaPlayer?.duration ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Seeks to a specific position in the audio.
     */
    fun seekTo(position: Int) {
        try {
            mediaPlayer?.seekTo(position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Releases the MediaPlayer resources.
     */
    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
            mediaPlayer = null
        }
    }
    
    /**
     * Releases all resources. Should be called when the app is destroyed.
     */
    fun release() {
        stopPlayback()
        playbackListener = null
    }
}
