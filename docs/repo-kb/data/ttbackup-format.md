# `.ttbackup` Format

> Derived from import code under
> [`database/migration/`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/migration/)
> and `MainActivity.processIntent` ([`ui/MainActivity.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/MainActivity.kt)).
> This is the structure OpenTransition expects when **importing** a backup. Any
> claims about export are based on `getSettingsAsJson` + per-model `toJson()` and
> are flagged below where the export side is not fully wired up yet.

## Overall shape

A `.ttbackup` is a **ZIP archive** with extension `.ttbackup` and MIME type
`application/ttbackup` (see manifest intent-filters in
[`mobile/src/main/AndroidManifest.xml`](../../../mobile/src/main/AndroidManifest.xml)).

It contains:

```
<root>/
├── settings.json           ← key/value subset; see below
├── milestones.json         ← JSON array of Milestone objects
├── photos.json             ← JSON array of Photo metadata objects
├── audio_analyses.json     ← JSON array of AudioAnalysis objects
├── *.jpg / *.png / *.heic  ← per-photo image files referenced by photos.json
└── *.3gp / *.amr / *.wav   ← per-audio files referenced by photos.json (where type == 2)
```

> The exact filenames in `photos.json` are values from `Photo.FIELD_FILE_NAME` (`File(filePath).name`) — see [`data/Photo.kt:57`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Photo.kt).

## `settings.json`

Written by `SettingsManager.getSettingsAsJson` ([`SettingsManager.kt:232-237`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt)):

| Field | Type | Notes |
|---|---|---|
| `currentAndroidVersion` | int | No-op on import (forward-compat hook) |
| `startDate` | long (epochDay) | Optional |
| `theme` | string | `Theme` enum name |
| `colorVariant` | string | `AppColorVariant` enum name |

Read by `SettingsManager.getSettingsFromJson` ([`SettingsManager.kt:240-275`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt)). **No other settings round-trip** — lock code, decoy code, encryption toggle, Firestore preference are intentionally excluded.

## `milestones.json` — `Milestone.toJson()` shape

Per [`data/Milestone.kt:40-46`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Milestone.kt):

| Field | Type |
|---|---|
| `id` | string (UUID) |
| `epochDay` | long |
| `timestamp` | long |
| `title` | string |
| `description` | string |

## `photos.json` — `Photo.toJson()` shape

Per [`data/Photo.kt:51-64`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Photo.kt):

| Field | Type | Notes |
|---|---|---|
| `id` | string (UUID) | |
| `epochDay` | long | |
| `timestamp` | long | |
| `fileName` | string | bare filename, **not absolute path** — resolved at import via `FileUtil.getMediaFile(fileName)` |
| `type` | int | `0` face, `1` body, `2` audio |

> On import, a photo whose referenced file is missing or zero-bytes is skipped, not the whole import ([`Photo.kt:122-125`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Photo.kt)).

## `audio_analyses.json` — `AudioAnalysis.toJson()` shape

Per [`data/AudioAnalysis.kt:74-103, 106-127`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/AudioAnalysis.kt):

Core (round-trips with `AudioAnalysisEntity`):
`id`, `photoId`, `f0Mean`, `f0Min`, `f0Max`, `f1Mean`..`f4Mean`, `f0StdDev`,
`durationSeconds`, `analysisTimestamp`.

Extended (added in schema v2): `pitchConfidenceMean`, `voicedRatio`,
`intensityMeanDb`, `intensityMaxDb`, `pitchRangeHz`, `pitchStabilityScore`,
`intonationMovement`, `sessionSummaryText`.

Realm-only (does **not** round-trip to Room): `transcript`.

## Import entry points

| Path | Source |
|---|---|
| `MainActivity.processIntent` (intent VIEW/SEND) | [`MainActivity.kt:308-391`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/MainActivity.kt) |
| Settings → Import button | [`SettingsFragment.kt:866-905`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt) |
| Direct legacy `.realm` import | [`RealmBackupImporter.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/migration/RealmBackupImporter.kt) (different path — handles a raw Realm file, **not** a `.ttbackup` ZIP) |

## Export status

- `Milestone.toJson()`, `Photo.toJson()`, `AudioAnalysis.toJson()` exist and produce the canonical shape above.
- `SettingsManager.getSettingsAsJson()` covers the 4 settings fields.
- Wiring a single "Export Backup" UI action that bundles all four JSON files + the binary media into a ZIP **is not yet shipped** (TODO at [`SettingsFragment.kt:776`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt) for the decoy-vault export specifically).

## Invariants (rewrite must preserve)

- `.ttbackup` is a **ZIP**, not a TAR or proprietary container.
- All JSON arrays at root, each named exactly as above.
- Photo files use their **basename only** inside the archive — never an absolute path.
- Importing must be idempotent on `id`: a `.ttbackup` re-applied twice must not duplicate rows. The current `@Insert(onConflict = REPLACE)` DAOs preserve this ([`PhotoDao.kt:27`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/dao/PhotoDao.kt) and siblings).

## Open questions

See [`_state/unknowns.md`](../_state/unknowns.md):

- Whether the archive currently ships a top-level folder or files at the root of the ZIP.
- Whether OpenTransition produces strictly the same JSON shape as upstream TransTracks (this requires upstream comparison — see [`decisions/diff-vs-transtracks.md`](../decisions/diff-vs-transtracks.md)).
