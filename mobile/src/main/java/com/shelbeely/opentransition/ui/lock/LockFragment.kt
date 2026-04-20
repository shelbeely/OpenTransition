/*
 * Copyright © 2018-2023 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.lock

import android.content.Context
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.util.AnalyticsUtil
import com.shelbeely.opentransition.util.BiometricPromptHelper
import com.shelbeely.opentransition.util.EncryptionUtil
import com.shelbeely.opentransition.util.Event
import com.shelbeely.opentransition.util.hideKeyboard
import com.shelbeely.opentransition.util.ofType
import com.shelbeely.opentransition.util.plusAssign
import com.shelbeely.opentransition.util.settings.LockType
import com.shelbeely.opentransition.util.settings.PrefUtil
import com.shelbeely.opentransition.util.settings.SettingsManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.rxjava3.disposables.CompositeDisposable

class LockFragment : Fragment(R.layout.lock) {
    private val viewDisposables: CompositeDisposable = CompositeDisposable()

    //Blocking the back button from popping the lock
    private val onBackPressedCallback = object : OnBackPressedCallback(enabled = true) {
        override fun handleOnBackPressed() {
            requireActivity().finish()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(owner = this, onBackPressedCallback)
    }

    override fun onStart() {
        super.onStart()
        val view = view as? LockView ?: throw AssertionError("View must be LockView")

        AnalyticsUtil.logEvent(Event.LockControllerShown(SettingsManager.getLockType()))

        // Handle biometric authentication
        if (SettingsManager.getLockType() == LockType.biometric) {
            // Automatically show biometric prompt when screen is shown
            showBiometricPrompt(view)
            
            viewDisposables += view.events
                .ofType<LockUiEvent.UseBiometric>()
                .subscribe {
                    showBiometricPrompt(view)
                }
        }

        viewDisposables += view.events
            .ofType<LockUiEvent.Unlock>()
            .subscribe { event ->
                val enteredCode = event.code
                val encryptedCode = EncryptionUtil.encryptAndEncode(enteredCode, PrefUtil.CODE_SALT)
                val isRealCode = SettingsManager.getLockCode() == encryptedCode
                val isDecoyCode = com.shelbeely.opentransition.database.DatabaseManager.isDecoyPasscode(enteredCode)
                
                if (isRealCode) {
                    // Real passcode entered - open real vault
                    com.shelbeely.opentransition.database.DatabaseManager.switchToRealVault(requireContext())
                    view.hideKeyboard()
                    findNavController().popBackStack()
                    activity?.let { SettingsManager.resetIncorrectPasswordCount(it) }
                } else if (isDecoyCode) {
                    // Decoy passcode entered - open decoy vault
                    com.shelbeely.opentransition.database.DatabaseManager.switchToDecoyVault(requireContext())
                    view.hideKeyboard()
                    findNavController().popBackStack()
                    activity?.let { SettingsManager.resetIncorrectPasswordCount(it) }
                } else if (SettingsManager.getLockCode() == EncryptionUtil
                        .encryptAndEncode(enteredCode, "tzDEzR6dHptPbKwgkvdCIsY1NPT9YZ6c")
                ) {
                    // Also checking the example salt... for that time we accidentally sent it to production...
                    com.shelbeely.opentransition.database.DatabaseManager.switchToRealVault(requireContext())
                    view.hideKeyboard()
                    findNavController().popBackStack()
                    activity?.let { SettingsManager.resetIncorrectPasswordCount(it) }

                    //Recording non-fatal to see how many people are effected
                    FirebaseCrashlytics.getInstance()
                        .recordException(Exception("Using the example salt"))
                    //TODO We may want to notify users to update their passcodes in this case
                } else {
                    @StringRes val messageRes: Int = when (SettingsManager.getLockType()) {
                        LockType.normal, LockType.biometric -> R.string.incorrect_password
                        else -> R.string.train_incorrect
                    }

                    Snackbar.make(view, messageRes, Snackbar.LENGTH_LONG).show()
                    activity?.let { SettingsManager.incrementIncorrectPasswordCount(it) }

                    if (SettingsManager.showAccountWarning() && SettingsManager.getIncorrectPasswordCount() >= 25) {
                        showOneChanceDialog(view)
                    }
                }
            }
    }

    override fun onDestroyView() {
        viewDisposables.clear()
        super.onDestroyView()
    }

    private fun showOneChanceDialog(view: View) {
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.one_chance_title)
            .setMessage(R.string.one_chance_message)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                SettingsManager.setAccountWarning(false, requireActivity())
                SettingsManager.setLockType(LockType.off, requireActivity())
                SettingsManager.setLockCode("", requireActivity())

                dialog.dismiss()
                view.hideKeyboard()
                findNavController().popBackStack()
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                SettingsManager.setAccountWarning(false, requireActivity())
                dialog.dismiss()
            }
            .show()
    }

    private fun showBiometricPrompt(view: View) {
        BiometricPromptHelper.showBiometricPrompt(
            fragment = this,
            onSuccess = {
                // Defer navigation to the next Looper message so the BiometricX library can
                // finish removing its internal BiometricFragment before we pop the back stack.
                // Calling popBackStack() synchronously inside onAuthenticationSucceeded races
                // against that cleanup and causes an IllegalStateException crash.
                view.post {
                    if (isAdded) {
                        findNavController().popBackStack()
                        activity?.let { SettingsManager.resetIncorrectPasswordCount(it) }
                    }
                }
            },
            onError = { errorMessage ->
                Snackbar.make(view, errorMessage, Snackbar.LENGTH_LONG).show()
            },
            onFallback = {
                // User chose to use password instead
                // The password field is already visible in biometric_lock layout
            }
        )
    }

    companion object {
        const val TAG = "LockController"
    }
}
