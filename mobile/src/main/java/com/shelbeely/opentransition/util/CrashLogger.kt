/*
 * Copyright © 2026 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.util

import android.content.Context
import android.os.Build
import android.util.Log
import com.shelbeely.opentransition.BuildConfig
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Writes uncaught exceptions to a plain-text file under the app's external
 * files directory so they can be retrieved off the device without root.
 *
 * Files land in:
 *   /sdcard/Android/data/<applicationId>/files/crash_logs/
 *
 * On a connected device they can be pulled with:
 *   adb pull /sdcard/Android/data/com.shelbeely.opentransition/files/crash_logs/
 *
 * The handler chains to whatever uncaught-exception handler was previously
 * installed (typically Firebase Crashlytics' handler followed by the system
 * default), so existing reporting and the system "App keeps stopping" dialog
 * still fire.
 */
object CrashLogger {
    private const val TAG = "CrashLogger"
    private const val DIR_NAME = "crash_logs"

    /** Cap on how many crash log files we keep, oldest deleted first. */
    private const val MAX_LOG_FILES = 20

    @Volatile
    private var initialized = false

    @Volatile
    private var appContext: Context? = null

    /**
     * Install the uncaught-exception handler. Safe to call multiple times;
     * subsequent calls are no-ops. Should be invoked as early as possible in
     * [android.app.Application.onCreate].
     */
    fun install(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            appContext = context.applicationContext
            val previous = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                try {
                    writeCrashLog(thread, throwable)
                } catch (loggingError: Throwable) {
                    Log.e(TAG, "Failed to write crash log", loggingError)
                }
                // Always delegate so Crashlytics + system crash dialog still run.
                previous?.uncaughtException(thread, throwable)
            }
            initialized = true
        }
    }

    /**
     * Record a non-fatal throwable (for example one caught by a coroutine
     * exception handler) to the same on-device log directory. Does nothing
     * if [install] has not been called yet.
     */
    fun logNonFatal(throwable: Throwable, contextLabel: String? = null) {
        try {
            writeCrashLog(Thread.currentThread(), throwable, contextLabel = contextLabel ?: "non-fatal")
        } catch (loggingError: Throwable) {
            Log.e(TAG, "Failed to write non-fatal crash log", loggingError)
        }
    }

    /** Returns the directory crash logs are written to, or null if unavailable. */
    fun getLogDirectory(): File? {
        val ctx = appContext ?: return null
        // Prefer external files dir (user-accessible without root); fall back to internal.
        val base = ctx.getExternalFilesDir(null) ?: ctx.filesDir ?: return null
        val dir = File(base, DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun writeCrashLog(
        thread: Thread,
        throwable: Throwable,
        contextLabel: String = "uncaught"
    ) {
        val dir = getLogDirectory() ?: return

        val now = Date()
        val fileTimestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(now)
        val humanTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.US).format(now)
        val file = File(dir, "crash_${fileTimestamp}.txt")

        val stackTrace = StringWriter().also { sw ->
            PrintWriter(sw).use { throwable.printStackTrace(it) }
        }.toString()

        val report = buildString {
            append("=== OpenTransition crash report ===\n")
            append("When:           ").append(humanTimestamp).append('\n')
            append("Type:           ").append(contextLabel).append('\n')
            append("Thread:         ").append(thread.name).append(" (id=").append(thread.id).append(")\n")
            append("App version:    ").append(BuildConfig.VERSION_NAME)
                .append(" (").append(BuildConfig.VERSION_CODE).append(")\n")
            append("Build type:     ").append(BuildConfig.BUILD_TYPE).append('\n')
            append("Application id: ").append(BuildConfig.APPLICATION_ID).append('\n')
            append("Android:        ").append(Build.VERSION.RELEASE)
                .append(" (SDK ").append(Build.VERSION.SDK_INT).append(")\n")
            append("Device:         ").append(Build.MANUFACTURER).append(' ').append(Build.MODEL)
                .append(" [").append(Build.DEVICE).append("]\n")
            append("ABI:            ").append(Build.SUPPORTED_ABIS.joinToString()).append('\n')
            append('\n')
            append("--- Stack trace ---\n")
            append(stackTrace)
        }

        file.writeText(report)
        Log.e(TAG, "Crash report written to ${file.absolutePath}")

        pruneOldLogs(dir)
    }

    private fun pruneOldLogs(dir: File) {
        val files = dir.listFiles { f -> f.isFile && f.name.startsWith("crash_") && f.name.endsWith(".txt") }
            ?: return
        if (files.size <= MAX_LOG_FILES) return
        files.sortedByDescending { it.lastModified() }
            .drop(MAX_LOG_FILES)
            .forEach { runCatching { it.delete() } }
    }
}
