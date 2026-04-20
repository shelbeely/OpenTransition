/*
 * Copyright © 2020 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.settings

import android.content.Context
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.ui.settings.SettingsConflictDialog.SettingsConflictAdapter.SettingsConflictViewHolder
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme
import com.shelbeely.opentransition.util.getString
import com.shelbeely.opentransition.util.settings.FirebaseSettingUtil
import com.shelbeely.opentransition.util.settings.LockDelay
import com.shelbeely.opentransition.util.settings.LockType
import com.shelbeely.opentransition.util.settings.AppColorVariant
import com.shelbeely.opentransition.util.settings.SettingsManager
import com.shelbeely.opentransition.util.settings.SettingsManager.Key
import com.shelbeely.opentransition.util.settings.SettingsManager.Key.*
import com.shelbeely.opentransition.util.settings.Theme
import com.shelbeely.opentransition.util.toFullDateString
import java.time.LocalDate

object SettingsConflictDialog {
    fun create(differences: List<Pair<Key, Any>>, context: Context): AlertDialog {
        val view = RecyclerView(context).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        }
        val adapter = SettingsConflictAdapter(differences)
        view.adapter = adapter
        view.layoutManager = LinearLayoutManager(context)

        return AlertDialog.Builder(context)
            .setTitle(R.string.settings_conflict)
            .setMessage(R.string.settings_conflict_message)
            .setPositiveButton(R.string.apply_settings) { dialog, _ ->
                val data: Map<String, Any> = differences.mapIndexed { index, (key, value) ->
                    val valueToUse = when (adapter.choices[index]) {
                        true -> value
                        false -> SettingsManager.firebaseValueForKey(key, context)!!
                    }

                    return@mapIndexed key.name to valueToUse
                }.toMap()

                val docRef = FirebaseSettingUtil.getSettingsDocRef()
                docRef.update(data)
                SettingsManager.enableFirebaseSync()
                dialog.dismiss()
            }
            .setView(view)
            .create()
    }

    @Composable
    private fun ConflictItemContent(
        labelText: String,
        localText: String,
        serverText: String,
        useServer: Boolean,
        onChoiceChanged: (useServer: Boolean) -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = labelText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f)
                    .widthIn(min = 128.dp)
                    .padding(end = 8.dp),
            )
            Row {
                OutlinedButton(
                    onClick = { onChoiceChanged(false) },
                    border = BorderStroke(
                        1.dp,
                        if (!useServer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (!useServer) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    ),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                ) {
                    Text(
                        text = localText,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        fontSize = 12.sp,
                    )
                }
                OutlinedButton(
                    onClick = { onChoiceChanged(true) },
                    border = BorderStroke(
                        1.dp,
                        if (useServer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (useServer) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    ),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                ) {
                    Text(
                        text = serverText,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }

    private class SettingsConflictAdapter(val differences: List<Pair<Key, Any>>) :
        RecyclerView.Adapter<SettingsConflictViewHolder>() {
        val choices: Array<Boolean> = Array(differences.size) { true }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            SettingsConflictViewHolder(
                ComposeView(parent.context).apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
                    layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                },
                this,
            )

        override fun getItemCount(): Int = differences.size

        override fun onBindViewHolder(holder: SettingsConflictViewHolder, position: Int) {
            holder.bind(differences[position], choices[position])
        }

        class SettingsConflictViewHolder(
            private val composeView: ComposeView,
            private val adapter: SettingsConflictAdapter,
        ) : RecyclerView.ViewHolder(composeView) {

            fun bind(conflict: Pair<Key, Any>, useServer: Boolean) {
                val (key, serverConflictValue) = conflict

                @StringRes val nameRes: Int
                val localValue: String
                val serverValue: String

                when (key) {
                    lockCode -> {
                        nameRes = R.string.lock_code_label
                        localValue = when {
                            SettingsManager.getLockCode()
                                .isEmpty() -> composeView.getString(R.string.no_code)

                            else -> composeView.getString(R.string.use_local_code)
                        }
                        serverValue = when {
                            ((serverConflictValue as? String)
                                ?: "").isEmpty() -> composeView.getString(R.string.no_code)

                            else -> composeView.getString(R.string.use_server_code)
                        }
                    }

                    lockDelay -> {
                        nameRes = R.string.lock_delay_label
                        localValue = composeView.getString(
                            SettingsManager.getLockDelay().displayNameRes()
                        )
                        serverValue = composeView.getString(
                            LockDelay.valueOf(serverConflictValue as String).displayNameRes()
                        )
                    }

                    lockType -> {
                        nameRes = R.string.select_lock_mode
                        localValue = composeView.getString(
                            SettingsManager.getLockType().displayNameRes()
                        )
                        serverValue = composeView.getString(
                            LockType.valueOf(serverConflictValue as String).displayNameRes()
                        )
                    }

                    startDate -> {
                        nameRes = R.string.start_date_label
                        localValue = SettingsManager.getStartDate(composeView.context)
                            .toFullDateString(composeView.context)
                        serverValue = LocalDate.ofEpochDay(serverConflictValue as Long)
                            .toFullDateString(composeView.context)
                    }

                    theme -> {
                        nameRes = R.string.theme_label
                        localValue = composeView.getString(SettingsManager.getTheme().displayNameRes())
                        serverValue = composeView.getString(
                            Theme.valueOf(serverConflictValue as String).displayNameRes()
                        )
                    }

                    colorVariant -> {
                        nameRes = R.string.theme_label
                        localValue = SettingsManager.getColorVariant().name
                        serverValue = serverConflictValue as String
                    }

                    enableAnalytics -> {
                        nameRes = R.string.anonymous_analytics
                        localValue = composeView.getString(
                            SettingsManager.getEnableAnalytics().displayNameRes()
                        )
                        serverValue = composeView.getString(
                            (serverConflictValue as Boolean).displayNameRes()
                        )
                    }

                    enableCrashReports -> {
                        nameRes = R.string.anonymous_crash_reports
                        localValue = composeView.getString(
                            SettingsManager.getEnableCrashReports().displayNameRes()
                        )
                        serverValue = composeView.getString(
                            (serverConflictValue as Boolean).displayNameRes()
                        )
                    }

                    showAds -> {
                        nameRes = R.string.support_ads
                        localValue = composeView.getString(SettingsManager.showAds().displayNameRes())
                        serverValue = composeView.getString(
                            (serverConflictValue as Boolean).displayNameRes()
                        )
                    }

                    encryptedDatabaseEnabled -> {
                        nameRes = R.string.encrypted_database
                        localValue = composeView.getString(SettingsManager.isEncryptedDatabaseEnabled().displayNameRes())
                        serverValue = composeView.getString(
                            (serverConflictValue as Boolean).displayNameRes()
                        )
                    }

                    decoyVaultEnabled -> {
                        nameRes = R.string.decoy_vault
                        localValue = composeView.getString(SettingsManager.isDecoyVaultEnabled().displayNameRes())
                        serverValue = composeView.getString(
                            (serverConflictValue as Boolean).displayNameRes()
                        )
                    }

                    quickHideEnabled -> {
                        nameRes = R.string.quick_hide
                        localValue = composeView.getString(SettingsManager.isQuickHideEnabled().displayNameRes())
                        serverValue = composeView.getString(
                            (serverConflictValue as Boolean).displayNameRes()
                        )
                    }

                    currentAndroidVersion, incorrectPasswordCount, saveToFirebase,
                    showAccountWarning, showWelcome, userLastSeen, decoyLockCode -> throw IllegalArgumentException(
                        "This settings should not be in conflict because they don't get synced"
                    )
                }

                val labelText = composeView.context.getString(nameRes)

                composeView.setContent {
                    OpenTransitionTheme(colorVariant = SettingsManager.getResolvedComposeColorVariant()) {
                        ConflictItemContent(
                            labelText = labelText,
                            localText = localValue,
                            serverText = serverValue,
                            useServer = useServer,
                            onChoiceChanged = { server -> adapter.choices[bindingAdapterPosition] = server },
                        )
                    }
                }
            }
        }
    }

    @StringRes
    private fun Boolean.displayNameRes() = when (this) {
        true -> R.string.enabled
        false -> R.string.disabled
    }
}

