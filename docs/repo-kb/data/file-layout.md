# File Layout (app-private storage)

> Everything user-content lives **inside the app-private data dir**. No external
> storage paths are written. Photos, audio, the Room database, and (legacy) Realm
> database all live under `/data/data/com.shelbeely.opentransition/` (or whatever
> `Context.filesDir` / `databases/` resolves to per device).

## Roots

| Logical name | Resolver | Typical absolute path |
|---|---|---|
| `filesDir` | `Context.filesDir` | `/data/user/0/com.shelbeely.opentransition/files` |
| `cacheDir` | `Context.cacheDir` | `/data/user/0/com.shelbeely.opentransition/cache` |
| `databases` | Room internal | `/data/user/0/com.shelbeely.opentransition/databases/` |
| `shared_prefs` | SharedPreferences internal | `/data/user/0/com.shelbeely.opentransition/shared_prefs/` |

## Photos and audio (within `filesDir`)

`FileUtil.getMediaFile(name)` resolves a media file by name inside `filesDir` ([`util/FileUtil.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/FileUtil.kt)). The exact subfolder structure is determined there.

Audio sent from the watch lands in `filesDir/audio/` ([`MobileWearableListenerService.kt:127`](../../../mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt)):

```
filesDir/
└── audio/
    └── <sanitized filename>.3gp        ← path-traversal-checked (see service code)
```

For phone-captured audio, `RecordAudioFragment` writes via `MediaRecorder` to a path under `filesDir` (see [`ui/recordaudio/RecordAudioFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/recordaudio/RecordAudioFragment.kt)).

## Database files

| File | Owner |
|---|---|
| `databases/opentransition.db` | Real Room DB ([`AppDatabase.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/AppDatabase.kt)) |
| `databases/opentransition_decoy.db` | Decoy Room DB ([`AppDatabase.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/AppDatabase.kt)) |
| Legacy `default.realm` (or whatever `Realm.configurationOf(default)` resolves to) | Legacy Realm DB; read by import & by Wear handlers |

When SQLCipher is enabled (today: never), the two `.db` files are encrypted with their respective passphrases from `EncryptedSharedPreferences "opentransition_db_keys"` ([`KeystoreManager.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/KeystoreManager.kt)).

## Temp directory

`FileUtil.clearTempFolder()` is called from `OpenTransitionApp.onCreate` ([`OpenTransitionApp.kt:83`](../../../mobile/src/main/java/com/shelbeely/opentransition/OpenTransitionApp.kt)); its target lives inside `filesDir` per [`util/FileUtil.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/FileUtil.kt).

## Crash log

`CrashLogger.install(this)` writes a plain-text per-launch crash log inside the app data dir so the user can retrieve it without a debugger ([`util/CrashLogger.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/CrashLogger.kt), wired in [`OpenTransitionApp.kt:74-89`](../../../mobile/src/main/java/com/shelbeely/opentransition/OpenTransitionApp.kt)).

## Wear-side files

The Wear app uses its own `cacheDir` for the most recent `MediaRecorder` output before streaming it via `ChannelClient` ([`wear/AudioRecordActivity.kt`](../../../wear/src/main/java/com/shelbeely/opentransition/wear/AudioRecordActivity.kt)).

## What is **not** here

- No SAF/MediaStore writes.
- No external storage (`getExternalFilesDir`) writes.
- No cloud sync of photo or audio bytes (only Firestore settings — see [`apis/firebase-contracts.md`](../apis/firebase-contracts.md)).

## Invariants

- A capture that fails to write the file before adding the Room row creates an orphan DB row. Restoring from `.ttbackup` re-validates file existence ([`Photo.kt:122-125`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Photo.kt)).
- All inter-app sharing of files goes through `TransTracksFileProvider` (`androidx.core.content.FileProvider` subclass) — [`data/TransTracksFileProvider.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/TransTracksFileProvider.kt).
