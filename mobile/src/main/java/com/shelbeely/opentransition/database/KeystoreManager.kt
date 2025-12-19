/*
 * Copyright © 2023-2025 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.database

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages encryption keys using Android Keystore
 * Keys are protected by hardware-backed security when available
 */
object KeystoreManager {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "opentransition_db_key"
    private const val DECOY_KEY_ALIAS = "opentransition_decoy_db_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    
    /**
     * Get or generate the master key for database encryption
     */
    fun getOrCreateDatabaseKey(context: Context, isDecoy: Boolean = false): ByteArray {
        val keyAlias = if (isDecoy) DECOY_KEY_ALIAS else KEY_ALIAS
        
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)
        
        // Check if key already exists
        if (!keyStore.containsAlias(keyAlias)) {
            generateDatabaseKey(keyAlias)
        }
        
        // Get the key from keystore
        val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
        return secretKey.encoded
    }
    
    /**
     * Generate a new encryption key in the Android Keystore
     */
    private fun generateDatabaseKey(alias: String) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )
        
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            // Require user authentication to use the key
            .setUserAuthenticationRequired(false) // Set to true if requiring biometric for every access
            .build()
        
        keyGenerator.init(spec)
        keyGenerator.generateKey()
    }
    
    /**
     * Delete the database key from keystore
     */
    fun deleteDatabaseKey(isDecoy: Boolean = false) {
        val keyAlias = if (isDecoy) DECOY_KEY_ALIAS else KEY_ALIAS
        
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)
        
        if (keyStore.containsAlias(keyAlias)) {
            keyStore.deleteEntry(keyAlias)
        }
    }
    
    /**
     * Check if a database key exists
     */
    fun hasDatabaseKey(isDecoy: Boolean = false): Boolean {
        val keyAlias = if (isDecoy) DECOY_KEY_ALIAS else KEY_ALIAS
        
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)
        
        return keyStore.containsAlias(keyAlias)
    }
    
    /**
     * Get a master key for EncryptedSharedPreferences
     */
    fun getMasterKey(context: Context): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
}
