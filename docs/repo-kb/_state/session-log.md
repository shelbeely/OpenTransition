# Session Log

## 2026-04-29 21:46 — Pass 1 bootstrap

### Goal

Move the `repo-knowledge-base` skill to the location specified by the [GitHub Agent Skills docs](https://docs.github.com/en/copilot/concepts/agents/about-agent-skills), then apply the skill to seed `docs/repo-kb/`.

### Commands Run

| Command | Result |
|---|---|
| `git mv repo-knowledge-base-SKILL.md .github/skills/repo-knowledge-base/SKILL.md` | OK |
| `find . -path ./node_modules -prune -o -name SKILL.md -print` | OK — confirmed 50+ existing skills under `.github/skills/` |
| `cat settings.gradle build.gradle gradle.properties` | OK — captured Kotlin/AGP/Nav/Realm versions and version-constant scheme |
| `head` of each `.github/workflows/*.yml` | OK — captured triggers for all 9 workflows |
| `grep '^\s+(implementation\|api\|kapt\|ksp\|...)' mobile/build.gradle wear/build.gradle shared/build.gradle` | OK — captured key dependencies |

No build / test / lint commands were run in this session — pass 1 only adds documentation (no Kotlin/Gradle code changes), so the existing CI signal on this branch is unchanged.

### Files Read

- `README.md`, `ARCHITECTURE.md`, `MONOREPO.md` (root)
- `build.gradle`, `settings.gradle`, `gradle.properties`, `mobile/build.gradle`, `wear/build.gradle`, `shared/build.gradle`
- `.github/copilot-instructions.md`
- All 9 `.github/workflows/*.yml`
- The skill itself (`.github/skills/repo-knowledge-base/SKILL.md`) for templates

### Files Created or Updated

Created:

- `.github/skills/repo-knowledge-base/SKILL.md` (moved from repo root)
- `docs/repo-kb/index.md`, `repo-map.md`, `architecture.md`, `quickstart-for-agents.md`, `build-and-release.md`, `testing.md`, `configuration.md`, `security-and-risk.md`, `maintenance-guide.md`, `glossary.md`, `dependency-map.md`, `data-flow.md`
- `docs/repo-kb/{features,components,apis,data,workflows,decisions,questions,files}/index.md`
- `docs/repo-kb/_state/{progress,coverage,session-log,unknowns,command-log}.md`
- `.github/instructions/repo-kb.instructions.md`
- `AGENTS.md`

Updated:

- `.github/copilot-instructions.md` (KB pointer appended; existing rules preserved)

Removed:

- `repo-knowledge-base-SKILL.md` (repo root) — moved, not deleted

### Key Findings

- `mobile/build.gradle` actually sets `minSdkVersion 26` and `targetSdkVersion 35`, while `.github/copilot-instructions.md` describes the mobile module as `minSdk 21 / targetSdk 36`. Logged in [`unknowns.md`](./unknowns.md) and [`questions/index.md`](../questions/index.md). KB takes the source values as authoritative per the skill's "source is final authority" rule.
- App version constants are centralised in root `build.gradle` so `:mobile` and `:wear` cannot drift — important context for any future versioning change.
- The `boycott-check.yml` workflow + `.github/scripts/boycott_check.py` enforces a dependency boycott list — agents adding deps must keep this passing.

### Problems

None blocking.

### Next Recommended Action

Start pass 2: `features/photo-capture.md` (the CameraX + ML Kit pipeline is the most code-heavy area unique to this app).

---

## 2026-04-30 04:23 — Refresh + pass 3 start

### Goal

Re-apply the `repo-knowledge-base` skill: refresh pass-1 inventory against the current source, then advance one deferred item using the contract template from §"Eighth Pass: APIs, Routes, and Data Contracts".

### Commands Run

| Command | Result |
|---|---|
| `find shared/src wear/src mobile/.../wear -type f` | Discovered 3 source files in `:shared` (not 2), 4 in `wear/.../wear/`, 1 in `mobile/.../wear/` |
| `view shared/.../WearableConstants.kt`, `models/MilestoneData.kt`, `util/WearableHelper.kt` | Captured the full constant set and helper API |
| `view wear/.../WearableListenerService.kt`, `wear/src/main/AndroidManifest.xml` | Captured wear-side dispatch and intent-filter / permissions |
| `grep -E 'PATH_\|DATA_PATH_' mobile/.../MobileWearableListenerService.kt` | Captured the mobile-side dispatch table |

No build / test / lint commands were run — pass 3 only added documentation.

### Files Read

- All three `:shared` Kotlin sources + `WearableHelperTest.kt`
- `wear/src/main/java/com/shelbeely/opentransition/wear/WearableListenerService.kt`
- `wear/src/main/AndroidManifest.xml`
- `mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt`
- `.github/skills/repo-knowledge-base/SKILL.md` §"Eighth Pass: APIs, Routes, and Data Contracts" (template)

### Files Created or Updated

Created:

- `docs/repo-kb/apis/wearable-data-layer.md` — full Mobile↔Wear contract page following the skill's per-contract template (Location / Purpose / Inputs / Outputs / Validation / Auth-Security / Callers / Side Effects / Related Tests / Evidence).

Updated:

- `docs/repo-kb/repo-map.md` — corrected `:wear` and `:shared` source layouts (added `AudioRecordActivity`, `CameraControlActivity`, `theme/WearTheme.kt`, `util/WearableHelper.kt` and its test, noted mobile-side `MobileWearableListenerService`).
- `docs/repo-kb/architecture.md` — pointed module-boundaries section at the new contract page; added `WearableHelper` to the `:shared` summary.
- `docs/repo-kb/data-flow.md` — pointed the mobile↔wear flow at the new contract page and `WearableHelper`.
- `docs/repo-kb/apis/index.md` — promoted the Mobile↔Wear contract from "deferred" to ✅ documented; added a Documented? column.
- `docs/repo-kb/_state/coverage.md` — APIs row updated; Source-entrypoints note acknowledges the two listener services.
- `docs/repo-kb/_state/progress.md` — split deferred work into pass 2 (features) and pass 3 (contracts); ticked the Wearable contract.

### Key Findings

- `:shared` has **three** Kotlin sources, not two — pass 1 missed `util/WearableHelper.kt` (with JVM unit test). Now indexed.
- `:wear` has **four** Kotlin source files (`MainActivity`, `AudioRecordActivity`, `CameraControlActivity`, `WearableListenerService`) plus a Compose theme — pass 1 listed only two.
- `:mobile` has its own listener service at `mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt` mirroring the wear-side dispatch.
- The wire format includes **far more paths than pass 1 implied** — full camera-control and audio-streaming paths exist.
- The `parseMilestones` helper swallows all deserialization exceptions and returns `emptyList()` — captured as a Validation note for any future schema change.

### Problems

- I drifted from the skill template on first draft of `wearable-data-layer.md` (used my own headings) and only fixed it after the user prompted me to re-read the skill. Recorded as a process lesson: **always re-open `SKILL.md` at the start of any session that touches the KB**.

### Next Recommended Action

Pick the next pass-2 / pass-3 item from `progress.md`. Lowest-risk, highest-value next: `data/room-entities.md` — Room schemas are already exported under `mobile/schemas/`, which gives a strict source of truth.

---

## 2026-05-21 05:38 — Phases 3–6 (data, contracts, upstream, rewrite)

### Goal

Promote the KB from "feature pages exist" to "evidence-based per-feature spec
that could drive a clean rewrite". Specifically: produce the data-layer pages,
extend the wearable contract with a functional/dead column, add a Firebase
contract page, do a pinned-SHA diff against upstream TransTracks, and write a
rewrite proposal that rests on the documented evidence.

### Commands Run

| Command | Result |
|---|---|
| `grep` for `PATH_*` / `DATA_PATH_*` usages in both listener services + `WearableHelper` | OK — produced the functional/dead matrix |
| `github-mcp-server-search_repositories` `TransTracks-Android` | Confirmed `TransTracks/TransTracks-Android` (archived) is the real upstream |
| `github-mcp-server-list_commits` | Pinned upstream to SHA `f8560a1aa643e06fa9cfb9dd7c8b4bcbf802f5c9` (2026-04-11, archival commit) |
| `github-mcp-server-get_file_contents` for upstream `README.md`, `build.gradle`, `app/build.gradle`, `settings.gradle`, `app/src/main/java/com/drspaceboo/transtracks/data/` | Captured upstream toolchain and `data/` listing |
| `grep` for Firebase usages, `AuthUI.IdpConfig.*`, Firestore `.collection(...)`, `AnalyticsUtil` events | Captured every Firebase entrypoint |
| `view` of `AudioAnalysisUtil.kt`, `PitchTracker.kt` | Verified DSP "computed vs stubbed" claim — formants explicitly zeroed per inline comment |

No build / test / lint commands were run — only docs changed.

### Files Created

- `docs/repo-kb/data/room-entities.md`
- `docs/repo-kb/data/realm-models.md`
- `docs/repo-kb/data/shared-prefs-keys.md`
- `docs/repo-kb/data/file-layout.md`
- `docs/repo-kb/data/ttbackup-format.md`
- `docs/repo-kb/apis/firebase-contracts.md`
- `docs/repo-kb/decisions/diff-vs-transtracks.md`
- `docs/repo-kb/decisions/rewrite-target-stack.md`

### Files Updated

- `docs/repo-kb/apis/wearable-data-layer.md` — added a "Functional today?" column to both the Message-paths and DataItem-paths tables, marking `PATH_AUDIO_START` / `PATH_AUDIO_STOP` / `PATH_SYNC_MILESTONES` / `DATA_PATH_AUDIO` as dead and `DATA_PATH_SETTINGS` / `PATH_TRIGGER_PHOTO` as partial, all with file/line citations.
- `docs/repo-kb/features/index.md` — replaced the pass-1 placeholder with a real cross-link table to the 11 feature pages that now exist.
- `docs/repo-kb/data/index.md` — promoted to "Complete"; cross-linked the 5 per-entity pages.
- `docs/repo-kb/apis/index.md` — promoted Firebase, Room, `.ttbackup`, and Realm to ✅.
- `docs/repo-kb/decisions/index.md` — listed the two new ADR-style pages.
- `docs/repo-kb/_state/coverage.md` — Features → Complete; APIs → Complete; Data → Complete; Decisions remains Partial pending numbered ADRs.
- `docs/repo-kb/_state/unknowns.md` — added six concrete questions (schema v2, transcript column, dead wear paths, upstream diff completeness, archive root layout).

### Key Findings

- **Audio DSP is honestly "real except formants"** — `PitchTracker` is a real nACF implementation; `f1Mean..f4Mean` are explicitly persisted as `0f` and the inline comment ([`AudioAnalysisUtil.kt:27-29`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/AudioAnalysisUtil.kt)) admits LPC is not implemented. This is the documented honest state — no guessing was needed.
- **`transcript` is a one-way column** — written to Realm only, no Room column. Going to need a migration when Realm is removed.
- **Three dead wire paths in the Wear contract.** Removing them is a Phase F cleanup item per the rewrite plan.
- **Upstream is archived** at SHA `f8560a1a` (2026-04-11). All future upstream diffs can pin to this SHA; the archived repo will not move.
- **Upstream is single-module** (`:app` only). The `:mobile`/`:wear`/`:shared` split is entirely an OpenTransition addition.
- **Firestore collection naming is `{uid}/settings`, not `users/{uid}`.** Already upstream-inherited; flagged in the Firebase contract page.

### Problems

- I initially tried to create feature pages that already existed (they were authored in an earlier session). The right reaction was to verify content first; I did, and they're already evidence-based and accurate. No rework needed there.

### Next Recommended Action

- Run the rewrite-target-stack plan past the maintainer; in particular, the **migration order** in Phase B–F is opinionated about what blocks what.
- Resolve the six items in `_state/unknowns.md` before locking the rewrite order, especially the `transcript` decision (add column vs drop persistence).
- Optional: walk upstream's `ui/` tree at SHA `f8560a1a` to settle the two follow-ups noted in `diff-vs-transtracks.md`.
