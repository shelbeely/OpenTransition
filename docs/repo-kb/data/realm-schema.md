# Realm Schema (legacy, read-only)

> **Status: read-only.** The Realm Kotlin SDK is retained **only** to ingest legacy data — `.ttbackup` archives from TransTracks (the predecessor app) and raw `.realm` files chosen via Settings → "Import Backup". New code must use Room (see [`room-entities.md`](./room-entities.md)). This is consistent with the dependency map and the architecture notes: "Realm Kotlin SDK 2.3.0 retained read-only for importing legacy `.ttbackup` archives" ([`architecture.md`](../architecture.md) §Persistence Layer).

## Location

- Models: [`mobile/src/main/java/com/shelbeely/opentransition/data/`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/) — `Photo.kt`, `Milestone.kt`, `AudioAnalysis.kt`.
- Configuration helper: [`mobile/src/main/java/com/shelbeely/opentransition/util/RealmConfigurations.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/RealmConfigurations.kt).
- Open helper: [`mobile/src/main/java/com/shelbeely/opentransition/util/Realms.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/Realms.kt) (`Realm.openDefault()`).
- Realm objects under `shared/` and `wear/`: **none** (verified by `grep -r 'RealmObject' shared/ wear/`).

## Configuration

`RealmConfigurations.kt` exposes a single `RealmConfiguration.Companion.default` extension that calls `RealmConfiguration.create(setOf(Milestone::class, Photo::class, AudioAnalysis::class))`. There is exactly one Realm schema, used for both the legacy default file and (read-only) for user-supplied `.realm` files copied into `cacheDir` during the Settings-driven import.

## Models

All three classes carry a "BACKWARDS COMPATIBILITY" KDoc explicitly directing new code to the matching Room entity. None declare relations, embedded objects, or indices.

### `Photo` (`Photo.kt`)

| Field | Type | Notes |
|---|---|---|
| `id` | `String` | `@PrimaryKey`. UUID. |
| `epochDay` | `Long` | |
| `timestamp` | `Long` | |
| `filePath` | `String` | |
| `type` | `Int` (`@Photo.Type` IntDef) | `0 = TYPE_FACE`, `1 = TYPE_BODY`, `2 = TYPE_AUDIO`. |

### `Milestone` (`Milestone.kt`)

| Field | Type | Notes |
|---|---|---|
| `id` | `String` | `@PrimaryKey`. UUID. |
| `epochDay` | `Long` | |
| `timestamp` | `Long` | |
| `title` | `String` | |
| `description` | `String` | |

### `AudioAnalysis` (`AudioAnalysis.kt`)

| Field | Type | Notes |
|---|---|---|
| `id` | `String` | `@PrimaryKey`. UUID. |
| `photoId` | `String` | Logical reference to `Photo.id`. |
| `f0Mean`, `f0Min`, `f0Max`, `f0StdDev` | `Float` | Pitch statistics. |
| `f1Mean`, `f2Mean`, `f3Mean`, `f4Mean` | `Float` | Formant means. |
| `pitchConfidenceMean`, `voicedRatio`, `intensityMeanDb`, `intensityMaxDb`, `pitchRangeHz`, `pitchStabilityScore`, `intonationMovement` | `Float` | Extended metrics. |
| `sessionSummaryText` | `String` | |
| `durationSeconds` | `Float` | |
| `analysisTimestamp` | `Long` | |
| `transcript` | `String` | Captured during recording (may be empty). **Not present** on the Room twin `AudioAnalysisEntity` — see [`_state/unknowns.md`](../_state/unknowns.md) "Realm vs. Room field drift". |

## Read-only enforcement (in source)

- `RealmBackupImporter` opens the user-supplied `.realm` file with the default config and only calls `realm.query(...)` — **never** `realm.write(...)`. After copy it `delete()`s the cached file. (`mobile/src/main/java/com/shelbeely/opentransition/database/migration/RealmBackupImporter.kt`.)
- `RealmToRoomMigration` reads from `Realm.openDefault()` and writes only into Room via `DatabaseManager`.
- The `.ttbackup` ingestion path in `MainActivity.processImport` is the **one** place that still writes into the legacy Realm DB (`copyToRealm(...)`). This is documented as a known transitional behaviour in [`features/import.md`](../features/import.md) §A. Once the Realm → Room migration completes, this write path is the next thing to remove.

## Import Flow (link, do not duplicate)

The full ingestion pipeline (intent filters, zip parsing, JSON replay, Realm → Room migration) lives in [`features/import.md`](../features/import.md). This page only documents the schema; do not duplicate the flow here.

## Related Tests

None located. Tracked in [`_state/unknowns.md`](../_state/unknowns.md).

## Evidence

- [`Photo.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Photo.kt), [`Milestone.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Milestone.kt), [`AudioAnalysis.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/AudioAnalysis.kt).
- [`util/RealmConfigurations.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/RealmConfigurations.kt), [`util/Realms.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/Realms.kt).
- [`dependency-map.md`](../dependency-map.md) — Realm marked read-only.
- [`architecture.md`](../architecture.md) §Persistence Layer.
- [`ENCRYPTED_DATABASE.md`](../../../ENCRYPTED_DATABASE.md) — explains why Realm still holds user data today.
