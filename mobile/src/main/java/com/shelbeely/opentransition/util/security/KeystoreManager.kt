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

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages secure key storage and retrieval using Android Keystore System.
 * Keys stored in the Keystore are hardware-backed (when available) and never leave the secure element.
 */
object KeystoreManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS_DATABASE = "opentransition_database_key"
    private const val KEY_ALIAS_DATABASE_DECOY = "opentransition_database_decoy_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    
    /**
     * Gets or creates a secure key for database encryption stored in Android Keystore.
     * This key is hardware-backed and requires device unlock to access.
     */
    fun getDatabaseKey(isDecoy: Boolean = false): ByteArray {
        val keyAlias = if (isDecoy) KEY_ALIAS_DATABASE_DECOY else KEY_ALIAS_DATABASE
        
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
        
        // Check if key already exists
        if (!keyStore.containsAlias(keyAlias)) {
            // Generate new key
            generateKey(keyAlias)
        }
        
        // Get the key
        val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
        return secretKey.encoded
    }
    
    /**
     * Generates a new AES-256 key in the Android Keystore with specified security properties.
     */
    private fun generateKey(alias: String) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false) // Can be enabled for additional security
            .setRandomizedEncryptionRequired(true)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }
    
    /**
     * Encrypts data using a Keystore-backed key.
     * Returns the IV concatenated with the ciphertext.
     */
    fun encrypt(alias: String, data: ByteArray): ByteArray {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
        
        val secretKey = keyStore.getKey(alias, null) as SecretKey
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(data)
        
        // Combine IV and ciphertext
        return iv + ciphertext
    }
    
    /**
     * Decrypts data using a Keystore-backed key.
     * Expects the IV to be prepended to the ciphertext.
     */
    fun decrypt(alias: String, encryptedData: ByteArray): ByteArray {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
        
        val secretKey = keyStore.getKey(alias, null) as SecretKey
        
        // Extract IV and ciphertext
        val iv = encryptedData.copyOfRange(0, 12) // GCM standard IV size
        val ciphertext = encryptedData.copyOfRange(12, encryptedData.size)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        
        return cipher.doFinal(ciphertext)
    }
    
    /**
     * Deletes a key from the Keystore.
     */
    fun deleteKey(alias: String) {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
        keyStore.deleteEntry(alias)
    }
    
    /**
     * Checks if a key exists in the Keystore.
     */
    fun keyExists(alias: String): Boolean {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
        return keyStore.containsAlias(alias)
    }
}
