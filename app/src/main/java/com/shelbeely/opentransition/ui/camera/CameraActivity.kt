/*
 * Copyright © 2018-2025 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.camera

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.util.FileUtil
import com.squareup.picasso.Picasso
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var btnCapture: ImageButton
    private lateinit var btnSwitchCamera: ImageButton
    private lateinit var btnFlash: ImageButton
    private lateinit var btnGrid: ImageButton
    private lateinit var btnTimer: ImageButton
    private lateinit var btnOverlay: ImageButton
    private lateinit var btnClose: ImageButton
    private lateinit var gridOverlay: View
    private lateinit var overlayImage: ImageView
    private lateinit var overlayOpacitySeekBar: SeekBar
    private lateinit var timerCountdown: TextView

    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService

    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private var gridEnabled = false
    private var overlayEnabled = false
    private var timerSeconds = 0
    private var overlayPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        overlayPhotoUri = intent.getParcelableExtra(EXTRA_OVERLAY_PHOTO_URI)

        initViews()
        loadPreferences()
        setupListeners()
        setupOverlay()

        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()
    }

    private fun initViews() {
        previewView = findViewById(R.id.previewView)
        btnCapture = findViewById(R.id.btnCapture)
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera)
        btnFlash = findViewById(R.id.btnFlash)
        btnGrid = findViewById(R.id.btnGrid)
        btnTimer = findViewById(R.id.btnTimer)
        btnOverlay = findViewById(R.id.btnOverlay)
        btnClose = findViewById(R.id.btnClose)
        gridOverlay = findViewById(R.id.gridOverlay)
        overlayImage = findViewById(R.id.overlayImage)
        overlayOpacitySeekBar = findViewById(R.id.overlayOpacitySeekBar)
        timerCountdown = findViewById(R.id.timerCountdown)
    }

    private fun loadPreferences() {
        val prefs = getSharedPreferences("camera_prefs", Context.MODE_PRIVATE)
        gridEnabled = prefs.getBoolean("grid_enabled", false)
        timerSeconds = prefs.getInt("timer_seconds", 0)
        val overlayOpacity = prefs.getInt("overlay_opacity", 50)
        
        updateGridVisibility()
        updateTimerIcon()
        overlayOpacitySeekBar.progress = overlayOpacity
        overlayImage.alpha = overlayOpacity / 100f
    }

    private fun setupListeners() {
        btnCapture.setOnClickListener { capturePhoto() }
        btnSwitchCamera.setOnClickListener { switchCamera() }
        btnFlash.setOnClickListener { cycleFlash() }
        btnGrid.setOnClickListener { toggleGrid() }
        btnTimer.setOnClickListener { cycleTimer() }
        btnOverlay.setOnClickListener { toggleOverlay() }
        btnClose.setOnClickListener { finish() }

        overlayOpacitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                overlayImage.alpha = progress / 100f
                savePreference("overlay_opacity", progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupOverlay() {
        if (overlayPhotoUri != null) {
            btnOverlay.visibility = View.VISIBLE
            try {
                Picasso.get()
                    .load(overlayPhotoUri)
                    .into(overlayImage)
            } catch (e: Exception) {
                e.printStackTrace()
                btnOverlay.visibility = View.GONE
            }
        } else {
            btnOverlay.visibility = View.GONE
            overlayImage.visibility = View.GONE
            overlayOpacitySeekBar.visibility = View.GONE
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: return

        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        imageCapture = ImageCapture.Builder()
            .setFlashMode(flashMode)
            .build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture
            )
        } catch (exc: Exception) {
            exc.printStackTrace()
        }
    }

    private fun capturePhoto() {
        if (timerSeconds > 0) {
            startCountdown()
        } else {
            takePicture()
        }
    }

    private fun startCountdown() {
        btnCapture.isEnabled = false
        timerCountdown.visibility = View.VISIBLE

        object : CountDownTimer(timerSeconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt()
                timerCountdown.text = secondsLeft.toString()
            }

            override fun onFinish() {
                timerCountdown.visibility = View.GONE
                btnCapture.isEnabled = true
                takePicture()
            }
        }.start()
    }

    private fun takePicture() {
        val imageCapture = imageCapture ?: return

        val photoFile = FileUtil.getTempImageFile()

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    exc.printStackTrace()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val intent = Intent().apply {
                        putExtra(EXTRA_PHOTO_PATH, photoFile.absolutePath)
                    }
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        )
    }

    private fun switchCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        bindCameraUseCases()
    }

    private fun cycleFlash() {
        flashMode = when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
            else -> ImageCapture.FLASH_MODE_OFF
        }
        imageCapture?.flashMode = flashMode
        updateFlashIcon()
    }

    private fun toggleGrid() {
        gridEnabled = !gridEnabled
        updateGridVisibility()
        savePreference("grid_enabled", gridEnabled)
    }

    private fun toggleOverlay() {
        overlayEnabled = !overlayEnabled
        updateOverlayVisibility()
    }

    private fun cycleTimer() {
        timerSeconds = when (timerSeconds) {
            0 -> 3
            3 -> 5
            5 -> 10
            else -> 0
        }
        updateTimerIcon()
        savePreference("timer_seconds", timerSeconds)
    }

    private fun updateFlashIcon() {
        val iconRes = when (flashMode) {
            ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_on
            ImageCapture.FLASH_MODE_AUTO -> R.drawable.ic_flash_auto
            else -> R.drawable.ic_flash_off
        }
        btnFlash.setImageResource(iconRes)
    }

    private fun updateGridVisibility() {
        gridOverlay.visibility = if (gridEnabled) View.VISIBLE else View.GONE
        btnGrid.setImageResource(
            if (gridEnabled) R.drawable.ic_grid_on else R.drawable.ic_grid_off
        )
    }

    private fun updateOverlayVisibility() {
        if (overlayEnabled && overlayPhotoUri != null) {
            overlayImage.visibility = View.VISIBLE
            overlayOpacitySeekBar.visibility = View.VISIBLE
        } else {
            overlayImage.visibility = View.GONE
            overlayOpacitySeekBar.visibility = View.GONE
        }
        btnOverlay.setImageResource(
            if (overlayEnabled) R.drawable.ic_overlay_on else R.drawable.ic_overlay_off
        )
    }

    private fun updateTimerIcon() {
        val iconRes = when (timerSeconds) {
            3 -> R.drawable.ic_timer_3
            5, 10 -> R.drawable.ic_timer_3  // We'll use same icon for now
            else -> R.drawable.ic_timer_off
        }
        btnTimer.setImageResource(iconRes)
    }

    private fun savePreference(key: String, value: Boolean) {
        getSharedPreferences("camera_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    private fun savePreference(key: String, value: Int) {
        getSharedPreferences("camera_prefs", Context.MODE_PRIVATE)
            .edit()
            .putInt(key, value)
            .apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        const val EXTRA_PHOTO_PATH = "photo_path"
        const val EXTRA_OVERLAY_PHOTO_URI = "overlay_photo_uri"
        const val REQUEST_CODE = 1001

        fun start(context: Context, overlayPhotoUri: Uri? = null) {
            val intent = Intent(context, CameraActivity::class.java).apply {
                overlayPhotoUri?.let { putExtra(EXTRA_OVERLAY_PHOTO_URI, it) }
            }
            if (context is AppCompatActivity) {
                context.startActivityForResult(intent, REQUEST_CODE)
            }
        }
    }
}
