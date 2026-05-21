# Feature: TransTracks `.ttbackup` Import

## Summary

OpenTransition can import a `.ttbackup` archive produced by upstream TransTracks
(or a previous version of OpenTransition itself). The user picks the file via the
system picker; settings, milestones, photos, and audio analyses inside the
archive are merged into the active database, and image/audio blobs are extracted
to app-private storage.

## Entry points

| Surface | Component | Path |
|---|---|---|
| Intent filters | `MainActivity` (handles `VIEW` + `SEND` for `application/ttbackup` & `*.ttbackup`) | [`mobile/src/main/AndroidManifest.xml`](../../../mobile/src/main/AndroidManifest.xml), [`ui/MainActivity.kt:308-391`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/MainActivity.kt) |
| Settings → Import button | `SettingsFragment.kt:866-905` | [`ui/settings/SettingsFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt) |
| Realm-backup importer (legacy `.realm` files) | `RealmBackupImporter` | [`database/migration/RealmBackupImporter.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/migration/RealmBackupImporter.kt) |
| In-place Realm→Room migration | `RealmToRoomMigration` | [`database/migration/RealmToRoomMigration.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/migration/RealmToRoomMigration.kt) |

> Two different import paths exist: `.ttbackup` (ZIP) via Settings or intent; `.realm` raw Realm file via `RealmBackupImporter`. See [`data/ttbackup-format.md`](../data/ttbackup-format.md) for the on-disk shape of the ZIP.

## Persistence touched

| Target | Writer |
|---|---|
| Room `milestones`, `photos`, `audio_analysis` | `RealmBackupImporter.importFromBackup` (writes `MilestoneEntity` / `PhotoEntity` / `AudioAnalysisEntity` via DAOs) — [`RealmBackupImporter.kt:77-100, 100+`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/migration/RealmBackupImporter.kt) |
| Image/audio files | Extracted to `getMediaFile(filename)` inside `filesDir` — [`Photo.fromJson` at `Photo.kt:107-108`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Photo.kt) |
| Settings | `SettingsManager.getSettingsFromJson(jsonReader)` — `currentAndroidVersion`, `startDate`, `theme`, `colorVariant` only ([`SettingsManager.kt:240-275`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt)) |

## Realm models read

`Photo`, `Milestone`, `AudioAnalysis` ([`data/`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/)). These are kept only for the import path — see [`data/realm-models.md`](../data/realm-models.md).

## External APIs

- **Android `ContentResolver.openInputStream`** to read the picked URI.
- **`java.util.zip.ZipInputStream`** to unpack `.ttbackup` (see `MainActivity.processIntent` and the dedicated unpacker — TODO file under `util/`).
- **Realm Kotlin SDK** to open the legacy `.realm` file when importing a raw Realm backup ([`RealmBackupImporter.kt:60-67`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/migration/RealmBackupImporter.kt)).

## Known issues (audit cross-reference)

- 🟡 [`MainActivity.kt:308-312`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/MainActivity.kt) — three TODOs around `.ttbackup` intent handling.
- 🟡 `SettingsFragment.kt:558` TODO: import progress is not surfaced to the user.
- 🟡 `MainActivity.kt:391` TODO: Realm read on the main thread during import.
- 🟡 Export of `.ttbackup` from OpenTransition is partial (settings JSON works; decoy-vault export is a TODO at `SettingsFragment.kt:776`).

## Invariants (rewrite must preserve)

- **`.ttbackup` round-trip** — a backup created by upstream TransTracks must import losslessly; a backup created by OpenTransition must be readable by any later OpenTransition version.
- Photo IDs from the backup are preserved (UUIDs round-trip via [`Photo.fromJson`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Photo.kt)).
- A backup file referencing a missing image throws and that single photo is skipped, not the whole import ([`Photo.kt:122-125`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Photo.kt)).

## Tests

None directly cover end-to-end import as of this pass.
