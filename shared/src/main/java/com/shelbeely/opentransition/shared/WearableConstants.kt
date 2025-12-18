/*
 * Copyright © 2025 OpenTransition. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.shared

/**
 * Constants for Wearable Data Layer communication between mobile and wear apps
 */
object WearableConstants {
    
    // Capability for detecting companion apps
    const val CAPABILITY_MOBILE_APP = "opentransition_mobile_app"
    const val CAPABILITY_WEAR_APP = "opentransition_wear_app"
    
    // Message paths
    const val PATH_TRIGGER_PHOTO = "/opentransition/trigger_photo"
    const val PATH_SYNC_MILESTONES = "/opentransition/sync_milestones"
    const val PATH_MILESTONE_UPDATE = "/opentransition/milestone_update"
    const val PATH_REQUEST_SYNC = "/opentransition/request_sync"
    
    // Camera control paths
    const val PATH_CAMERA_SHUTTER = "/opentransition/camera/shutter"
    const val PATH_CAMERA_ZOOM = "/opentransition/camera/zoom"
    const val PATH_CAMERA_FLASH = "/opentransition/camera/flash"
    const val PATH_CAMERA_SWITCH = "/opentransition/camera/switch"
    
    // Audio recording paths
    const val PATH_AUDIO_START = "/opentransition/audio/start"
    const val PATH_AUDIO_STOP = "/opentransition/audio/stop"
    const val PATH_AUDIO_DATA = "/opentransition/audio/data"
    
    // Data item paths
    const val DATA_PATH_MILESTONES = "/opentransition/data/milestones"
    const val DATA_PATH_SETTINGS = "/opentransition/data/settings"
    const val DATA_PATH_AUDIO = "/opentransition/data/audio"
    
    // Message keys
    const val KEY_PHOTO_TYPE = "photo_type"
    const val KEY_MILESTONE_DATA = "milestone_data"
    const val KEY_MILESTONE_COUNT = "milestone_count"
    const val KEY_LAST_SYNC = "last_sync"
    const val KEY_ZOOM_LEVEL = "zoom_level"
    const val KEY_FLASH_MODE = "flash_mode"
    const val KEY_AUDIO_DATA = "audio_data"
    const val KEY_AUDIO_FILENAME = "audio_filename"
    
    // Photo types
    const val PHOTO_TYPE_FACE = "face"
    const val PHOTO_TYPE_BODY = "body"
    
    // Camera control values
    const val FLASH_MODE_AUTO = "auto"
    const val FLASH_MODE_ON = "on"
    const val FLASH_MODE_OFF = "off"
}
