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

import android.provider.MediaStore
import com.shelbeely.opentransition.TransTracksApp
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import java.util.UUID

object FileUtil {
    private const val TEMP_FOLDER = "temp/"

    fun clearTempFolder() {
        getTempFolder().deleteRecursively()
    }

    fun getImageFile(fileName: String): File = File(getPhotosDirectory(), fileName)

    fun getAudioFile(fileName: String): File = File(getAudioDirectory(), fileName)

    fun getMediaFile(fileName: String): File {
        // Try to find file in photos directory first
        val photoFile = File(getPhotosDirectory(), fileName)
        if (photoFile.exists()) {
            return photoFile
        }
        // Then try audio directory
        val audioFile = File(getAudioDirectory(), fileName)
        if (audioFile.exists()) {
            return audioFile
        }
        // Default to photos directory for backward compatibility
        return photoFile
    }

    fun getNewImageFile(photoDate: LocalDate): File {
        val photoDateString = photoDate.toFileDateFormat()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

        val file = getImageFile("photo_${photoDateString}_imported_$timeStamp.jpg")
        file.createNewFile()
        return file
    }

    fun getNewAudioFile(photoDate: LocalDate): File {
        val photoDateString = photoDate.toFileDateFormat()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

        val file = getAudioFile("audio_${photoDateString}_recorded_$timeStamp.m4a")
        file.createNewFile()
        return file
    }

    fun getPhotosDirectory(): File {
        val photosDir = File(TransTracksApp.instance.filesDir, "photos/")
        photosDir.mkdirs()
        return photosDir
    }

    fun getAudioDirectory(): File {
        val audioDir = File(TransTracksApp.instance.filesDir, "audio/")
        audioDir.mkdirs()
        return audioDir
    }

    private fun getTempFolder(): File {
        val tempFolder = File(TransTracksApp.instance.filesDir, TEMP_FOLDER)
        tempFolder.mkdirs()
        return tempFolder
    }

    fun getTempFile(fileName: String): File {
        val file = File(getTempFolder(), fileName)
        file.createNewFile()
        return file
    }

    fun getTempImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(Date())
        val uuid = UUID.randomUUID().toString()
        return getTempFile("${timeStamp}_${uuid}.jpg")
    }

    fun getTempAudioFile(): File {
        val timeStamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(Date())
        val uuid = UUID.randomUUID().toString()
        return getTempFile("${timeStamp}_${uuid}.m4a")
    }

    fun removeImageFromGallery(filePath: String) {
        TransTracksApp.instance.contentResolver.delete(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "${MediaStore.Files.FileColumns.DATA} = ?",
            arrayOf(filePath)
        )
    }
}
