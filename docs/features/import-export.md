# Import & Export

Developer documentation for import/export functionality.

## Overview

OpenTransition supports importing data from the original **TransTracks** app (now retired) and exporting/importing its own `.ttbackup` archives. Data portability is a core feature — you should never be locked in.

## Importing from TransTracks (Migration Guide)

**TransTracks was retired from the Google Play Store in 2025.** OpenTransition is the recommended successor and is fully compatible with the `.ttbackup` format that TransTracks exports.

### Step 1: Export from TransTracks

If you still have TransTracks installed:

1. Open TransTracks
2. Go to **Settings → Export**
3. The app creates a `.ttbackup` file and opens the Android share sheet
4. Save it somewhere safe (Google Drive, Files app, email, etc.)

!!! tip "Reinstalling TransTracks"
    If you've uninstalled TransTracks, it may still be available for reinstall from the Play Store under **Manage apps and device → Manage → Not installed**. Android auto-backup may restore your data.

!!! info ".ttbackup format"
    A `.ttbackup` file is a standard ZIP archive with a custom extension. You can rename it to `.zip` to inspect its contents on a computer. It contains raw photo files and a JSON metadata file.

### Step 2: Import into OpenTransition

1. Locate your `.ttbackup` file
2. Tap the file — Android will offer to open it with OpenTransition
3. Alternatively, go to **Settings → Import Backup** and select the file
4. Confirm the import
5. Wait for completion — all photos, milestones, and metadata are preserved

### What Gets Imported

| Item | Imported? |
|------|-----------|
| Photos (face, body, custom) | ✅ Yes |
| Milestones | ✅ Yes |
| Audio recordings | ✅ Yes |
| Audio analysis data (pitch, formants) | ✅ Yes |
| Timestamps and dates | ✅ Yes |
| App settings (theme, lock type) | ✅ Yes |

### Technical Details

TransTracks used Realm as its database. OpenTransition uses Room. The backwards-compatible import pipeline handles the conversion:

- **`RealmBackupImporter.kt`** — reads the `.ttbackup` ZIP, extracts photos and JSON metadata
- **`RealmToRoomMigration.kt`** — converts Realm-format data to Room entities
- All Realm data model classes (`Photo.kt`, `Milestone.kt`, `AudioAnalysis.kt`) are kept solely for import compatibility
- All Realm-related code is marked with `BACKWARDS COMPATIBILITY` comments

After import, all data lives in the Room database. The original `.ttbackup` file is not modified.

## OpenTransition Export (`.ttbackup`)

### Exporting Your Data

1. Go to **Settings → Backup & Sync → Export Data**
2. Choose a location to save the `.ttbackup` file
3. Share or copy it to a safe location (Google Drive, external drive, etc.)

The file contains all photos, milestones, audio recordings, analysis data, and settings.

### Export Format

A `.ttbackup` file is a ZIP archive containing:

```
backup.ttbackup (zip)
├── data.json       # All metadata (photos, milestones, settings)
└── images/         # All photo and audio files
    ├── photo_1234567890.jpg
    ├── audio_1234567891.mp3
    └── ...
```

JSON structure example:

```json
{
  "settings": {
    "theme": "pink",
    "lockType": "normal",
    "startDate": 1234567890
  },
  "photos": [
    {
      "id": "uuid-here",
      "timestamp": 1234567890000,
      "epochDay": 19000,
      "filename": "photo_123.jpg",
      "type": 1
    }
  ],
  "milestones": [
    {
      "id": "uuid-here",
      "title": "Started HRT",
      "description": "Began hormone therapy today!",
      "epochDay": 19000,
      "timestamp": 1234567890000
    }
  ]
}
```

## Restoring an OpenTransition Backup

1. Locate your `.ttbackup` file
2. Tap the file or go to **Settings → Import Backup**
3. Confirm import
4. Data is merged or replaced (existing data may be overwritten — review the prompt carefully)

## Cloud Backup (Firebase)

In addition to local `.ttbackup` files, OpenTransition supports optional Firebase cloud sync:

- Sign in with a Google account in **Settings → Backup & Sync**
- Enable Cloud Sync
- Data syncs automatically in the background
- Cloud data is stored in your own Firebase project, not ours

See [Settings & Sync](settings-sync.md) for details.

## Key Files

- `mobile/src/main/java/com/shelbeely/opentransition/data/RealmBackupImporter.kt` — TransTracks `.ttbackup` importer
- `mobile/src/main/java/com/shelbeely/opentransition/data/RealmToRoomMigration.kt` — Realm → Room migration
- `mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt` — Import/export UI entry points

## Related

- [Data Layer](../architecture/data-layer.md) — Database and storage details
- [Credits](../credits.md) — TransTracks attribution
