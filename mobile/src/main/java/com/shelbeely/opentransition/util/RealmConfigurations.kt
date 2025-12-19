/*
 * Copyright © 2023 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.util

import com.shelbeely.opentransition.data.AudioAnalysis
import com.shelbeely.opentransition.data.Milestone
import com.shelbeely.opentransition.data.Photo
import com.shelbeely.opentransition.util.security.KeystoreManager
import com.shelbeely.opentransition.util.security.VaultManager
import io.realm.kotlin.RealmConfiguration

/**
 * Gets the default Realm configuration with encryption enabled.
 * Uses Android Keystore for secure key management.
 */
val RealmConfiguration.Companion.default
    get() = create(
        setOf(Milestone::class, Photo::class, AudioAnalysis::class),
        name = "default.realm",
        encryptionKey = KeystoreManager.getDatabaseKey(isDecoy = false)
    )

/**
 * Gets the decoy Realm configuration with separate encryption.
 * Used for the two-passcode model to provide plausible deniability.
 */
val RealmConfiguration.Companion.decoy
    get() = create(
        setOf(Milestone::class, Photo::class, AudioAnalysis::class),
        name = "decoy.realm",
        encryptionKey = KeystoreManager.getDatabaseKey(isDecoy = true)
    )

/**
 * Creates a Realm configuration with optional encryption.
 */
fun RealmConfiguration.Companion.create(
    schema: Set<kotlin.reflect.KClass<out io.realm.kotlin.types.RealmObject>>,
    name: String = "default.realm",
    encryptionKey: ByteArray? = null
): RealmConfiguration {
    val builder = RealmConfiguration.Builder(schema)
        .name(name)
        .schemaVersion(1)
    
    encryptionKey?.let { builder.encryptionKey(it) }
    
    return builder.build()
}
