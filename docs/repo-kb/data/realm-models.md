# Realm Models (read-only, import-only)

> The codebase is mid-migration from Realm Kotlin SDK (`io.realm.kotlin:library-base:2.3.0`)
> to Room. **No production write path produces a fresh `.realm` database for new installs**,
> but several call-sites still **read from Realm** for backward compatibility, and one
> write path still inserts new `Photo` rows there (the Wear audio-received handler).

## Models kept

| Class | Path |
|---|---|
| `Photo` | [`mobile/src/main/java/com/shelbeely/opentransition/data/Photo.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Photo.kt) |
| `Milestone` | [`mobile/src/main/java/com/shelbeely/opentransition/data/Milestone.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Milestone.kt) |
| `AudioAnalysis` | [`mobile/src/main/java/com/shelbeely/opentransition/data/AudioAnalysis.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/AudioAnalysis.kt) |

Each is annotated `@PrimaryKey id: String = UUID.randomUUID().toString()` and includes `toJson()` / `fromJson()` for `.ttbackup` round-trips.

## What still reads them

- `RealmBackupImporter.importFromBackup` — reads the legacy file in [`database/migration/RealmBackupImporter.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/migration/RealmBackupImporter.kt) and copies into Room.
- `RealmToRoomMigration` — in-place migration on devices that still have a Realm file ([`database/migration/RealmToRoomMigration.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/migration/RealmToRoomMigration.kt)).
- `MobileWearableListenerService.handleSyncRequest` — reads `Milestone` to push the latest 100 to the watch ([`wear/MobileWearableListenerService.kt:172-200`](../../../mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt)).
- `MobileWearableListenerService.saveAudioToRealm` — **writes** a new `Photo` row with `type = TYPE_AUDIO` when audio arrives from the watch ([`MobileWearableListenerService.kt:228-249`](../../../mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt)).
- Many UI adapters still read the Realm classes directly — see [`audit-report/07-issues-and-bugs.md`](../../../audit-report/07-issues-and-bugs.md) ISSUE-004 for the canonical list.

## Schema overlap with Room

| Field | `Realm Photo` | `PhotoEntity` |
|---|---|---|
| `id`, `epochDay`, `timestamp`, `filePath`, `type` | ✅ | ✅ identical |

| Field | `Realm Milestone` | `MilestoneEntity` |
|---|---|---|
| `id`, `epochDay`, `timestamp`, `title`, `description` | ✅ | ✅ identical |

| Field | `Realm AudioAnalysis` | `AudioAnalysisEntity` |
|---|---|---|
| Core (`id, photoId, f0*, fN*, f0StdDev, durationSeconds, analysisTimestamp`) | ✅ | ✅ |
| Extended (`pitchConfidenceMean`, `voicedRatio`, `intensity*`, `pitchRange*`, `pitchStability*`, `intonationMovement`, `sessionSummaryText`) | ✅ | ✅ |
| `transcript` | ✅ | ❌ **missing on Room side** |

> The missing `transcript` column is the single column on `AudioAnalysis` that **does not round-trip** through Room. See [`_state/unknowns.md`](../_state/unknowns.md).

## Realm file location

Default path: `Realm.openDefault()` uses `Realm.configurationOf(default)` with class set `setOf(Milestone::class, Photo::class, AudioAnalysis::class)`. The resolver lives in [`util/RealmExtensions.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/RealmExtensions.kt) (`openDefault()` extension).

## When to remove

The Realm dependency cannot be removed until:

1. `MobileWearableListenerService.saveAudioToRealm` writes to Room instead.
2. `handleSyncRequest` reads from Room instead.
3. Every adapter listed in [`audit-report/07-issues-and-bugs.md`](../../../audit-report/07-issues-and-bugs.md) ISSUE-004 is converted.
4. `transcript` column is added to `AudioAnalysisEntity` (or a follow-up migration is shipped that backfills from Realm).
5. The `.realm` import path is converted to a fully read-only ZIP/JSON parser, not opening Realm.

See [`decisions/rewrite-target-stack.md`](../decisions/rewrite-target-stack.md) for ordering.
