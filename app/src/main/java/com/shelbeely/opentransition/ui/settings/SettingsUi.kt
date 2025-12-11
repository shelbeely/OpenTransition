/*
 * Copyright © 2018-2023 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.databinding.SettingsBinding
import com.shelbeely.opentransition.ui.settings.SettingsUiState.Content
import com.shelbeely.opentransition.ui.settings.SettingsUiState.Loading
import com.shelbeely.opentransition.util.getString
import com.shelbeely.opentransition.util.gone
import com.shelbeely.opentransition.util.toFullDateString
import com.shelbeely.opentransition.util.toV3
import com.shelbeely.opentransition.util.visible
import com.jakewharton.rxbinding3.appcompat.navigationClicks
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.checkedChanges
import io.reactivex.rxjava3.core.Observable
import java.time.LocalDate

sealed class SettingsUiEvent {
    object Back : SettingsUiEvent()
    object ChangeName : SettingsUiEvent()
    object ChangeEmail : SettingsUiEvent()
    object SignIn : SettingsUiEvent()
    object ChangePassword : SettingsUiEvent()
    object DeleteAccount : SettingsUiEvent()
    object SignOut : SettingsUiEvent()
    object ChangeStartDate : SettingsUiEvent()
    object ChangeTheme : SettingsUiEvent()
    object ChangeLockMode : SettingsUiEvent()
    object ChangeLockDelay : SettingsUiEvent()
    object Import : SettingsUiEvent()
    object Export : SettingsUiEvent()
    object ToggleAnalytics : SettingsUiEvent()
    object ToggleCrashReports : SettingsUiEvent()
    object Contribute : SettingsUiEvent()
    object PrivacyPolicy : SettingsUiEvent()
}

data class SettingsUIUserDetails(
    val name: String?, val email: String?, val hasPasswordProvider: Boolean
)

sealed class SettingsUiState {
    data class Content(
        val userDetails: SettingsUIUserDetails?, val startDate: LocalDate, val theme: String,
        val lockMode: String, val enableLockDelay: Boolean, val lockDelay: String,
        val appVersion: String, val copyright: String,
        val enableAnalytics: Boolean, val enableCrashReports: Boolean
    ) : SettingsUiState()

    data class Loading(val content: Content, val overallProgress: Int, val stepProgress: Int) :
        SettingsUiState()
}

class SettingsView(context: Context, attributeSet: AttributeSet) :
    ConstraintLayout(context, attributeSet) {
    private lateinit var binding: SettingsBinding

    val events: Observable<SettingsUiEvent> by lazy(LazyThreadSafetyMode.NONE) {
        Observable.mergeArray(
            binding.settingsToolbar.navigationClicks().toV3().map { SettingsUiEvent.Back },
            binding.settingsAccountName.clicks().toV3().map { SettingsUiEvent.ChangeName },
            binding.settingsAccountEmail.clicks().toV3().map { SettingsUiEvent.ChangeEmail },
            binding.settingsAccountSignIn.clicks().toV3().map { SettingsUiEvent.SignIn },
            binding.settingsAccountChangePassword.clicks().toV3()
                .map { SettingsUiEvent.ChangePassword },
            binding.settingsAccountDeleteAccount.clicks().toV3()
                .map { SettingsUiEvent.DeleteAccount },
            binding.settingsAccountSignOut.clicks().toV3().map { SettingsUiEvent.SignOut },
            binding.settingsStartDate.clicks().toV3().map { SettingsUiEvent.ChangeStartDate },
            binding.settingsTheme.clicks().toV3().map { SettingsUiEvent.ChangeTheme },
            binding.settingsLock.clicks().toV3().map { SettingsUiEvent.ChangeLockMode },
            binding.settingsLockDelay.clicks().toV3().map { SettingsUiEvent.ChangeLockDelay },
            binding.settingsImport.clicks().toV3().map { SettingsUiEvent.Import },
            binding.settingsExport.clicks().toV3().map { SettingsUiEvent.Export },
            binding.settingsAnalytics.checkedChanges().toV3()
                .filter { userAction }.map { SettingsUiEvent.ToggleAnalytics },
            binding.settingsCrashReports.checkedChanges().toV3()
                .filter { userAction }.map { SettingsUiEvent.ToggleCrashReports },
            binding.settingsContribute.clicks().toV3().map { SettingsUiEvent.Contribute },
            binding.settingsPrivacyPolicy.clicks().toV3().map { SettingsUiEvent.PrivacyPolicy }
        )
    }

    private var currentStartDate: LocalDate? = null

    private var userAction = false

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding = SettingsBinding.bind(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        binding.settingsAccountNameLabel.setOnClickListener { binding.settingsAccountName.performClick() }
        binding.settingsAccountEmailLabel.setOnClickListener { binding.settingsAccountEmail.performClick() }
        binding.settingsStartLabel.setOnClickListener { binding.settingsStartDate.performClick() }
        binding.settingsThemeLabel.setOnClickListener { binding.settingsTheme.performClick() }
        binding.settingsLockLabel.setOnClickListener { binding.settingsLock.performClick() }
        binding.settingsLockDescription.setOnClickListener { binding.settingsLock.performClick() }
        binding.settingsLockDelayLabel.setOnClickListener { binding.settingsLockDelay.performClick() }
    }

    fun display(state: SettingsUiState) {
        userAction = false
        when (state) {
            is Content -> {
                binding.settingsLoadingLayout.gone()
                displayContent(state)
            }

            is Loading -> {
                binding.settingsLoadingLayout.visible()
                binding.settingsLoadingProgress.progress = state.overallProgress
                binding.settingsLoadingProgress.secondaryProgress = state.stepProgress
            }
        }
        userAction = true
    }

    private fun displayContent(content: Content) {
        if (content.userDetails != null) {
            displayUserDetails(content.userDetails)
        } else {
            binding.settingsAccountDescription.visible()
            binding.settingsAccountNameLayout.gone()
            binding.settingsAccountEmailLayout.gone()
            binding.settingsAccountSignIn.visible()
            binding.settingsAccountChangePassword.gone()
            binding.settingsAccountLoggedInButtonSpace1.gone()
            binding.settingsAccountDeleteAccount.gone()
            binding.settingsAccountLoggedInButtonSpace2.gone()
            binding.settingsAccountSignOut.gone()
        }

        currentStartDate = content.startDate

        binding.settingsStartDate.text = content.startDate.toFullDateString(context)
        binding.settingsTheme.text = content.theme
        binding.settingsLock.text = content.lockMode

        binding.settingsLockDelayLabel.isEnabled = content.enableLockDelay
        binding.settingsLockDelay.isEnabled = content.enableLockDelay

        binding.settingsLockDelay.text = content.lockDelay

        binding.settingsAnalytics.isChecked = content.enableAnalytics
        binding.settingsCrashReports.isChecked = content.enableCrashReports

        binding.settingsAppVersion.text = content.appVersion

        // Make copyright text clickable with links
        val copyrightText = content.copyright
        val spannableString = SpannableString(copyrightText)
        
        // Find and make "TransTracks" clickable
        val transTracksStart = copyrightText.indexOf("TransTracks")
        if (transTracksStart >= 0) {
            val transTracksEnd = transTracksStart + "TransTracks".length
            spannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/TransTracks/TransTracks"))
                        context.startActivity(intent)
                    }
                },
                transTracksStart,
                transTracksEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        
        // Find and make "Shelbeely" clickable
        val shelBeelyStart = copyrightText.indexOf("Shelbeely")
        if (shelBeelyStart >= 0) {
            val shelBeelyEnd = shelBeelyStart + "Shelbeely".length
            spannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/shelbeely"))
                        context.startActivity(intent)
                    }
                },
                shelBeelyStart,
                shelBeelyEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        
        // Find and make "OpenTransition contributors" clickable
        val openTransitionStart = copyrightText.indexOf("OpenTransition contributors")
        if (openTransitionStart >= 0) {
            val openTransitionEnd = openTransitionStart + "OpenTransition contributors".length
            spannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/shelbeely/OpenTransition/graphs/contributors"))
                        context.startActivity(intent)
                    }
                },
                openTransitionStart,
                openTransitionEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        
        binding.settingsCopyright.text = spannableString
        binding.settingsCopyright.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun displayUserDetails(user: SettingsUIUserDetails) {
        binding.settingsAccountDescription.gone()
        binding.settingsAccountNameLayout.visible()
        binding.settingsAccountEmailLayout.visible()
        binding.settingsAccountSignIn.gone()
        binding.settingsAccountLoggedInButtonSpace1.visible()
        binding.settingsAccountDeleteAccount.visible()
        binding.settingsAccountLoggedInButtonSpace2.visible()
        binding.settingsAccountSignOut.visible()

        if (user.email != null) {
            binding.settingsAccountChangePassword.visible()

            val buttonRes =
                if (user.hasPasswordProvider) R.string.change_password else R.string.set_password
            binding.settingsAccountChangePassword.setText(buttonRes)
        } else {
            binding.settingsAccountChangePassword.gone()
        }

        binding.settingsAccountName.text = user.name ?: getString(R.string.unknown)
        binding.settingsAccountEmail.text = user.email ?: getString(R.string.unknown)
    }
}
