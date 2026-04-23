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
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shelbeely.opentransition.database.room.dao.AudioAnalysisDao
import com.shelbeely.opentransition.database.room.dao.MilestoneDao
import com.shelbeely.opentransition.database.room.dao.PhotoDao
import com.shelbeely.opentransition.database.room.entities.AudioAnalysisEntity
import com.shelbeely.opentransition.database.room.entities.MilestoneEntity
import com.shelbeely.opentransition.database.room.entities.PhotoEntity
import com.shelbeely.opentransition.util.settings.SettingsManager
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        MilestoneEntity::class,
        PhotoEntity::class,
        AudioAnalysisEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun milestoneDao(): MilestoneDao
    abstract fun photoDao(): PhotoDao
    abstract fun audioAnalysisDao(): AudioAnalysisDao
    
    companion object {
        private const val DATABASE_NAME = "opentransition.db"
        private const val DECOY_DATABASE_NAME = "opentransition_decoy.db"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        @Volatile
        private var DECOY_INSTANCE: AppDatabase? = null
        
        /**
         * Get the database instance (real or decoy based on parameter)
         * Supports both encrypted and unencrypted modes
         */
        fun getInstance(context: Context, isDecoy: Boolean = false): AppDatabase {
            if (isDecoy) {
                return DECOY_INSTANCE ?: synchronized(this) {
                    DECOY_INSTANCE ?: buildDatabase(context, isDecoy).also { DECOY_INSTANCE = it }
                }
            }
            
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context, isDecoy).also { INSTANCE = it }
            }
        }
        
        /**
         * Build the database with optional encryption
         * Encryption is controlled by SettingsManager.isEncryptedDatabaseEnabled()
         */
        private fun buildDatabase(context: Context, isDecoy: Boolean): AppDatabase {
            val dbName = if (isDecoy) DECOY_DATABASE_NAME else DATABASE_NAME
            val isEncrypted = SettingsManager.isEncryptedDatabaseEnabled()
            
            val builder = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                dbName
            )
            
            // Add encryption if enabled
            if (isEncrypted) {
                val passphrase = KeystoreManager.getOrCreateDatabaseKey(context, isDecoy)
                val factory = SupportFactory(passphrase)
                builder.openHelperFactory(factory)
            }
            
            return builder
                // No migrations are required at version 1. Future schema bumps
                // MUST add an explicit `Migration` here; the previous
                // `fallbackToDestructiveMigration()` call would have silently
                // wiped user data on any version bump.
                // See audit-report/07-issues-and-bugs.md ISSUE-006.
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Database created - can initialize with default data if needed
                    }
                })
                .build()
        }
        
        /**
         * Close database instances
         */
        fun closeDatabase(isDecoy: Boolean = false) {
            if (isDecoy) {
                DECOY_INSTANCE?.close()
                DECOY_INSTANCE = null
            } else {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
        
        /**
         * Close all database instances
         */
        fun closeAll() {
            INSTANCE?.close()
            INSTANCE = null
            DECOY_INSTANCE?.close()
            DECOY_INSTANCE = null
        }
    }
}
