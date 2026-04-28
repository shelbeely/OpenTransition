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

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.io.File

/**
 * Decodes a compressed audio file (AAC / M4A / MP3 …) to mono 32-bit float PCM
 * using the platform [MediaCodec] API.
 *
 * Shared between [AudioAnalysisUtil] and [com.shelbeely.opentransition.ui.widget.SpectrogramView]
 * so that neither needs to duplicate the codec boilerplate.
 */
object AudioDecoder {

    /**
     * Decodes [file] to a mono float PCM array.
     *
     * @return `(samples, sampleRateHz)` or `null` if decoding fails or produces no output.
     */
    fun decodeAudioToPcm(file: File): Pair<FloatArray, Int>? {
        if (!file.exists() || file.length() == 0L) return null

        val extractor = MediaExtractor()
        extractor.setDataSource(file.absolutePath)

        var trackIndex = -1
        var mimeType = ""
        var sampleRate = 44100
        var channelCount = 1

        for (i in 0 until extractor.trackCount) {
            val fmt = extractor.getTrackFormat(i)
            val mime = fmt.getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith("audio/")) {
                trackIndex = i
                mimeType = mime
                sampleRate = fmt.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                channelCount = if (fmt.containsKey(MediaFormat.KEY_CHANNEL_COUNT))
                    fmt.getInteger(MediaFormat.KEY_CHANNEL_COUNT) else 1
                extractor.selectTrack(i)
                break
            }
        }

        if (trackIndex < 0) {
            extractor.release()
            return null
        }

        val codec: MediaCodec = try {
            MediaCodec.createDecoderByType(mimeType)
        } catch (_: Exception) {
            extractor.release()
            return null
        }

        try {
            codec.configure(extractor.getTrackFormat(trackIndex), null, null, 0)
            codec.start()
        } catch (_: Exception) {
            codec.release()
            extractor.release()
            return null
        }

        val bufferInfo = MediaCodec.BufferInfo()
        val rawSamples = ArrayList<Float>(sampleRate * 30) // pre-allocate ~30 s
        var inputDone = false
        var outputDone = false
        val timeoutUs = 10_000L

        try {
            while (!outputDone) {
                if (!inputDone) {
                    val inIdx = codec.dequeueInputBuffer(timeoutUs)
                    if (inIdx >= 0) {
                        val inBuf = codec.getInputBuffer(inIdx) ?: continue
                        val size = extractor.readSampleData(inBuf, 0)
                        if (size < 0) {
                            codec.queueInputBuffer(
                                inIdx, 0, 0, 0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            inputDone = true
                        } else {
                            codec.queueInputBuffer(inIdx, 0, size, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                when (val outIdx = codec.dequeueOutputBuffer(bufferInfo, timeoutUs)) {
                    MediaCodec.INFO_TRY_AGAIN_LATER -> Unit
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> Unit
                    else -> if (outIdx >= 0) {
                        val outBuf = codec.getOutputBuffer(outIdx)
                        if (outBuf != null) {
                            val shortBuf = outBuf.asShortBuffer()
                            while (shortBuf.hasRemaining()) {
                                val s = shortBuf.get().toFloat() / 32768f
                                rawSamples.add(s)
                                // Discard extra channels — keep channel 0 (mono downmix)
                                repeat(channelCount - 1) {
                                    if (shortBuf.hasRemaining()) shortBuf.get()
                                }
                            }
                        }
                        codec.releaseOutputBuffer(outIdx, false)
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            outputDone = true
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Return whatever we decoded so far
        } finally {
            try { codec.stop() } catch (_: Exception) { }
            try { codec.release() } catch (_: Exception) { }
            try { extractor.release() } catch (_: Exception) { }
        }

        if (rawSamples.isEmpty()) return null
        return rawSamples.toFloatArray() to sampleRate
    }
}
