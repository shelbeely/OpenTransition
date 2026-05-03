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

## 2026-05-03 00:47 — App introduction docs and audio

### Goal

Create a spoken + written app introduction for OpenTransition as requested in the problem statement.

### What changed

| File | Description |
|---|---|
| `docs/app-introduction.md` | New file. Short written intro, 30-second narration script, 60-second narration script. |
| `docs/audio/app-introduction.mp3` | Generated audio of the 30-second script using ElevenLabs, narrated in Shelbee's own cloned voice (`voice_id: 6msqkRQQIp6SumoiB7TS`). |

### What was validated

- Feature list cross-checked against `README.md` §"Key Features" — no overclaims.
- Audio tracking correctly described as a **preview feature** (pitch/formant are estimates, not measurements).
- ElevenLabs API call succeeded; audio saved to `docs/audio/app-introduction.mp3`.

### Notes for future agents

- The "Shelbee" cloned ElevenLabs voice ID is `6msqkRQQIp6SumoiB7TS` — use it for any further audio generation for this project.
- No build/lint changes were made; no tests required for documentation-only PR.
