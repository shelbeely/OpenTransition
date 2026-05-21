# Room Entities

> Sourced from `@Entity` classes under [`mobile/src/main/java/com/shelbeely/opentransition/database/room/entities/`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/entities/) and the Room-emitted schema files in [`mobile/schemas/com.shelbeely.opentransition.database.AppDatabase/`](../../../mobile/schemas/com.shelbeely.opentransition.database.AppDatabase/).
> Current `AppDatabase.version = 3` ([`AppDatabase.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/AppDatabase.kt)).

## Tables

### `milestones` — [`MilestoneEntity`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/entities/MilestoneEntity.kt)

| Column | Type | Notes |
|---|---|---|
| `id` (PK) | TEXT NOT NULL | UUID string |
| `epochDay` | INTEGER NOT NULL | `LocalDate.toEpochDay()` (UTC) |
| `timestamp` | INTEGER NOT NULL | `System.currentTimeMillis()` |
| `title` | TEXT NOT NULL | |
| `description` | TEXT NOT NULL | empty string if none |

Indices: none. Foreign keys: none.

### `photos` — [`PhotoEntity`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/entities/PhotoEntity.kt)

| Column | Type | Notes |
|---|---|---|
| `id` (PK) | TEXT NOT NULL | UUID string |
| `epochDay` | INTEGER NOT NULL | UTC date |
| `timestamp` | INTEGER NOT NULL | epoch-ms |
| `filePath` | TEXT NOT NULL | absolute path inside `Context.filesDir` |
| `type` | INTEGER NOT NULL | `0 = face`, `1 = body`, `2 = audio` ([`Photo.kt:66-69`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Photo.kt)) |

Indices: none. Foreign keys: none. **No FK from `audio_analysis.photoId` to `photos.id`** — the linkage is by-value only.

### `audio_analysis` — [`AudioAnalysisEntity`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/entities/AudioAnalysisEntity.kt)

| Column | Type | Source |
|---|---|---|
| `id` (PK) | TEXT NOT NULL | UUID |
| `photoId` | TEXT NOT NULL | UUID of associated `photos` row (no FK) |
| `f0Mean` | REAL NOT NULL | Pitch mean (Hz) |
| `f0Min` | REAL NOT NULL | Pitch min (Hz) |
| `f0Max` | REAL NOT NULL | Pitch max (Hz) |
| `f1Mean`..`f4Mean` | REAL NOT NULL | **Persisted as 0f today** — see [`features/audio-tracking.md`](../features/audio-tracking.md) |
| `f0StdDev` | REAL NOT NULL | Pitch standard deviation |
| `durationSeconds` | REAL NOT NULL | |
| `analysisTimestamp` | INTEGER NOT NULL | |
| `pitchConfidenceMean` | REAL NOT NULL DEFAULT 0 | added in schema v2 |
| `voicedRatio` | REAL NOT NULL DEFAULT 0 | added in schema v2 |
| `intensityMeanDb` | REAL NOT NULL DEFAULT 0 | added in schema v2 |
| `intensityMaxDb` | REAL NOT NULL DEFAULT 0 | added in schema v2 |
| `pitchRangeHz` | REAL NOT NULL DEFAULT 0 | added in schema v2 |
| `pitchStabilityScore` | REAL NOT NULL DEFAULT 0 | added in schema v2 |
| `intonationMovement` | REAL NOT NULL DEFAULT 0 | added in schema v2 |
| `sessionSummaryText` | TEXT NOT NULL DEFAULT '' | added in schema v2 |

Indices: none. Foreign keys: none.

> **Missing column:** the Realm `AudioAnalysis` has a `transcript` field; the Room
> entity does **not**. Transcripts are written to Realm only ([`data/AudioAnalysis.kt:71-72`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/AudioAnalysis.kt)). This is a known gap — see [`_state/unknowns.md`](../_state/unknowns.md).

### `voice_goals` — [`VoiceGoalEntity`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/entities/VoiceGoalEntity.kt)

| Column | Type | Notes |
|---|---|---|
| `id` (PK) | TEXT NOT NULL | UUID |
| `name` | TEXT NOT NULL | User-chosen label |
| `metricKey` | TEXT NOT NULL | One of `f0Mean`, `pitchRangeHz`, `voicedRatio`, `pitchStabilityScore`, `intonationMovement` (mirrors [`VoiceMetric.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/VoiceMetric.kt)) |
| `targetMin` | REAL NOT NULL | Lower bound |
| `targetMax` | REAL NOT NULL | Upper bound |
| `createdAt` | INTEGER NOT NULL | epoch-ms |

Indices: none. Foreign keys: none. **Added in schema v3.**

## Migrations

| From → To | Class | Behaviour | Path |
|---|---|---|---|
| 1 → 2 | `MIGRATION_1_2` | Adds the extended-metric columns to `audio_analysis` with `DEFAULT 0` (or `''` for `sessionSummaryText`) | [`AppDatabase.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/AppDatabase.kt) |
| 2 → 3 | `MIGRATION_2_3` | Creates the `voice_goals` table | [`AppDatabase.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/AppDatabase.kt) |

Schemas committed: `1.json`, `3.json`. **`2.json` is not committed** — Room only emits a schema file when a release-versioned database is shipped; v2 is treated as transient. See [`_state/unknowns.md`](../_state/unknowns.md).

## Where DAOs live

- [`PhotoDao`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/dao/PhotoDao.kt)
- [`MilestoneDao`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/dao/MilestoneDao.kt)
- [`AudioAnalysisDao`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/dao/AudioAnalysisDao.kt)
- [`VoiceGoalDao`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/dao/VoiceGoalDao.kt)

## Vault routing

`AppDatabase.getInstance(context, isDecoy)` returns one of two singletons backed by
`opentransition.db` (real) or `opentransition_decoy.db` (decoy). All access **must**
go through `DatabaseManager.getDatabase(context)` so the vault swap works — see
[`features/decoy-vault.md`](../features/decoy-vault.md).

## Constraints relied on elsewhere

- `audio_analysis.photoId` is queried by [`AudioAnalysisDao.observeByPhotoId`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/dao/AudioAnalysisDao.kt). The lack of an index there may matter once libraries grow large.
- Default ordering across all DAOs is `epochDay DESC, timestamp DESC` (photos/milestones) or `analysisTimestamp DESC` / `createdAt DESC`.
