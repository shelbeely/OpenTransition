/*
 * Copyright © 2018-2023 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.shelbeely.opentransition.R

object BiometricPromptHelper {
    
    /**
     * Check if biometric authentication is available on the device
     */
    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
    
    /**
     * Check if biometric credentials are enrolled
     */
    fun isBiometricEnrolled(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == 
                BiometricManager.BIOMETRIC_SUCCESS
    }
    
    /**
     * Get the biometric availability status message
     */
    fun getBiometricStatusMessage(context: Context): String? {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> null
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> 
                context.getString(R.string.biometric_not_available)
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> 
                context.getString(R.string.biometric_not_available)
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> 
                context.getString(R.string.biometric_not_enrolled)
            else -> context.getString(R.string.biometric_not_available)
        }
    }
    
    /**
     * Show biometric prompt for authentication
     */
    fun showBiometricPrompt(
        fragment: Fragment,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFallback: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(fragment.requireContext())
        
        val biometricPrompt = BiometricPrompt(fragment, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        // User clicked "Use Password" button
                        onFallback()
                    } else {
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // This is called when biometric is valid but not recognized
                    // Don't show error here, let user try again
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(fragment.getString(R.string.biometric_prompt_title))
            .setSubtitle(fragment.getString(R.string.biometric_prompt_subtitle))
            .setNegativeButtonText(fragment.getString(R.string.biometric_prompt_negative_button))
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
