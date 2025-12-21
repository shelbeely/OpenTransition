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
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * Manages encryption keys using EncryptedSharedPreferences
 * Passphrases are protected by hardware-backed MasterKey when available
 */
object KeystoreManager {
    private const val ENCRYPTED_PREFS_NAME = "opentransition_db_keys"
    private const val KEY_REAL_DB = "real_db_passphrase"
    private const val KEY_DECOY_DB = "decoy_db_passphrase"
    
    /**
     * Get or generate the passphrase for database encryption
     * The passphrase is stored securely in EncryptedSharedPreferences
     */
    fun getOrCreateDatabaseKey(context: Context, isDecoy: Boolean = false): ByteArray {
        val prefKey = if (isDecoy) KEY_DECOY_DB else KEY_REAL_DB
        val encryptedPrefs = getEncryptedPrefs(context)
        
        // Check if passphrase already exists
        val existingPassphrase = encryptedPrefs.getString(prefKey, null)
        if (existingPassphrase != null) {
            return existingPassphrase.toByteArray(Charsets.UTF_8)
        }
        
        // Generate a new random passphrase
        val passphrase = generateRandomPassphrase()
        
        // Store it securely
        encryptedPrefs.edit()
            .putString(prefKey, passphrase)
            .apply()
        
        return passphrase.toByteArray(Charsets.UTF_8)
    }
    
    /**
     * Generate a secure random passphrase for the database
     */
    private fun generateRandomPassphrase(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32) // 256 bits
        random.nextBytes(bytes)
        // Convert to hex string for storage
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Delete the database passphrase
     */
    fun deleteDatabaseKey(context: Context, isDecoy: Boolean = false) {
        val prefKey = if (isDecoy) KEY_DECOY_DB else KEY_REAL_DB
        
        try {
            val encryptedPrefs = getEncryptedPrefs(context)
            encryptedPrefs.edit()
                .remove(prefKey)
                .apply()
        } catch (e: java.security.GeneralSecurityException) {
            // Log and ignore security errors during deletion
            android.util.Log.w("KeystoreManager", "Failed to delete database key", e)
        } catch (e: java.io.IOException) {
            // Log and ignore IO errors during deletion
            android.util.Log.w("KeystoreManager", "Failed to delete database key", e)
        }
    }
    
    /**
     * Check if a database passphrase exists
     */
    fun hasDatabaseKey(context: Context, isDecoy: Boolean = false): Boolean {
        val prefKey = if (isDecoy) KEY_DECOY_DB else KEY_REAL_DB
        
        return try {
            val encryptedPrefs = getEncryptedPrefs(context)
            encryptedPrefs.contains(prefKey)
        } catch (e: java.security.GeneralSecurityException) {
            android.util.Log.w("KeystoreManager", "Failed to check database key", e)
            false
        } catch (e: java.io.IOException) {
            android.util.Log.w("KeystoreManager", "Failed to check database key", e)
            false
        }
    }
    
    /**
     * Get EncryptedSharedPreferences instance
     */
    private fun getEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = getMasterKey(context)
        return EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    /**
     * Get a master key for EncryptedSharedPreferences
     */
    private fun getMasterKey(context: Context): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
}
