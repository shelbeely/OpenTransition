# Decisions

> Decisions captured in code/audit artifacts, plus standalone ADR-style pages.

## ADR-style pages

| Page | Scope |
|---|---|
| [`diff-vs-transtracks.md`](./diff-vs-transtracks.md) | Per-line evidence-based diff between OpenTransition and the archived upstream `TransTracks/TransTracks-Android` at SHA `f8560a1a` |
| [`rewrite-target-stack.md`](./rewrite-target-stack.md) | Target stack and ordered migration plan for any future ground-up rewrite, grounded in the Phase 1–5 evidence |

## Inferred Decisions (with evidence)

| # | Decision | Status | Evidence |
|---|---|---|---|
| 1 | Use a single Gradle multi-module monorepo (`:mobile`, `:wear`, `:shared`) instead of separate repos | Inferred | [`MONOREPO.md`](../../../MONOREPO.md), [`ARCHITECTURE.md`](../../../ARCHITECTURE.md), [`settings.gradle`](../../../settings.gradle) |
| 2 | Share version constants in root `build.gradle` so `:mobile` and `:wear` cannot drift | Known | Root [`build.gradle`](../../../build.gradle) `appMajor`/`appMinor`/`getAppVersionCode`; [`audit-report/15-release-and-distribution.md`](../../../audit-report/15-release-and-distribution.md) §6 |
| 3 | Migrate UI from XML Views → Jetpack Compose incrementally with interop | Known | [`README.md`](../../../README.md) Tech Stack, presence of both stacks in `mobile/build.gradle` |
| 4 | Migrate async layer from RxJava 3 → Coroutines incrementally via `kotlinx-coroutines-rx3` | Known | [`README.md`](../../../README.md), [`.github/skills/rxjava-to-coroutines-migration/SKILL.md`](../../../.github/skills/rxjava-to-coroutines-migration/SKILL.md) |
| 5 | Use Room (KSP) for new DB; keep Realm read-only just for TransTracks import | Known | [`mobile/build.gradle`](../../../mobile/build.gradle), [`README.md`](../../../README.md) |
| 6 | Optional SQLCipher encryption rather than always-on | Known | [`ENCRYPTED_DATABASE.md`](../../../ENCRYPTED_DATABASE.md) |
| 7 | Fail loud if a release task is requested without the release keystore | Known | `isReleaseBuildRequested` blocks in `mobile/build.gradle` and `wear/build.gradle`; [`audit-report/15-release-and-distribution.md`](../../../audit-report/15-release-and-distribution.md) §3 |
| 8 | Disable Crashlytics in debug builds | Known | `mobile/build.gradle`: `android.buildTypes.debug.ext.enableCrashlytics = false` |
| 9 | Run a CI "boycott check" against new dependencies | Known | [`boycott-check.yml`](../../../.github/workflows/boycott-check.yml), [`.github/scripts/boycott_check.py`](../../../.github/scripts/boycott_check.py) |
| 10 | Adopt agent skills under `.github/skills/` per [GitHub's Agent Skills spec](https://docs.github.com/en/copilot/concepts/agents/about-agent-skills) | Known | This skill at [`.github/skills/repo-knowledge-base/SKILL.md`](../../../.github/skills/repo-knowledge-base/SKILL.md) and 40+ others under `.github/skills/` |

Use the template in [`.github/skills/repo-knowledge-base/SKILL.md`](../../../.github/skills/repo-knowledge-base/SKILL.md) §"Eleventh Pass: Decisions and History" if you formalize any of these as standalone ADRs.
