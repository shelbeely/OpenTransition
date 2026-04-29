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
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme
import com.shelbeely.opentransition.util.applySystemBarInsets
import com.shelbeely.opentransition.util.settings.SettingsManager
import com.jakewharton.rxrelay3.PublishRelay
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
    object ToggleEncryptedDatabase : SettingsUiEvent()
    object ToggleDecoyVault : SettingsUiEvent()
    object SetDecoyPasscode : SettingsUiEvent()
    object ToggleQuickHide : SettingsUiEvent()
    object ImportRealmBackup : SettingsUiEvent()
    object Contribute : SettingsUiEvent()
    object PrivacyPolicy : SettingsUiEvent()
    object Credits : SettingsUiEvent()
    object OssLicenses : SettingsUiEvent()
}

data class SettingsUIUserDetails(
    val name: String?, val email: String?, val hasPasswordProvider: Boolean
)

sealed class SettingsUiState {
    data class Content(
        val userDetails: SettingsUIUserDetails?, val startDate: LocalDate, val theme: String,
        val lockMode: String, val enableLockDelay: Boolean, val lockDelay: String,
        val appVersion: String, val copyright: String,
        val enableAnalytics: Boolean, val enableCrashReports: Boolean,
        val encryptedDatabaseEnabled: Boolean, val decoyVaultEnabled: Boolean, val quickHideEnabled: Boolean
    ) : SettingsUiState()

    data class Loading(val content: Content, val overallProgress: Int, val stepProgress: Int) :
        SettingsUiState()
}

class SettingsView(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet) {

    private val eventRelay: PublishRelay<SettingsUiEvent> = PublishRelay.create()
    val events: Observable<SettingsUiEvent> = eventRelay

    private var currentState: SettingsUiState? = null

    private val composeView: ComposeView by lazy {
        ComposeView(context).also { cv ->
            cv.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            addView(cv, LayoutParams(MATCH_PARENT, MATCH_PARENT))
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        applySystemBarInsets(left = false, top = true, right = false, bottom = true)
        renderCompose()
    }

    fun display(state: SettingsUiState) {
        currentState = state
        renderCompose()
    }

    private fun renderCompose() {
        val state = currentState ?: return
        composeView.setContent {
            OpenTransitionTheme(colorVariant = SettingsManager.getResolvedComposeColorVariant()) {
                SettingsScreen(
                    state = state,
                    onBack = { eventRelay.accept(SettingsUiEvent.Back) },
                    onChangeName = { eventRelay.accept(SettingsUiEvent.ChangeName) },
                    onChangeEmail = { eventRelay.accept(SettingsUiEvent.ChangeEmail) },
                    onSignIn = { eventRelay.accept(SettingsUiEvent.SignIn) },
                    onChangePassword = { eventRelay.accept(SettingsUiEvent.ChangePassword) },
                    onDeleteAccount = { eventRelay.accept(SettingsUiEvent.DeleteAccount) },
                    onSignOut = { eventRelay.accept(SettingsUiEvent.SignOut) },
                    onChangeStartDate = { eventRelay.accept(SettingsUiEvent.ChangeStartDate) },
                    onChangeTheme = { eventRelay.accept(SettingsUiEvent.ChangeTheme) },
                    onChangeLockMode = { eventRelay.accept(SettingsUiEvent.ChangeLockMode) },
                    onChangeLockDelay = { eventRelay.accept(SettingsUiEvent.ChangeLockDelay) },
                    onImport = { eventRelay.accept(SettingsUiEvent.Import) },
                    onExport = { eventRelay.accept(SettingsUiEvent.Export) },
                    onToggleAnalytics = { eventRelay.accept(SettingsUiEvent.ToggleAnalytics) },
                    onToggleCrashReports = { eventRelay.accept(SettingsUiEvent.ToggleCrashReports) },
                    onToggleEncryptedDatabase = { eventRelay.accept(SettingsUiEvent.ToggleEncryptedDatabase) },
                    onToggleDecoyVault = { eventRelay.accept(SettingsUiEvent.ToggleDecoyVault) },
                    onSetDecoyPasscode = { eventRelay.accept(SettingsUiEvent.SetDecoyPasscode) },
                    onToggleQuickHide = { eventRelay.accept(SettingsUiEvent.ToggleQuickHide) },
                    onImportRealmBackup = { eventRelay.accept(SettingsUiEvent.ImportRealmBackup) },
                    onContribute = { eventRelay.accept(SettingsUiEvent.Contribute) },
                    onPrivacyPolicy = { eventRelay.accept(SettingsUiEvent.PrivacyPolicy) },
                    onCredits = { eventRelay.accept(SettingsUiEvent.Credits) },
                    onOssLicenses = { eventRelay.accept(SettingsUiEvent.OssLicenses) }
                )
            }
        }
    }
}
