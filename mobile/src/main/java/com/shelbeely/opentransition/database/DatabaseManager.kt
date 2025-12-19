/*
 * Copyright © 2023 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.database

import android.content.Context
import com.shelbeely.opentransition.util.settings.SettingsManager

/**
 * Manages access to the correct database (real or decoy)
 * Handles switching between vaults based on authentication
 */
object DatabaseManager {
    private var currentVaultIsDecoy = false
    
    /**
     * Get the current active database
     */
    fun getDatabase(context: Context): EncryptedDatabase {
        return if (SettingsManager.isEncryptedDatabaseEnabled()) {
            EncryptedDatabase.getInstance(context, currentVaultIsDecoy)
        } else {
            // If encryption is not enabled, always use the real database
            EncryptedDatabase.getInstance(context, false)
        }
    }
    
    /**
     * Switch to the decoy vault
     */
    fun switchToDecoyVault(context: Context) {
        if (SettingsManager.isDecoyVaultEnabled()) {
            currentVaultIsDecoy = true
        }
    }
    
    /**
     * Switch to the real vault
     */
    fun switchToRealVault(context: Context) {
        currentVaultIsDecoy = false
    }
    
    /**
     * Check if currently using the decoy vault
     */
    fun isUsingDecoyVault(): Boolean {
        return currentVaultIsDecoy
    }
    
    /**
     * Verify if a passcode opens the decoy vault
     */
    fun isDecoyPasscode(passcode: String): Boolean {
        if (!SettingsManager.isDecoyVaultEnabled()) {
            return false
        }
        
        val decoyCode = SettingsManager.getDecoyLockCode()
        if (decoyCode.isEmpty()) {
            return false
        }
        
        return com.shelbeely.opentransition.util.EncryptionUtil.encryptAndEncode(
            passcode, 
            com.shelbeely.opentransition.util.settings.PrefUtil.CODE_SALT
        ) == decoyCode
    }
}
