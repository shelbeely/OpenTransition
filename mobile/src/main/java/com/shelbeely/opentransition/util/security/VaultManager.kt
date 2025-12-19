/*
 * Copyright © 2025 OpenTransition. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.util.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.shelbeely.opentransition.util.EncryptionUtil
import com.shelbeely.opentransition.util.settings.PrefUtil

/**
 * Manages vault selection between real and decoy vaults for the two-passcode model.
 * Provides plausible deniability by allowing a decoy passcode to open a harmless vault.
 */
object VaultManager {
    private const val PREFS_NAME = "vault_prefs"
    private const val KEY_DECOY_CODE_HASH = "decoy_code_hash"
    private const val KEY_CURRENT_VAULT = "current_vault"
    
    enum class VaultType {
        REAL,
        DECOY
    }
    
    /**
     * Gets encrypted SharedPreferences for vault management.
     */
    private fun getEncryptedPrefs(context: Context) = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback to standard SharedPreferences if encrypted prefs fail
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Checks if a decoy vault has been configured.
     */
    fun isDecoyVaultConfigured(context: Context): Boolean {
        val prefs = getEncryptedPrefs(context)
        return prefs.contains(KEY_DECOY_CODE_HASH) && 
               prefs.getString(KEY_DECOY_CODE_HASH, null)?.isNotEmpty() == true
    }
    
    /**
     * Sets up the decoy vault with a separate passcode.
     */
    fun setupDecoyVault(context: Context, decoyPasscode: String) {
        val hashedCode = EncryptionUtil.encryptAndEncode(decoyPasscode, PrefUtil.CODE_SALT)
        val prefs = getEncryptedPrefs(context)
        prefs.edit()
            .putString(KEY_DECOY_CODE_HASH, hashedCode)
            .apply()
    }
    
    /**
     * Removes the decoy vault configuration.
     */
    fun removeDecoyVault(context: Context) {
        val prefs = getEncryptedPrefs(context)
        prefs.edit()
            .remove(KEY_DECOY_CODE_HASH)
            .apply()
    }
    
    /**
     * Gets the stored decoy passcode hash.
     */
    fun getDecoyCodeHash(context: Context): String? {
        val prefs = getEncryptedPrefs(context)
        return prefs.getString(KEY_DECOY_CODE_HASH, null)
    }
    
    /**
     * Determines which vault should be opened based on the entered passcode.
     * Returns DECOY if the passcode matches the decoy code, otherwise REAL.
     */
    fun determineVaultType(context: Context, enteredCode: String, realCodeHash: String): VaultType {
        if (!isDecoyVaultConfigured(context)) {
            return VaultType.REAL
        }
        
        val hashedEnteredCode = EncryptionUtil.encryptAndEncode(enteredCode, PrefUtil.CODE_SALT)
        val decoyCodeHash = getDecoyCodeHash(context)
        
        return if (hashedEnteredCode == decoyCodeHash) {
            VaultType.DECOY
        } else {
            VaultType.REAL
        }
    }
    
    /**
     * Sets the currently active vault.
     */
    fun setCurrentVault(context: Context, vaultType: VaultType) {
        val prefs = getEncryptedPrefs(context)
        prefs.edit()
            .putString(KEY_CURRENT_VAULT, vaultType.name)
            .apply()
    }
    
    /**
     * Gets the currently active vault type.
     */
    fun getCurrentVault(context: Context): VaultType {
        val prefs = getEncryptedPrefs(context)
        val vaultName = prefs.getString(KEY_CURRENT_VAULT, VaultType.REAL.name)
        return try {
            VaultType.valueOf(vaultName ?: VaultType.REAL.name)
        } catch (e: IllegalArgumentException) {
            VaultType.REAL
        }
    }
}
