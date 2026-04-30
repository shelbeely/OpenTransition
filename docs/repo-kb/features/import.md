# Feature: TransTracks `.ttbackup` Import

## User-Facing Behavior

OpenTransition can ingest data from the legacy TransTracks app via two distinct paths:

1. **`.ttbackup` file open (primary path).** Opening a `.ttbackup` file from any file browser (Gmail attachment, ES File Explorer, Files app, etc.) launches OpenTransition. A confirmation dialog ("Import warning") is shown; on **Yes**, a full-screen progress bar (0 → 50 → 100%) runs while the zip is unpacked and its `data.json` is replayed into the legacy Realm DB. Photos and audio inside the zip are extracted directly into the app's photo/audio directories. On success, an "Import partial success" or "Import complete" dialog summarises any rows that failed to parse.
2. **Settings → "Import Backup" (secondary path).** A picker prompts the user to choose a raw `.realm` file (not a `.ttbackup` zip). The file is copied into `cacheDir`, opened read-only via the Realm Kotlin SDK, and each `Milestone` / `Photo` / `AudioAnalysis` row is inserted directly into the **Room** database via `DatabaseManager`.

After the legacy-Realm import path, the **Realm → Room** migration (`RealmToRoomMigration`) moves the data into Room when the user enables the encrypted database. Until then, the app reads from both stores in parallel during the migration window.

## Implementation Map

| Layer | Files |
|---|---|
| Intent filters (`.ttbackup`) | [`mobile/src/main/AndroidManifest.xml`](../../../mobile/src/main/AndroidManifest.xml) (three `<intent-filter>` blocks for MIME `application/vnd.ni.ttbackup` plus seven `pathPattern` variants) |
| Intent dispatch | [`mobile/src/main/java/com/shelbeely/opentransition/ui/MainActivity.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/MainActivity.kt) (`processIntent`, `processImport`) |
| Zip parsing helper | [`mobile/src/main/java/com/shelbeely/opentransition/util/ZipEntrys.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/ZipEntrys.kt) (`fileName()` extension) |
| Photo/audio output paths | [`mobile/src/main/java/com/shelbeely/opentransition/util/FileUtil.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/FileUtil.kt) (`getImageFile`, `getAudioFile`, `getTempFile`) |
| JSON parsing | [`mobile/src/main/java/com/shelbeely/opentransition/data/Photo.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Photo.kt), [`Milestone.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Milestone.kt), [`AudioAnalysis.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/AudioAnalysis.kt) (`fromJson` factories) |
| Settings JSON | [`mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) (`getSettingsFromJson`) |
| Legacy Realm config | [`mobile/src/main/java/com/shelbeely/opentransition/util/RealmConfigurations.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/RealmConfigurations.kt), [`util/Realms.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/Realms.kt) (`Realm.openDefault()`) |
| Settings-driven `.realm` import | [`mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt) (`showImportBackupDialog`, `backupPickerLauncher`, `performBackupImport`), [`SettingsScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsScreen.kt) |
| Realm → Room copy (settings path) | [`mobile/src/main/java/com/shelbeely/opentransition/database/migration/RealmBackupImporter.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/migration/RealmBackupImporter.kt) |
| In-place Realm → Room migration | [`mobile/src/main/java/com/shelbeely/opentransition/database/migration/RealmToRoomMigration.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/migration/RealmToRoomMigration.kt) (`migrate`, `isMigrationComplete`) |
| Migration trigger | [`mobile/src/main/java/com/shelbeely/opentransition/OpenTransitionApp.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/OpenTransitionApp.kt) (`triggerAutomaticMigrationIfNeeded`) |
| Strings | [`mobile/src/main/res/values/strings.xml`](../../../mobile/src/main/res/values/strings.xml) (`import_backup`, `import_warning_*`, `import_error`, `import_failure`, `import_partial_success_*`, `import_in_progress`, `import_complete`) |
| Tests | None located. See [`_state/unknowns.md`](../_state/unknowns.md). |

## Flow

### A. `.ttbackup` (zip) → legacy Realm

1. **Filter match.** The user opens a `.ttbackup` file. Three intent filters in `AndroidManifest.xml` cover (a) explicit MIME `application/vnd.ni.ttbackup`, (b) `*/*` MIME with seven `.*\.ttbackup` `pathPattern` variants (workaround for Android's PatternMatcher dot handling), and (c) blank-MIME ES File Explorer behaviour.
2. **Confirmation.** `MainActivity.processIntent(intent)` reads `intent.data`, then shows the `import_warning_*` dialog. **Yes** calls `processImport(fileUri)`.
3. **Unzip.** On RxJava `io()` scheduler, `processImport` opens the URI via `contentResolver.openInputStream` and walks each `ZipEntry` with a `ZipInputStream`. The destination per entry is decided by prefix:
   - `data.json` → temp file via `FileUtil.getTempFile("data.json")`
   - `audio/<name>` → `FileUtil.getAudioFile(name)` under `<filesDir>/audio/`
   - `photos/<name>` → `FileUtil.getImageFile(name)` under `<filesDir>/photos/`
   - any other entry → assumed legacy image, written to `<filesDir>/photos/` for backward compatibility
4. **Progress.** After unzip the activity posts `binding.loadingProgress.progress = 50` to the UI thread.
5. **Replay JSON into Realm.** `JsonReader` reads `data.json` and, inside `realm.writeBlocking { ... }`, dispatches by top-level key:
   - `settings` → `SettingsManager.getSettingsFromJson(jsonReader)`
   - `photos` → array of `Photo.fromJson(...)` → `copyToRealm(photo, UpdatePolicy.ALL)`
   - `milestones` → array of `Milestone.fromJson(...)` → `copyToRealm(...)`
   - `audioAnalysis` → array of `AudioAnalysis.fromJson(...)` → `copyToRealm(...)`
   Any unknown top-level key is skipped via `jsonReader.skipValue()`.
6. **Result.** Per-entity counters (`photoImportIssues`, `milestoneImportIssues`, `audioAnalysisImportIssues`) are incremented when `fromJson` returns `null`. The result is folded into `ImportResult.Success(...)` or `ImportResult.Failure`.
7. **UI.** Back on the main scheduler the loading layout is hidden and one of three dialogs is shown: `import_failure`, `import_partial_success_*` (with a localized list of partial counts), or success.

### B. Settings → `.realm` → Room

1. **Picker.** `SettingsFragment.showImportBackupDialog` shows a confirmation, then launches `Intent.ACTION_OPEN_DOCUMENT` with `type = "*/*"` and `EXTRA_MIME_TYPES = ["*/*", "application/octet-stream"]`.
2. **Import.** `performBackupImport(uri)` shows a non-cancellable progress dialog and awaits `RealmBackupImporter.importFromBackup(context, uri)` on `Dispatchers.IO`.
3. **Copy & open Realm.** `RealmBackupImporter.copyBackupToTemp(...)` streams the URI into `<cacheDir>/temp_import_realm.realm`, then opens it with a `RealmConfiguration` whose schema is `{Milestone, Photo, AudioAnalysis}` and whose directory is `cacheDir`. (The file is opened *read-only* in effect — only `query` is used; no writes.)
4. **Copy rows into Room.** Each `realm.query(...).find()` collection is iterated; per row a `MilestoneEntity` / `PhotoEntity` / `AudioAnalysisEntity` is built and inserted via `DatabaseManager.getDatabase(context).<dao>().insert<X>(...)`. Per-row exceptions increment failure counters; the result is `ImportResult(success, error?, milestonesSuccess/Failed, photosSuccess/Failed, audioAnalysesSuccess/Failed)`.
5. **Cleanup.** A `finally` block closes the Realm and deletes `temp_import_realm.realm` even on failure.
6. **UI.** Snackbar shows `import_complete` (with total) or `import_failed` (with error message).

### C. Realm → Room (in-place, separate from import)

`RealmToRoomMigration.migrate(context)` (called from the Settings encrypted-DB wizard, not at app start) opens the legacy Realm via `Realm.openDefault()`, copies the same three entity classes into Room through `DatabaseManager`, and persists `realm_to_room_migration_completed` in the `migration_prefs` SharedPreferences. `OpenTransitionApp.triggerAutomaticMigrationIfNeeded()` *checks* this flag and logs that migration will run when the user enables the encrypted database — it does not run the migration itself.

The Realm DB is only read for migration/import; no new writes go through Realm except the legacy `.ttbackup` replay in flow A. See [`dependency-map.md`](../dependency-map.md) for the read-only intent.

## Config and Environment

- **MIME type:** `application/vnd.ni.ttbackup` (the legacy TransTracks vendor MIME).
- **Photos directory:** `<context.filesDir>/photos/`. **Audio directory:** `<context.filesDir>/audio/`. **Temp:** `<context.filesDir>/temp/`. **Realm temp scratch:** `<context.cacheDir>/temp_import_realm.realm`.
- **No external storage permission** is requested for either flow — both rely on `ContentResolver` and the SAF picker.
- **No persisted progress.** The progress bar is in-memory only; killing the app mid-import leaves the unpacked photos on disk and a partially populated Realm.
- **Activity orientation:** `MainActivity` is locked to portrait (see manifest `screenOrientation="portrait"`) so the long-running import does not cross config-change.
- **Boundary with Room:** Flow A writes to Realm only. To make those rows visible in Room, the user (or settings flow) must trigger `RealmToRoomMigration`.

## Failure Modes

- **Null `intent.data`** → `import_error` dialog, no work done.
- **`openInputStream` returns null / throws** → outer `catch (Exception)` returns `ImportResult.Failure`; user sees `import_failure`.
- **Entry write throws** (per-entry `try/catch` around `FileOutputStream`) → printed to Logcat, the entry is silently skipped, the loop continues. There is no per-file error count surfaced to the user.
- **`data.json` missing or empty** → `dataFile == null || dataFile.length() == 0` → `ImportResult.Failure`.
- **Per-row JSON parse failure** → `Photo.fromJson` / `Milestone.fromJson` / `AudioAnalysis.fromJson` returns `null` and the corresponding `*ImportIssues` counter increments; user sees `import_partial_success_*` with the localized counts.
- **Settings-path: unreadable URI** → `RealmBackupImporter.copyBackupToTemp` throws `IllegalArgumentException("Unable to read the selected backup file...")`; user sees `import_failed`.
- **Settings-path: per-row insert failure** → caught and counted as `*Failed`; the import is still reported as `success = true` if any rows succeeded.
- **Schema drift.** Both flows read the legacy Realm schema fixed in `RealmConfigurations.kt` (`Milestone`, `Photo`, `AudioAnalysis`). Adding fields to those classes without keeping them backward-compatible will break existing `.ttbackup` files.
- **No checksum / signature** is validated on `.ttbackup`; any zip ending in `.ttbackup` will be parsed.

## How to Modify Safely

- Treat the **legacy Realm schema** (`Milestone`, `Photo`, `AudioAnalysis`) as a **wire format**. Field renames or removals will silently drop data on import. Add fields with defaults; never remove.
- If you change `FileUtil.getImageFile` / `getAudioFile` paths, also update the per-entry `when { ... }` in `MainActivity.processImport` so extracted bytes still land in the gallery.
- The 50% progress jump is hard-coded mid-flow. If you split the import into more steps, drive the progress from the actual byte/entry count rather than constants.
- Both import paths run on background dispatchers/schedulers (`RxSchedulers.io()`, `Dispatchers.IO`). Do not introduce Realm or Room calls on the main thread inside these paths.
- The `.realm` settings-path importer copies the entire backup into `cacheDir` and only deletes it in a `finally`. Do not switch to a streaming-read approach without preserving that cleanup contract.
- Add `RealmBackupImporter` test coverage if you change its mapping logic — there is currently none ([`_state/unknowns.md`](../_state/unknowns.md)).
- If you change the MIME or extension, update **all three** `<intent-filter>` blocks in `AndroidManifest.xml` and the corresponding `pathPattern` workarounds.
- Update [`features/index.md`](./index.md) and [`_state/coverage.md`](../_state/coverage.md) if the import surface changes.

## Evidence

- [`mobile/src/main/AndroidManifest.xml`](../../../mobile/src/main/AndroidManifest.xml) (`.ttbackup` intent filters)
- [`mobile/src/main/java/com/shelbeely/opentransition/ui/MainActivity.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/MainActivity.kt) (`processIntent`, `processImport`, `ImportResult` sealed class)
- [`mobile/src/main/java/com/shelbeely/opentransition/database/migration/RealmBackupImporter.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/migration/RealmBackupImporter.kt)
- [`mobile/src/main/java/com/shelbeely/opentransition/database/migration/RealmToRoomMigration.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/migration/RealmToRoomMigration.kt)
- [`mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt) (`showImportBackupDialog`, `performBackupImport`)
- [`mobile/src/main/java/com/shelbeely/opentransition/util/RealmConfigurations.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/RealmConfigurations.kt)
- [`mobile/src/main/java/com/shelbeely/opentransition/util/FileUtil.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/FileUtil.kt)
- [`mobile/src/main/java/com/shelbeely/opentransition/data/Photo.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Photo.kt), [`Milestone.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Milestone.kt), [`AudioAnalysis.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/AudioAnalysis.kt)
- [`mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) (`getSettingsFromJson`)
- [`mobile/src/main/res/values/strings.xml`](../../../mobile/src/main/res/values/strings.xml) (`import_*` strings)
