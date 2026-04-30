# Knowledge Base Coverage

Snapshot at the end of pass 1.

## Summary

| Area | Status | Notes |
|---|---|---|
| Root docs | Complete | README, ARCHITECTURE, MONOREPO, CONTRIBUTING, ENCRYPTED_DATABASE all linked from KB |
| Build tooling | Complete | [`build-and-release.md`](../build-and-release.md) covers AGP, Kotlin, KSP, signing, version sharing |
| Source entrypoints | Partial | Module directories mapped; specific Activities/Services not yet pageized. The Wear `WearableListenerService` and the mobile `MobileWearableListenerService` are now indexed under the [Wearable Data Layer contract](../apis/wearable-data-layer.md) |
| Features | Partial | Three feature pages now documented under [`features/`](../features/index.md): [`photo-capture.md`](../features/photo-capture.md), [`import.md`](../features/import.md), [`app-lock.md`](../features/app-lock.md). Remaining features (milestones, audio, disguised launcher, encrypted DB standalone, Wear capture/milestones) still pointer-only via [`features/index.md`](../features/index.md) |
| Components | Partial (index only) | [`components/index.md`](../components/index.md) is now a flat top-level map (Activities, Fragment+Screen pairs, Services, Providers, Wear activities, `:shared` files). Per-component pages still deferred. |
| Tests | Partial | Commands + CI summarized in [`testing.md`](../testing.md); audit-report holds the coverage detail |
| CI/CD | Complete | [`workflows/index.md`](../workflows/index.md) lists all 9 workflows with triggers + roles |
| Config / env | Complete | [`configuration.md`](../configuration.md) lists files, generated files, CI secret names |
| APIs / contracts | Partial | [`apis/index.md`](../apis/index.md) — Mobile↔Wear contract is fully documented at [`apis/wearable-data-layer.md`](../apis/wearable-data-layer.md); Room/Realm/Firestore/AdMob still deferred |
| Data | Partial | [`data/index.md`](../data/index.md) lists stores; [`data/room-entities.md`](../data/room-entities.md) and [`data/realm-schema.md`](../data/realm-schema.md) added 2026-04-30 (full Room schema/DAOs/migrations + SQLCipher entry point; legacy Realm models marked read-only). EncryptedSharedPreferences and Firestore document shapes still deferred. |
| Decisions | Partial | 10 inferred decisions captured in [`decisions/index.md`](../decisions/index.md); no formal ADRs |
| Security / risk | Complete (risk-map level) | [`security-and-risk.md`](../security-and-risk.md) — full audit lives in `audit-report/10-security-review.md` |
| Glossary | Complete | [`glossary.md`](../glossary.md) |
| Dependency map | Complete (high-level) | [`dependency-map.md`](../dependency-map.md) |
| Data flow | Partial | High-level diagrams in [`data-flow.md`](../data-flow.md); per-feature flows deferred |
| Maintenance guide | Complete | [`maintenance-guide.md`](../maintenance-guide.md) |
| Copilot instructions | Updated | KB pointer appended to [`.github/copilot-instructions.md`](../../../.github/copilot-instructions.md) |
| Path-scoped KB editing instructions | Created | [`.github/instructions/repo-kb.instructions.md`](../../../.github/instructions/repo-kb.instructions.md) |
| Agent instructions | Created | [`AGENTS.md`](../../../AGENTS.md) |

## Files Scanned (pass 1)

| Path | Status | Notes |
|---|---|---|
| `README.md` | Summarized | Tech stack table, features inventory |
| `ARCHITECTURE.md` | Summarized | Module diagram, communication architecture |
| `MONOREPO.md` | Linked | Not summarized (dedicated guide) |
| `CONTRIBUTING.md` | Linked | Not summarized |
| `ENCRYPTED_DATABASE.md` | Linked | Authoritative for SQLCipher design |
| `WEAR_APP_FEATURES.md` | Linked | Authoritative for Wear feature list |
| `LOGO_REPLACEMENT_GUIDE.md` | Linked | Out-of-scope for pass 1 |
| `Agent.md` | Noted | Predates `AGENTS.md` |
| `DEFERRED_WORK.md` | Linked | Backlog reference |
| `audit-report/*.md` | Linked | Authoritative audits — KB does not duplicate |
| `build.gradle` (root) | Summarized | Version constants, plugin classpath |
| `settings.gradle` | Summarized | Module set |
| `gradle.properties` | Summarized | Build flags |
| `mobile/build.gradle` | Summarized (key deps) | Full dep list intentionally not duplicated |
| `wear/build.gradle` | Summarized | All deps listed in [`dependency-map.md`](../dependency-map.md) |
| `shared/build.gradle` | Summarized | All deps listed |
| `mkdocs.yml` | Noted | Powers the public docs site |
| `secrets.properties.example` | Noted | Referenced by [`configuration.md`](../configuration.md) |
| `.github/workflows/*.yml` | Summarized | One row in [`workflows/index.md`](../workflows/index.md) per file |
| `.github/skills/repo-knowledge-base/SKILL.md` | Installed | This is the skill that built the KB |

## Skipped (intentionally)

| Path | Reason |
|---|---|
| `mobile/src/**/*.kt`, `wear/src/**/*.kt`, `shared/src/**/*.kt` | Per-file/feature documentation deferred to pass 2 |
| `mobile/proguard-rules.pro`, `wear/proguard-rules.pro` | Listed; full rule analysis is out of scope |
| `keys/debug-keystore.jks`, any binary asset | Binary; documented existence only |
| `screenshots/*.png` | Generated artifacts |
| `docs/` (the MkDocs site) | Linked; not re-summarized |
| `audit-report/runtime-artifacts/` | Run artifacts |

## Instruction Files

| File | Status | Purpose |
|---|---|---|
| `.github/copilot-instructions.md` | Updated | Repo-wide Copilot instructions; KB pointer appended |
| `.github/instructions/repo-kb.instructions.md` | Created | Path-scoped editing rules for `docs/repo-kb/**` |
| `AGENTS.md` | Created | Agent entry point for Copilot CLI / VS Code agent |
| `Agent.md` | Preserved | Pre-existing; left untouched |

## Next Pass

Prioritised checklist:

1. ~~Create `features/photo-capture.md` from the CameraX + ML Kit pipeline.~~ ✅ Done 2026-04-30.
2. ~~Create `features/import.md` from the TransTracks `.ttbackup` → Realm → Room flow.~~ ✅ Done 2026-04-30 (covers both `.ttbackup` and the in-app `.realm` picker, plus `RealmToRoomMigration`).
3. ~~Create `features/app-lock.md` covering PIN/pattern/biometric + decoy vault.~~ ✅ Done 2026-04-30 (note: source has no `pattern` lock type; documented set is `off`/`normal`/`trains`/`biometric`).
4. ~~Create `data/room-entities.md` enumerating Room entities and DAOs from `mobile/src/main/java/com/shelbeely/opentransition/data/`.~~ ✅ Done 2026-04-30 (covers `AppDatabase` v3, four entities, every DAO query, both migrations, and the SQLCipher `SupportFactory` integration point). Also added `data/realm-schema.md` documenting the read-only legacy Realm models.
5. ~~Create `apis/wearable-data-layer.md` from `shared/.../WearableConstants.kt` + `models/MilestoneData.kt`.~~ ✅ Done in earlier pass.
6. ~~Resolve the SDK-targets discrepancy in [`questions/index.md`](../questions/index.md) and update both KB and `.github/copilot-instructions.md`.~~ ✅ Done 2026-04-30 — `.github/copilot-instructions.md` updated to `(minSdk 26, targetSdk 35, compileSdk 36)` and SDK-floor "What NOT to do" line updated to `minSdkVersion below 26`. KB `architecture.md` already source-aligned.
7. ~~Re-validate that all referenced commands still work in the Cloud Agent runner; update [`command-log.md`](./command-log.md).~~ ✅ Done 2026-04-30. `:mobile:testDebugUnitTest`, `:mobile:assembleDebug`, and `test` all PASS in Copilot Cloud Agent. `:mobile:lintDebug` FAILS with 2 pre-existing errors (`HomeScreen.kt:133` `LocalContextGetResourceValueCall`); recorded as baseline, fix out of scope for KB.
8. Continue Features pass: milestones (cross-cuts `:mobile` + `:wear`), disguised launcher icon, audio tracking, encrypted-DB standalone page, Wear capture/milestones.
9. Fix the 2 pre-existing `:mobile:lintDebug` errors so the documented pre-PR check is green again. Out of scope for KB; tracked here as a follow-up.
