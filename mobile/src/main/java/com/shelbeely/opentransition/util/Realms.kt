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

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

/**
 * **BACKWARDS COMPATIBILITY**: Realm extension function for compatibility with the old app.
 * 
 * This utility is retained to support importing backups from the forked version.
 * The new app uses Room database with DatabaseManager.
 * 
 * Used by: RealmToRoomMigration, RealmBackupImporter
 */
fun Realm.Companion.openDefault() = Realm.open(RealmConfiguration.default)