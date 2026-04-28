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
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shelbeely.opentransition.database.room.dao.AudioAnalysisDao
import com.shelbeely.opentransition.database.room.dao.MilestoneDao
import com.shelbeely.opentransition.database.room.dao.PhotoDao
import com.shelbeely.opentransition.database.room.dao.VoiceGoalDao
import com.shelbeely.opentransition.database.room.entities.AudioAnalysisEntity
import com.shelbeely.opentransition.database.room.entities.MilestoneEntity
import com.shelbeely.opentransition.database.room.entities.PhotoEntity
import com.shelbeely.opentransition.database.room.entities.VoiceGoalEntity
import com.shelbeely.opentransition.util.settings.SettingsManager
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        MilestoneEntity::class,
        PhotoEntity::class,
        AudioAnalysisEntity::class,
        VoiceGoalEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun milestoneDao(): MilestoneDao
    abstract fun photoDao(): PhotoDao
    abstract fun audioAnalysisDao(): AudioAnalysisDao
    abstract fun voiceGoalDao(): VoiceGoalDao
    
    companion object {
        private const val DATABASE_NAME = "opentransition.db"
        private const val DECOY_DATABASE_NAME = "opentransition_decoy.db"

        // ── Migrations ────────────────────────────────────────────────────

        /**
         * 1 → 2: Add extended voice-analysis columns and session summary text
         * to the audio_analysis table.  All new columns have sensible defaults
         * so existing rows stay valid.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE audio_analysis ADD COLUMN pitchConfidenceMean REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE audio_analysis ADD COLUMN voicedRatio REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE audio_analysis ADD COLUMN intensityMeanDb REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE audio_analysis ADD COLUMN intensityMaxDb REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE audio_analysis ADD COLUMN pitchRangeHz REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE audio_analysis ADD COLUMN pitchStabilityScore REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE audio_analysis ADD COLUMN intonationMovement REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE audio_analysis ADD COLUMN sessionSummaryText TEXT NOT NULL DEFAULT ''")
            }
        }

        /**
         * 2 → 3: Add the voice_goals table for target-range tracking.
         * No existing tables are modified.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS voice_goals (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        metricKey TEXT NOT NULL,
                        targetMin REAL NOT NULL,
                        targetMax REAL NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        // ── Singleton ─────────────────────────────────────────────────────
        
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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
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
