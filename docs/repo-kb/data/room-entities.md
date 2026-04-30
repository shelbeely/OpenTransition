# Room Entities, DAOs & Migrations

The mobile app's primary local store is a single Room database (`AppDatabase`) at schema **version 3**, optionally wrapped with SQLCipher. This page enumerates every entity, every DAO query, every migration, and the SQLCipher integration point — grounded in source.

## Location

- Database class: [`mobile/src/main/java/com/shelbeely/opentransition/database/AppDatabase.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/AppDatabase.kt)
- Entities: [`mobile/src/main/java/com/shelbeely/opentransition/database/room/entities/`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/entities/)
- DAOs: [`mobile/src/main/java/com/shelbeely/opentransition/database/room/dao/`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/dao/)
- Exported schemas: [`mobile/schemas/com.shelbeely.opentransition.database.AppDatabase/`](../../../mobile/schemas/com.shelbeely.opentransition.database.AppDatabase/) — `1.json`, `3.json` are present (no `2.json` was exported).

## Purpose

Persists the four user-visible domains of the app: **milestones**, **photos** (and audio recording metadata, classified as type `2`), **per-recording voice analysis**, and **user-defined voice goals**. Single-process Room database with `Flow`-based observability and coroutine-based mutators. Optional at-rest encryption via SQLCipher when the user toggles it on.

## Database

`AppDatabase` (`AppDatabase.kt:30–44`):

| Aspect | Value |
|---|---|
| `version` | `3` |
| `exportSchema` | `true` (KSP arg `room.schemaLocation` → `mobile/schemas/`, see [`build-and-release.md`](../build-and-release.md)) |
| `entities` | `MilestoneEntity`, `PhotoEntity`, `AudioAnalysisEntity`, `VoiceGoalEntity` |
| Files (real / decoy) | `opentransition.db` / `opentransition_decoy.db` |
| Singleton | Two `@Volatile` slots (`INSTANCE`, `DECOY_INSTANCE`); `getInstance(context, isDecoy)` is the entry point |
| Type converters | **None.** No `@TypeConverter` / `@TypeConverters` declared anywhere under `mobile/src/main/`. All columns use Room-native primitive types. |
| Foreign keys | **None.** No `@Entity(foreignKeys = …)` declared. The `AudioAnalysisEntity.photoId` ↔ `PhotoEntity.id` link is a logical reference only, not enforced by SQLite. |
| Indices | **None.** No `@Index` declared. |
| Callbacks | An empty `RoomDatabase.Callback.onCreate` is registered (see `AppDatabase.kt:138–142`). |

> Vault selection (real vs. decoy) is owned by `DatabaseManager` (see [`features/app-lock.md`](../features/app-lock.md) §"Vault switching mechanics"). `AppDatabase.getInstance` only knows the boolean; it does not consult `SettingsManager.getActiveVault()` itself.

## Entities

### `MilestoneEntity` — table `milestones`

[`MilestoneEntity.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/entities/MilestoneEntity.kt)

| Column | Type | Notes |
|---|---|---|
| `id` | `String` | `@PrimaryKey`. UUID assigned by domain code. |
| `epochDay` | `Long` | Day bucket the milestone is filed under. |
| `timestamp` | `Long` | Unix epoch ms, used as secondary sort key. |
| `title` | `String` | |
| `description` | `String` | |

### `PhotoEntity` — table `photos`

[`PhotoEntity.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/entities/PhotoEntity.kt)

| Column | Type | Notes |
|---|---|---|
| `id` | `String` | `@PrimaryKey`. |
| `epochDay` | `Long` | |
| `timestamp` | `Long` | |
| `filePath` | `String` | Absolute path on app-private storage. |
| `type` | `Int` | `0 = TYPE_FACE`, `1 = TYPE_BODY`, `2 = TYPE_AUDIO` (constants live on the legacy [`Photo`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Photo.kt) Realm class). |

### `AudioAnalysisEntity` — table `audio_analysis`

[`AudioAnalysisEntity.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/entities/AudioAnalysisEntity.kt)

| Column | Type | Notes |
|---|---|---|
| `id` | `String` | `@PrimaryKey`. |
| `photoId` | `String` | Logical FK to `PhotoEntity.id` (not enforced). |
| `f0Mean`/`f0Min`/`f0Max`/`f0StdDev` | `Float` | Pitch (Hz) statistics. |
| `f1Mean`…`f4Mean` | `Float` | Formant means (Hz). |
| `durationSeconds` | `Float` | |
| `analysisTimestamp` | `Long` | Unix epoch ms. |
| `pitchConfidenceMean`, `voicedRatio`, `intensityMeanDb`, `intensityMaxDb`, `pitchRangeHz`, `pitchStabilityScore`, `intonationMovement` | `Float` (default `0f`) | Added in **schema v2** (see migration below). |
| `sessionSummaryText` | `String` (default `""`) | Added in **schema v2**. |

> The legacy Realm twin [`AudioAnalysis`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/AudioAnalysis.kt) carries an additional `transcript: String` field that is **not** present in the Room entity. Filed under [`_state/unknowns.md`](../_state/unknowns.md) (see "Realm vs. Room field drift" below).

### `VoiceGoalEntity` — table `voice_goals`

[`VoiceGoalEntity.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/entities/VoiceGoalEntity.kt)

| Column | Type | Notes |
|---|---|---|
| `id` | `String` | `@PrimaryKey`. UUID. |
| `name` | `String` | Free-text user label. |
| `metricKey` | `String` | One of: `f0Mean`, `pitchRangeHz`, `voicedRatio`, `pitchStabilityScore`, `intonationMovement` (constants on the entity's `companion object`). Mirrors the `VoiceMetric` enum. |
| `targetMin` / `targetMax` | `Float` | Target band (same unit as the metric). |
| `createdAt` | `Long` | Unix epoch ms. |

## DAOs

All DAO suspend functions run off-main-thread under Room's executor. All `Flow`-returning queries emit on data changes.

### `MilestoneDao` — [`MilestoneDao.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/dao/MilestoneDao.kt)

| Purpose | Member |
|---|---|
| Observe full list (ordered by day desc, then timestamp desc) | `getAllMilestones(): Flow<List<MilestoneEntity>>` |
| Read by id | `suspend getMilestoneById(id)` |
| Upsert one / many | `suspend insertMilestone(...)`, `suspend insertMilestones(list)` (`OnConflictStrategy.REPLACE`) |
| Mutate | `suspend updateMilestone(...)`, `suspend deleteMilestone(...)`, `suspend deleteMilestoneById(id)` |
| Wipe | `suspend deleteAllMilestones()` |

### `PhotoDao` — [`PhotoDao.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/dao/PhotoDao.kt)

| Purpose | Member |
|---|---|
| Observe all photos (day desc, timestamp desc) | `getAllPhotos(): Flow<List<PhotoEntity>>` |
| Observe a single type bucket (face / body / audio) | `getPhotosByType(type): Flow<List<PhotoEntity>>` |
| Read by id | `suspend getPhotoById(id)` |
| Upsert one / many | `suspend insertPhoto(...)`, `suspend insertPhotos(list)` |
| Mutate | `suspend updatePhoto(...)`, `suspend deletePhoto(...)`, `suspend deletePhotoById(id)` |
| Wipe | `suspend deleteAllPhotos()` |

### `AudioAnalysisDao` — [`AudioAnalysisDao.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/dao/AudioAnalysisDao.kt)

| Purpose | Member |
|---|---|
| Observe full history (newest first) | `getAllAudioAnalyses(): Flow<List<AudioAnalysisEntity>>` |
| One-shot full read | `suspend getAllAudioAnalysesList(): List<AudioAnalysisEntity>` |
| Read by id | `suspend getAudioAnalysisById(id)` |
| Read by photoId | `suspend getAudioAnalysisByPhotoId(photoId)` |
| Observe latest analysis for a recording | `observeByPhotoId(photoId): Flow<AudioAnalysisEntity?>` (`LIMIT 1`) |
| Upsert one / many | `suspend insertAudioAnalysis(...)`, `suspend insertAudioAnalyses(list)` |
| Mutate | `suspend updateAudioAnalysis(...)`, `suspend deleteAudioAnalysis(...)`, `suspend deleteAudioAnalysisById(id)` |
| Wipe | `suspend deleteAllAudioAnalyses()` |

### `VoiceGoalDao` — [`VoiceGoalDao.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/dao/VoiceGoalDao.kt)

| Purpose | Member |
|---|---|
| Observe all goals (newest first) | `getAllGoals(): Flow<List<VoiceGoalEntity>>` |
| One-shot full read | `suspend getAllGoalsList()` |
| Read by id | `suspend getGoalById(id)` |
| Filter by metric | `suspend getGoalsForMetric(metricKey): List<VoiceGoalEntity>` |
| Upsert | `suspend insertGoal(...)` (`REPLACE`) |
| Mutate | `suspend updateGoal(...)`, `suspend deleteGoal(...)`, `suspend deleteGoalById(id)` |
| Wipe | `suspend deleteAllGoals()` |

## Migrations

Both migrations are registered via `addMigrations(MIGRATION_1_2, MIGRATION_2_3)` in `AppDatabase.buildDatabase` (`AppDatabase.kt:137`). Both are pure DDL — no row-level data movement.

### `MIGRATION_1_2` (v1 → v2) — `AppDatabase.kt:57–68`

Adds the eight extended voice-analysis columns to `audio_analysis`. Every new column has `NOT NULL DEFAULT 0` (or `DEFAULT ''` for `sessionSummaryText`) so existing rows remain valid:

- `pitchConfidenceMean`, `voicedRatio`, `intensityMeanDb`, `intensityMaxDb`, `pitchRangeHz`, `pitchStabilityScore`, `intonationMovement` — all `REAL NOT NULL DEFAULT 0`
- `sessionSummaryText` — `TEXT NOT NULL DEFAULT ''`

### `MIGRATION_2_3` (v2 → v3) — `AppDatabase.kt:74–89`

Creates the `voice_goals` table (DDL inline). No existing tables are modified.

> No `2.json` schema is exported under `mobile/schemas/`; only `1.json` and `3.json` are checked in. This is consistent with skipping the intermediate schema, but worth noting if a future contributor wants to add a `MIGRATION_2_3` consistency test.

## SQLCipher Integration

`AppDatabase.buildDatabase` (`AppDatabase.kt:119–144`) is the **only** SQLCipher integration point in the codebase:

1. Reads `SettingsManager.isEncryptedDatabaseEnabled()`.
2. If `true`, fetches a 256-bit passphrase via `KeystoreManager.getOrCreateDatabaseKey(context, isDecoy)` (the passphrase is per-vault; see [`features/app-lock.md`](../features/app-lock.md) §"Vault switching mechanics" for how `KeystoreManager` mints and stores it in `EncryptedSharedPreferences("opentransition_db_keys")`).
3. Wraps the passphrase in `net.sqlcipher.database.SupportFactory(passphrase)` and installs it via `Room.databaseBuilder(...).openHelperFactory(factory)`.
4. If `false`, no factory is installed and Room uses the platform SQLite stack.

The encryption toggle is currently hidden in user-facing Settings (`ENCRYPTED_DATABASE.md` "Status" notice). Until the Realm → Room migration completes, switching it on encrypts a database that holds little real user data.

## Schema Snapshots

| Version | File | Notes |
|---|---|---|
| `1` | [`mobile/schemas/.../1.json`](../../../mobile/schemas/com.shelbeely.opentransition.database.AppDatabase/1.json) | Initial schema (pre-extended voice metrics, no `voice_goals`). |
| `3` | [`mobile/schemas/.../3.json`](../../../mobile/schemas/com.shelbeely.opentransition.database.AppDatabase/3.json) | Current schema, including the v2 column additions and the v3 `voice_goals` table. |

## Callers (high level)

- **Photo capture / gallery** — see [`features/photo-capture.md`](../features/photo-capture.md).
- **Audio analysis & voice progress** — `RecordAudioFragment`, `VoiceProgressFragment`, `VoiceSessionDetailFragment` (see [`components/index.md`](../components/index.md)).
- **Backup ingestion** — `RealmBackupImporter` writes through `DatabaseManager` into Room (see [`features/import.md`](../features/import.md)).
- **In-place migration from legacy Realm** — `RealmToRoomMigration` (see [`features/import.md`](../features/import.md) §A vs. §B).

## Related Tests

None located under `mobile/src/test/` or `mobile/src/androidTest/` for these DAOs/entities at the time of writing. Tracked in [`_state/unknowns.md`](../_state/unknowns.md).

## Evidence

- [`AppDatabase.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/AppDatabase.kt) — class header (entities, version, `exportSchema`), migrations, SQLCipher wiring.
- Entity files listed above — column definitions and primary keys.
- DAO files listed above — query bodies.
- [`mobile/schemas/com.shelbeely.opentransition.database.AppDatabase/`](../../../mobile/schemas/com.shelbeely.opentransition.database.AppDatabase/) — exported schema JSON.
- [`ENCRYPTED_DATABASE.md`](../../../ENCRYPTED_DATABASE.md) — design intent for the SQLCipher path.
