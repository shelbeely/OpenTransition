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

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.database.migration.RealmToRoomMigration
import com.shelbeely.opentransition.ui.settings.SettingsUiState.Content
import com.shelbeely.opentransition.ui.settings.SettingsUiState.Loading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onChangeName: () -> Unit,
    onChangeEmail: () -> Unit,
    onSignIn: () -> Unit,
    onChangePassword: () -> Unit,
    onDeleteAccount: () -> Unit,
    onSignOut: () -> Unit,
    onChangeStartDate: () -> Unit,
    onChangeTheme: () -> Unit,
    onChangeLockMode: () -> Unit,
    onChangeLockDelay: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onToggleAnalytics: () -> Unit,
    onToggleCrashReports: () -> Unit,
    onToggleEncryptedDatabase: () -> Unit,
    onToggleDecoyVault: () -> Unit,
    onSetDecoyPasscode: () -> Unit,
    onToggleQuickHide: () -> Unit,
    onImportRealmBackup: () -> Unit,
    onContribute: () -> Unit,
    onPrivacyPolicy: () -> Unit,
) {
    val content: Content = when (state) {
        is Content -> state
        is Loading -> state.content
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state is Loading) {
                LinearProgressIndicator(
                    progress = { state.overallProgress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
            ) {
                AccountSection(
                    content = content,
                    onChangeName = onChangeName,
                    onChangeEmail = onChangeEmail,
                    onSignIn = onSignIn,
                    onChangePassword = onChangePassword,
                    onDeleteAccount = onDeleteAccount,
                    onSignOut = onSignOut
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))

                AppSettingsSection(
                    content = content,
                    onChangeStartDate = onChangeStartDate,
                    onChangeTheme = onChangeTheme,
                    onChangeLockMode = onChangeLockMode,
                    onChangeLockDelay = onChangeLockDelay
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))

                DataSection(onImport = onImport, onExport = onExport)

                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))

                SecuritySection(
                    content = content,
                    onToggleEncryptedDatabase = onToggleEncryptedDatabase,
                    onToggleDecoyVault = onToggleDecoyVault,
                    onSetDecoyPasscode = onSetDecoyPasscode,
                    onToggleQuickHide = onToggleQuickHide,
                    onImportRealmBackup = onImportRealmBackup
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))

                PrivacySection(
                    content = content,
                    onToggleAnalytics = onToggleAnalytics,
                    onToggleCrashReports = onToggleCrashReports
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))

                AboutSection(
                    content = content,
                    onContribute = onContribute,
                    onPrivacyPolicy = onPrivacyPolicy
                )
            }
        }
    }
}

@Composable
private fun AccountSection(
    content: Content,
    onChangeName: () -> Unit,
    onChangeEmail: () -> Unit,
    onSignIn: () -> Unit,
    onChangePassword: () -> Unit,
    onDeleteAccount: () -> Unit,
    onSignOut: () -> Unit,
) {
    SectionHeader(text = stringResource(R.string.transtracks_account))

    if (content.userDetails == null) {
        Text(
            text = stringResource(R.string.transtracks_account_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = onSignIn) {
                Text(text = stringResource(R.string.sign_in))
            }
        }
    } else {
        LabelValueRow(
            label = stringResource(R.string.account_name_label),
            value = content.userDetails.name ?: stringResource(R.string.unknown),
            onValueClick = onChangeName
        )
        LabelValueRow(
            label = stringResource(R.string.account_email_label),
            value = content.userDetails.email ?: stringResource(R.string.unknown),
            onValueClick = onChangeEmail
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (content.userDetails.email != null) {
                val passwordButtonRes =
                    if (content.userDetails.hasPasswordProvider) R.string.change_password
                    else R.string.set_password
                Button(onClick = onChangePassword) {
                    Text(text = stringResource(passwordButtonRes))
                }
                Spacer(modifier = Modifier.weight(0.5f))
            }
            OutlinedButton(onClick = onDeleteAccount) {
                Text(text = stringResource(R.string.delete_account))
            }
            Spacer(modifier = Modifier.weight(0.5f))
            OutlinedButton(onClick = onSignOut) {
                Text(text = stringResource(R.string.sign_out))
            }
        }
    }
}

@Composable
private fun AppSettingsSection(
    content: Content,
    onChangeStartDate: () -> Unit,
    onChangeTheme: () -> Unit,
    onChangeLockMode: () -> Unit,
    onChangeLockDelay: () -> Unit,
) {
    LabelValueRow(
        label = stringResource(R.string.start_date_label),
        value = content.startDate.toString(),
        onValueClick = onChangeStartDate
    )
    LabelValueRow(
        label = stringResource(R.string.theme_label),
        value = content.theme,
        onValueClick = onChangeTheme
    )
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.lock_label),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onChangeLockMode) {
                Text(text = content.lockMode, style = MaterialTheme.typography.titleMedium)
            }
        }
        Text(
            text = stringResource(R.string.lock_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    LabelValueRow(
        label = stringResource(R.string.lock_delay_label),
        value = content.lockDelay,
        onValueClick = onChangeLockDelay,
        enabled = content.enableLockDelay
    )
}

@Composable
private fun DataSection(onImport: () -> Unit, onExport: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.data_label),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Button(onClick = onImport) {
            Text(text = stringResource(R.string._import))
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onExport) {
            Text(text = stringResource(R.string.export))
        }
    }
}

@Composable
private fun SecuritySection(
    content: Content,
    onToggleEncryptedDatabase: () -> Unit,
    onToggleDecoyVault: () -> Unit,
    onSetDecoyPasscode: () -> Unit,
    onToggleQuickHide: () -> Unit,
    onImportRealmBackup: () -> Unit,
) {
    SectionHeader(text = stringResource(R.string.security))

    // ITEM-60: Surface Room migration status so users know where they stand.
    val context = LocalContext.current
    val migrationComplete = remember { RealmToRoomMigration.isMigrationComplete(context) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.db_migration_status_label),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (migrationComplete) stringResource(R.string.db_migration_complete)
                   else stringResource(R.string.db_migration_pending),
            style = MaterialTheme.typography.bodyMedium,
            color = if (migrationComplete) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    // The "Encrypted Database" toggle and decoy-vault flow are gated by
    // SettingsManager.isEncryptedDatabaseFeatureAvailable() because the
    // underlying Realm→Room migration is still in progress. Showing the
    // toggle today would advertise an encryption capability that does not
    // protect the bulk of user-visible data (photos, milestones, audio).
    // See audit-report/07-issues-and-bugs.md ISSUE-004 / ISSUE-013.
    if (com.shelbeely.opentransition.util.settings.SettingsManager
            .isEncryptedDatabaseFeatureAvailable()) {
        SwitchRow(
            label = stringResource(R.string.encrypted_database),
            description = stringResource(R.string.encrypted_database_description),
            checked = content.encryptedDatabaseEnabled,
            onCheckedChange = { onToggleEncryptedDatabase() }
        )

        SwitchRow(
            label = stringResource(R.string.decoy_vault),
            description = stringResource(R.string.decoy_vault_description),
            checked = content.decoyVaultEnabled,
            enabled = content.encryptedDatabaseEnabled,
            onCheckedChange = { onToggleDecoyVault() }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onSetDecoyPasscode,
                enabled = content.encryptedDatabaseEnabled && content.decoyVaultEnabled
            ) {
                Text(text = stringResource(R.string.set_decoy_passcode))
            }
        }
    }

    SwitchRow(
        label = stringResource(R.string.quick_hide),
        description = stringResource(R.string.quick_hide_description),
        checked = content.quickHideEnabled,
        onCheckedChange = { onToggleQuickHide() }
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(onClick = onImportRealmBackup) {
            Text(text = stringResource(R.string.import_backup))
        }
    }
}

@Composable
private fun PrivacySection(
    content: Content,
    onToggleAnalytics: () -> Unit,
    onToggleCrashReports: () -> Unit,
) {
    SwitchRow(
        label = stringResource(R.string.anonymous_analytics),
        checked = content.enableAnalytics,
        onCheckedChange = { onToggleAnalytics() }
    )
    SwitchRow(
        label = stringResource(R.string.anonymous_crash_reports),
        checked = content.enableCrashReports,
        onCheckedChange = { onToggleCrashReports() }
    )
}

@Composable
private fun AboutSection(
    content: Content,
    onContribute: () -> Unit,
    onPrivacyPolicy: () -> Unit,
) {
    SectionHeader(text = stringResource(R.string.about_transtracks_title))

    Text(
        text = stringResource(R.string.about_transtracks_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )

    TextButton(
        onClick = onContribute,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.contribute),
            style = MaterialTheme.typography.titleMedium
        )
    }

    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.app_version_label),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = content.appVersion,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    TextButton(
        onClick = onPrivacyPolicy,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.privacy_policy),
            style = MaterialTheme.typography.titleMedium
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    CopyrightText(
        copyright = content.copyright,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun CopyrightText(copyright: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val linkStyle = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline
    )

    val annotated = buildAnnotatedString {
        val transTracksTag = "URL_TRANSTRACKS"
        val shelBeelyTag = "URL_SHELBEELY"
        val openTransitionTag = "URL_OPENTRANSITION"

        val transTracksKey = "TransTracks"
        val shelBeelyKey = "Shelbeely"
        val openTransitionKey = "OpenTransition contributors"

        val ttIdx = copyright.indexOf(transTracksKey)
        val sbIdx = copyright.indexOf(shelBeelyKey)
        val otIdx = copyright.indexOf(openTransitionKey)

        var cursor = 0
        data class Link(val start: Int, val end: Int, val tag: String, val url: String)

        val links = buildList {
            if (ttIdx >= 0) add(Link(ttIdx, ttIdx + transTracksKey.length, transTracksTag, "https://github.com/TransTracks/TransTracks"))
            if (sbIdx >= 0) add(Link(sbIdx, sbIdx + shelBeelyKey.length, shelBeelyTag, "https://github.com/shelbeely"))
            if (otIdx >= 0) add(Link(otIdx, otIdx + openTransitionKey.length, openTransitionTag, "https://github.com/shelbeely/OpenTransition/graphs/contributors"))
        }.sortedBy { it.start }

        for (link in links) {
            if (cursor < link.start) append(copyright.substring(cursor, link.start))
            pushStringAnnotation(tag = link.tag, annotation = link.url)
            withStyle(linkStyle) { append(copyright.substring(link.start, link.end)) }
            pop()
            cursor = link.end
        }
        if (cursor < copyright.length) append(copyright.substring(cursor))
    }

    androidx.compose.foundation.text.ClickableText(
        text = annotated,
        style = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        ),
        modifier = modifier,
        onClick = { offset ->
            listOf("URL_TRANSTRACKS", "URL_SHELBEELY", "URL_OPENTRANSITION").forEach { tag ->
                annotated.getStringAnnotations(tag, offset, offset).firstOrNull()?.let { ann ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ann.item))
                    context.startActivity(intent)
                }
            }
        }
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

@Composable
private fun LabelValueRow(
    label: String,
    value: String,
    onValueClick: () -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onValueClick, enabled = enabled) {
            Text(text = value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String? = null,
    enabled: Boolean = true,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
