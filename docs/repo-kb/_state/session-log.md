# Session Log

## 2026-04-29 21:46 — Pass 1 bootstrap

### Goal

Move the `repo-knowledge-base` skill to the location specified by the [GitHub Agent Skills docs](https://docs.github.com/en/copilot/concepts/agents/about-agent-skills), then apply the skill to seed `docs/repo-kb/`.

### Commands Run

| Command | Result |
|---|---|
| `git mv repo-knowledge-base-SKILL.md .github/skills/curated/repo-knowledge-base/SKILL.md` | OK |
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
- The skill itself (`.github/skills/curated/repo-knowledge-base/SKILL.md`) for templates

### Files Created or Updated

Created:

- `.github/skills/curated/repo-knowledge-base/SKILL.md` (moved from repo root)
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
