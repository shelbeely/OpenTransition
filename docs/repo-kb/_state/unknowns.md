# Unknowns

Open items the source did not fully answer during the pass-1 KB build. These are not bugs — they are documentation gaps.

## SDK targets disagree between docs and source

`mobile/build.gradle` declares `minSdkVersion 26` and `targetSdkVersion 35`, but `.github/copilot-instructions.md` describes `:mobile` as `minSdk 21` and `targetSdk 36`. The KB ([`architecture.md`](../architecture.md)) records the source-derived values and notes the disagreement; the prose docs need a follow-up reconciliation.

Evidence:

- `mobile/build.gradle` `defaultConfig`
- `.github/copilot-instructions.md` "Repository layout" table

Next step: a maintainer decides which values are intended and updates whichever is wrong (the code, the prose, or both). Mirror page: [`questions/index.md`](../questions/index.md).

## Pass-2 scope is documented but not yet executed

Per-feature, per-entity, and per-file pages are intentionally deferred (see [`progress.md`](./progress.md)). They are not "unknown" in the sense of contradicting the source — they are simply not yet documented.

## CI secret name list may be incomplete

[`configuration.md`](../configuration.md) lists the well-known secrets (`KEYSTORE_64`, `STORE_PASS`, `KEY_ALIAS`, `KEY_PASS`, plus the secrets read by `prepare-secrets.sh` / `prepare-google-services.sh`). The pass-1 build did not exhaustively read every `env:` block in every workflow; future passes should diff `.github/ci-scripts/*.sh` against the workflow `env:` declarations to confirm completeness.

## Room schema v2 file is not committed

`mobile/schemas/com.shelbeely.opentransition.database.AppDatabase/` contains `1.json` and `3.json` but **no `2.json`**. This is consistent with Room only emitting schemas for shipped versions, but the absence is not documented in-repo. Filed for completeness — see [`data/room-entities.md`](../data/room-entities.md). **Question:** was v2 ever shipped to users, or was it a development-only step squashed into v3 before release?

## `transcript` does not round-trip through Room

`Realm AudioAnalysis.transcript` is written by [`RecordAudioFragment.kt:267`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/recordaudio/RecordAudioFragment.kt) but `AudioAnalysisEntity` has no `transcript` column. **Question:** is the intent to add the column (Room migration 3 → 4) or to drop transcript persistence entirely once Realm is removed? See [`data/realm-models.md`](../data/realm-models.md) and [`decisions/rewrite-target-stack.md`](../decisions/rewrite-target-stack.md) Phase C.

## `PATH_MILESTONE_UPDATE` is listened-for but never sent

The wear-side service and `MainActivity` both register handlers for `PATH_MILESTONE_UPDATE`, but no `:mobile` source ever calls `MessageClient.sendMessage(..., PATH_MILESTONE_UPDATE, ...)`. **Question:** is the intent to add a producer (so a single milestone edit can fast-path to wear without a full `DataItem` re-broadcast), or to remove the dead path? See [`apis/wearable-data-layer.md`](../apis/wearable-data-layer.md) functional/dead column.

## `DATA_PATH_SETTINGS` / `DATA_PATH_AUDIO` / `PATH_SYNC_MILESTONES` / `PATH_AUDIO_START` / `PATH_AUDIO_STOP` are dead

Declared in `WearableConstants` but never produced. Same question shape: ship a producer, or remove the constant?

## Upstream diff completeness

The upstream diff in [`decisions/diff-vs-transtracks.md`](../decisions/diff-vs-transtracks.md) compared:

- `README.md`, root `build.gradle`, `app/build.gradle`, `settings.gradle`
- the `app/src/main/java/com/drspaceboo/transtracks/data/` directory listing only

It did **not** walk the upstream `ui/` tree or the upstream Manifest. Two specific follow-ups are pending:

- Does upstream have its own (different) "decoy" or "hidden mode" pattern that OpenTransition has replaced?
- Are upstream's RxJava `Controller` types preserved name-for-name in OpenTransition, or has any been replaced by a Compose+Domain pair?

## `.ttbackup` archive top-level layout

The schema in [`data/ttbackup-format.md`](../data/ttbackup-format.md) lists files **at the root of the ZIP**, but the import code accepts the archive via an Intent stream and we did not verify whether upstream actually wrote everything at the root or under a top-level folder. A round-trip test against a real archived-upstream `.ttbackup` is needed before locking the rewrite contract.
