/*
 * Copyright © 2025-2026 Shelbeely and OpenTransition contributors.
 *
 * Part of OpenTransition, a fork of TransTracks (© 2018-2021 TransTracks),
 * licensed under GPL-3.0-or-later. See the NOTICE and AUTHORS files for
 * full attribution.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.credits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shelbeely.opentransition.R

/**
 * Hand-curated entry for the Open Source Licenses list.
 *
 * Hand-curating avoids pulling in `oss-licenses-plugin` (a Google Play
 * Services dependency) just to render a list. The trade-off is that adding a
 * new third-party library means appending a row to [OssLicensesData.entries]
 * — kept in this file for discoverability.
 */
data class OssEntry(
    val name: String,
    val licenseName: String,
    val licenseUrl: String,
    val notes: String? = null,
)

object OssLicensesData {
    /**
     * The original TransTracks app — the upstream this fork is based on.
     * Always pinned at the top of the list; do not mix into [entries].
     */
    val upstream = OssEntry(
        name = "TransTracks (Android)",
        licenseName = "GNU GPL v3.0",
        licenseUrl = CreditsLinks.TRANSTRACKS_REPO,
        notes = "Original photo-journal app © 2018-2021 TransTracks, written " +
                "single-handedly by Cassie Wilson. Archived in 2025; " +
                "OpenTransition is the active continuation."
    )

    /**
     * Bundled third-party libraries. Sorted alphabetically. When adding a
     * dependency to `mobile/build.gradle`, please add a corresponding entry
     * here so users continue to see correct attribution.
     */
    val entries: List<OssEntry> = listOf(
        OssEntry(
            "AndroidX (Jetpack)",
            "Apache License 2.0",
            "https://www.apache.org/licenses/LICENSE-2.0",
            "AppCompat, Activity, Compose, Lifecycle, Navigation, Room, " +
                    "RecyclerView, ConstraintLayout, ExifInterface, Biometric, " +
                    "Security-Crypto, CameraX, DynamicAnimation, SQLite."
        ),
        OssEntry(
            "Coil 3 (Compose & OkHttp)",
            "Apache License 2.0",
            "https://github.com/coil-kt/coil/blob/main/LICENSE.txt"
        ),
        OssEntry(
            "Firebase Android SDK",
            "Apache License 2.0",
            "https://github.com/firebase/firebase-android-sdk/blob/master/LICENSE",
            "Analytics, Auth, Crashlytics, Firestore, FirebaseUI-Auth."
        ),
        OssEntry(
            "Google Material Components for Android",
            "Apache License 2.0",
            "https://github.com/material-components/material-components-android/blob/master/LICENSE"
        ),
        OssEntry(
            "Google Play Services (Ads, Auth, Wearable, Review)",
            "Android Software Development Kit License",
            "https://developer.android.com/studio/terms"
        ),
        OssEntry(
            "Gson",
            "Apache License 2.0",
            "https://github.com/google/gson/blob/main/LICENSE"
        ),
        OssEntry(
            "Kotlin Standard Library",
            "Apache License 2.0",
            "https://github.com/JetBrains/kotlin/blob/master/license/README.md"
        ),
        OssEntry(
            "kotlinx.coroutines (incl. coroutines-rx3)",
            "Apache License 2.0",
            "https://github.com/Kotlin/kotlinx.coroutines/blob/master/LICENSE.txt"
        ),
        OssEntry(
            "ML Kit Face Detection",
            "Android Software Development Kit License",
            "https://developers.google.com/ml-kit/terms"
        ),
        OssEntry(
            "Realm Kotlin SDK",
            "Apache License 2.0",
            "https://github.com/realm/realm-kotlin/blob/main/LICENSE",
            "Used read-only to import legacy TransTracks .ttbackup files."
        ),
        OssEntry(
            "RxBinding 3",
            "Apache License 2.0",
            "https://github.com/JakeWharton/RxBinding/blob/master/LICENSE.txt"
        ),
        OssEntry(
            "RxJava 3 / RxAndroid 3 / RxRelay 3",
            "Apache License 2.0",
            "https://github.com/ReactiveX/RxJava/blob/3.x/LICENSE"
        ),
        OssEntry(
            "SQLCipher for Android",
            "BSD-style (Zetetic)",
            "https://www.zetetic.net/sqlcipher/license/",
            "Optional encrypted database support."
        ),
        OssEntry(
            "CursorRecyclerViewAdapter",
            "Apache License 2.0",
            "https://github.com/skyfishjy/android-ribbon-menu",
            "Adapted from work © 2014 skyfish.jy@gmail.com; copyright " +
                    "preserved in source."
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OssLicensesScreen(
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.oss_screen_title),
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.oss_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            item {
                SectionTitle(stringResource(R.string.oss_section_upstream))
                EntryRow(OssLicensesData.upstream, onOpenUrl)
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
            }

            item {
                SectionTitle(stringResource(R.string.oss_section_libraries))
            }

            items(OssLicensesData.entries) { entry ->
                EntryRow(entry, onOpenUrl)
                HorizontalDivider()
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun EntryRow(entry: OssEntry, onOpenUrl: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = entry.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = entry.licenseName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!entry.notes.isNullOrBlank()) {
            Text(
                text = entry.notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        TextButton(
            onClick = { onOpenUrl(entry.licenseUrl) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = stringResource(R.string.oss_view_license_link))
        }
    }
}
