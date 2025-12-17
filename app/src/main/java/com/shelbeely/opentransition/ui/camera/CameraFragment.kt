/*
 * Copyright © 2025 OpenTransition Contributors. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.background.CameraXHandler
import com.shelbeely.opentransition.background.FaceDetectionResult
import com.shelbeely.opentransition.databinding.FragmentCameraBinding
import com.shelbeely.opentransition.util.AnalyticsUtil
import com.shelbeely.opentransition.util.Event
import java.io.File

/**
 * Camera fragment with face detection and alignment guides.
 * 
 * Features:
 * - Real-time face detection and pose tracking
 * - Visual alignment guides (face box, head tilt, chin guidance)
 * - Dysphoria-safe UX with optional overlay toggle
 * - Spring-based Material 3 Expressive animations
 */
class CameraFragment : Fragment(R.layout.fragment_camera) {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val args: CameraFragmentArgs by navArgs()

    private var cameraXHandler: CameraXHandler? = null
    private var overlaysEnabled = true

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            showPermissionDeniedMessage()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCameraBinding.bind(view)

        AnalyticsUtil.logEvent(Event.CameraFragmentShown)

        setupUI()
        checkCameraPermission()
    }

    private fun setupUI() {
        // Back button with spring animation
        binding.btnBack.setOnClickListener {
            animateButtonPress(it) {
                findNavController().navigateUp()
            }
        }

        // Capture button with spring animation
        binding.btnCapture.setOnClickListener {
            animateButtonPress(it) {
                takePhoto()
            }
        }

        // Toggle overlay button
        binding.btnToggleOverlay.setOnClickListener {
            animateButtonPress(it) {
                toggleOverlay()
            }
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        cameraXHandler = CameraXHandler(
            context = requireContext(),
            lifecycleOwner = viewLifecycleOwner,
            previewView = binding.previewView,
            onFaceDetection = { result ->
                handleFaceDetection(result)
            }
        )

        cameraXHandler?.startCamera(
            onSuccess = {
                // Camera started successfully
            },
            onError = { exception ->
                showError("Camera error: ${exception.message}")
            }
        )
    }

    private fun handleFaceDetection(result: FaceDetectionResult) {
        binding.overlayView.updateFaceDetection(result)
    }

    private fun takePhoto() {
        cameraXHandler?.takePhoto(
            onSuccess = { file ->
                navigateToAssignPhoto(file)
            },
            onError = { exception ->
                showError("Photo capture failed: ${exception.message}")
            }
        )
    }

    private fun navigateToAssignPhoto(file: File) {
        findNavController().navigate(
            CameraFragmentDirections.actionGlobalAssignPhotos(
                uris = arrayOf(Uri.fromFile(file)),
                type = args.type,
                destinationToPopTo = args.destinationToPopTo,
                epochDay = args.epochDay
            )
        )
    }

    private fun toggleOverlay() {
        overlaysEnabled = !overlaysEnabled
        binding.overlayView.setOverlaysEnabled(overlaysEnabled)
        
        val message = if (overlaysEnabled) {
            "Guides enabled"
        } else {
            "Guides disabled"
        }
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showPermissionDeniedMessage() {
        Snackbar.make(
            binding.root,
            R.string.camera_permission_required_message,
            Snackbar.LENGTH_LONG
        ).setAction(R.string.settings) {
            // Navigate to app settings
            com.shelbeely.opentransition.util.Utils.goToDeviceSettings(requireActivity())
        }.show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    /**
     * Material 3 Expressive: Bouncy button press animation with spring physics
     */
    private fun animateButtonPress(view: View, onClick: () -> Unit) {
        // Scale down
        view.scaleX = 0.9f
        view.scaleY = 0.9f

        // Spring back with bounce
        SpringAnimation(view, DynamicAnimation.SCALE_X, 1f).apply {
            spring.stiffness = SpringForce.STIFFNESS_LOW
            spring.dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
        }.start()

        SpringAnimation(view, DynamicAnimation.SCALE_Y, 1f).apply {
            spring.stiffness = SpringForce.STIFFNESS_LOW
            spring.dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
        }.start()

        // Execute click action after brief delay
        view.postDelayed({ onClick() }, 100)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraXHandler?.shutdown()
        _binding = null
    }
}
