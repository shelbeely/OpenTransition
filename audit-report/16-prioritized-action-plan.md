# 16 — Prioritized Action Plan

> Backlog ordered by ROI (impact ÷ effort). Each item links to an `ISSUE-###` from
> `07-issues-and-bugs.md` and a platform tag.

## NOW (≤ 1 week of focused work)

| Item | Tag | Issue | Why | Skills |
|------|-----|-------|-----|--------|
| **1. Fix Play Store deploy package name** | `[CI]` | ISSUE-002 | Single-line config change; without it, no release pipeline works correctly. | `gh-fix-ci` |
| **2. Register Wearable Capabilities** | `[INTEGRATION]` `[MOBILE]` `[WEAR]` | ISSUE-001 | Two `wear.xml` resource files. Unblocks every Wear feature. | `android-data-layer` · `android-coroutines` |
| **3. Implement `handleSyncRequest` (mobile→wear milestones)** | `[MOBILE]` `[INTEGRATION]` | ISSUE-003 | Wire `WearableHelper.syncMilestones` from the existing service. | `android-data-layer` · `android-coroutines` |
| **4. Decide & act on Realm/Room duplication** | `[MOBILE]` | ISSUE-004 | Pick (a) finish migration or (b) hide the toggle. Either prevents misleading users. | `android-data-layer` |
| **5. Honest Settings copy for "Encrypt my database"** | `[MOBILE]` | ISSUE-004 / ISSUE-013 | If (4b), update `ENCRYPTED_DATABASE.md` and Settings string to describe scope accurately. | — |
| **6. Honest Wear feature copy** | `[DOCS]` | ISSUE-024 | Update `WEAR_APP_FEATURES.md` to match the implemented surface. | — |
| **7. Fix CI secret name drift** | `[CI]` | ISSUE-020 | Standardise `SECRETS_PROPERTIES_64` and `GOOGLE_SERVICES_JSON_64`. | `gh-fix-ci` |
| **8. Add `:wear:assembleDebug` to PR-debug workflow** | `[CI]` `[WEAR]` | ISSUE-019 | One step. Catches wear-only regressions. | `gh-fix-ci` |
| **9. Cancel `Handler.postDelayed` in Wear audio activity** | `[WEAR]` | ISSUE-009 | One-line fix for benign leak. | `kotlin-concurrency-expert` |
| **10. Disable LeakCanary's launcher activity icon in debug** | `[MOBILE]` | (runtime obs.) | Debug APK shows two app icons; `<activity-alias android:enabled="false">` patch trivially fixes. | — |

## NEXT (1 month)

| Item | Tag | Issue | Skills |
|------|-----|-------|--------|
| **11. Implement Wear `WearableListenerService` handler bodies** | `[WEAR]` | ISSUE-007 | `android-coroutines` |
| **12. Switch Wear audio shipping to `ChannelClient`** | `[WEAR]` `[INTEGRATION]` | ISSUE-010 | `android-coroutines` |
| **13. Route mobile audio handler through `DatabaseManager`** | `[MOBILE]` | ISSUE-013 | `android-data-layer` |
| **14. Add source-node validation in `MobileWearableListenerService`** | `[MOBILE]` | ISSUE-011 | `kotlin-concurrency-expert` |
| **15. Filename canonicalization for incoming audio** | `[MOBILE]` | (sec note in `10-security-review.md §7`) | — |
| **16. Replace fake formant analysis with real DSP — or label as estimates** | `[MOBILE]` | ISSUE-005 | `android-viewmodel` |
| **17. Move main-thread Realm reads off-thread** | `[MOBILE]` | ISSUE-012 | `kotlin-concurrency-expert` · `android-coroutines` |
| **18. Pick one image library (Coil); remove Picasso** | `[MOBILE]` | (see `09-`) | `coil-compose` |
| **19. Migrate off archived `firebase-ui-auth`** | `[MOBILE]` | (see `14-`) | — |
| **20. Upgrade SQLCipher to 4.6+ for 16 KB alignment** | `[MOBILE]` | (Aligned16KB lint) | `android-gradle-logic` |
| **21. Bundle Wear AAB in the deploy step** | `[CI]` `[WEAR]` | ISSUE-019 | `gh-fix-ci` |
| **22. Wear hold `PARTIAL_WAKE_LOCK` while recording** | `[WEAR]` | (see `04-`) | `android-coroutines` |
| **23. Add a `CoroutineExceptionHandler` baseline** | `[MOBILE]` | (see `06-`) | `kotlin-concurrency-expert` |
| **24. Add `WearableHelper` round-trip tests in `:shared`** | `[SHARED]` | (see `12-`) | `android-testing` |
| **25. Fail-loud release signing** | `[MOBILE]` `[CI]` | (see `15-`) | `gh-fix-ci` |
| **26. Schema version field on every Data Layer payload** | `[INTEGRATION]` `[SHARED]` | (see `05-`) | `android-data-layer` |
| **27. Synchronise wear & mobile `versionCode`** | `[WEAR]` | (see `15-`) | `android-gradle-logic` |
| **28. Adopt Detekt + ktlint** | `[MOBILE]` `[WEAR]` | (see `06-`) | `android-gradle-logic` |
| **29. Adopt Gradle version catalog (`libs.versions.toml`)** | `[ALL]` | (see `14-`) | `android-gradle-logic` |
| **30. Pre-allocate Paint/Path in `FormantChartView`** | `[MOBILE]` | DrawAllocation lint | `compose-performance-audit` |
| **31. Remove unused deps (`navigation-compose`, `lifecycle-runtime-compose`, dead Wear Compose)** | `[MOBILE]` `[WEAR]` | ISSUE-015 / ISSUE-016 | `android-gradle-logic` |
| **32. Add `CONTRIBUTING.md` and pairing/dev-setup doc for wear** | `[DOCS]` | (see `13-`) | — |
| **33. Fix Dependabot reviewer to current org** | `[CI]` | ISSUE-021 | `gh-fix-ci` |

## LATER (quarter+)

| Item | Tag | Issue | Skills |
|------|-----|-------|--------|
| **34. Split `SettingsFragment.kt` (961 LOC)** | `[MOBILE]` | ISSUE-022 | `android-architecture` |
| **35. Move XML Activity → Compose (single-Activity Compose host)** | `[MOBILE]` | (see `03-`) | `migrate-xml-views-to-jetpack-compose` · `xml-to-compose-migration` · `compose-ui` · `navigation-3` |
| **36. Migrate Wear UI from Views → Wear Compose (use the dead `WearTheme`)** | `[WEAR]` | ISSUE-016 | `migrate-xml-views-to-jetpack-compose` · `xml-to-compose-migration` · `compose-ui` |
| **37. Add Tile + Complication for Wear** | `[WEAR]` | (see `04-` table) | — |
| **38. Implement Health Connect / Health Services if vocal-tracking matures** | `[BOTH]` | (`04-` §7) | — |
| **39. Add Baseline Profile + Macrobenchmark** | `[MOBILE]` | (see `11-`) | `compose-performance-audit` |
| **40. End-to-end RxJava → Coroutines + Flow migration** | `[MOBILE]` | architectural | `rxjava-to-coroutines-migration` · `kotlin-concurrency-expert` |
| **41. Add screenshot tests (Roborazzi)** | `[MOBILE]` | (see `12-`) | `android-testing` |
| **42. Adopt Hilt or Koin (or formalise the manual DI)** | `[MOBILE]` | (see `02-` §2) | `android-architecture` |
| **43. F-Droid `foss` flavor (drop AdMob/Crashlytics/Firebase)** | `[MOBILE]` | (see `15-`) | `r8-analyzer` |
| **44. Dependency lockfile + verification metadata** | `[ALL]` | (see `14-`) | `android-gradle-logic` |
| **45. Move colour palette + date helpers to `:shared`** | `[SHARED]` | (see `09-`) | `android-architecture` |

## Cross-reference matrix (top items ↔ findings)

| Action | ISSUE | Sub-report |
|--------|-------|-----------|
| 1 | ISSUE-002 | 07, 12 |
| 2 | ISSUE-001 | 04, 05, 07, 08 |
| 3 | ISSUE-003 | 05, 07, 08 |
| 4 | ISSUE-004 | 03, 07, 09, 10 |
| 5 | ISSUE-013 | 05, 07, 10 |
| 6 | ISSUE-024 | 13 |
| 7 | ISSUE-020 | 07, 12 |
| 8 | ISSUE-019 | 07, 12 |
| 9 | ISSUE-009 | 04, 07, 11 |
| 10 | runtime obs. | 17 |

## Estimated cumulative impact if items 1–10 are done

- **Mobile rating** improves from 5 → 7
- **Wear rating** improves from 4 → 6
- **Integration rating** improves from 2 → 6
- **Documentation rating** improves from 4 → 6
- **Overall** improves from 5 → 6.5

Items 11–25 (the "Next" tier) bring overall to ~7.5/10. The remaining "Later" tier is product-quality
investment rather than triage.

## Skills Legend

The `Skills` column in each table references agent skills installed in `.github/skills/`. Load the
relevant skill before starting an item to get targeted guidance and code patterns.

| Skill name | Path in `.github/skills/` | Scope |
|---|---|---|
| `gh-fix-ci` | `curated/gh-fix-ci` | Diagnose & fix failing GitHub Actions CI/CD workflows |
| `android-coroutines` | `concurrency_and_networking/android-coroutines` | Production-quality Kotlin Coroutines patterns on Android |
| `kotlin-concurrency-expert` | `concurrency_and_networking/kotlin-concurrency-expert` | Review & fix Coroutines bugs (leaks, lifecycle, cancellation) |
| `android-data-layer` | `architecture/android-data-layer` | Repository pattern, Room, Retrofit, offline-first sync |
| `android-architecture` | `architecture/android-architecture` | Clean Architecture, Hilt DI, modularisation |
| `android-viewmodel` | `architecture/android-viewmodel` | StateFlow / SharedFlow ViewModel patterns |
| `android-gradle-logic` | `build_and_tooling/android-gradle-logic` | Convention plugins, Version Catalogs, Gradle best practices |
| `gradle-build-performance` | `performance/gradle-build-performance` | Debug & optimise Gradle build times |
| `coil-compose` | `ui/coil-compose` | Image loading with Coil in Jetpack Compose |
| `compose-ui` | `ui/compose-ui` | Compose best practices: state hoisting, modifiers, theming |
| `compose-performance-audit` | `performance/compose-performance-audit` | Recomposition audits, lazy-list keys, stability |
| `xml-to-compose-migration` | `migration/xml-to-compose-migration` | Layout mapping & state migration guide (XML → Compose) |
| `migrate-xml-views-to-jetpack-compose` | `android/jetpack-compose/migration/migrate-xml-views-to-jetpack-compose` | Step-by-step XML View → Jetpack Compose workflow |
| `rxjava-to-coroutines-migration` | `migration/rxjava-to-coroutines-migration` | RxJava type/operator mapping to Coroutines & Flow |
| `navigation-3` | `android/navigation/navigation-3` | Jetpack Navigation 3 setup, type-safe routes, deep links |
| `android-testing` | `testing_and_automation/android-testing` | Unit, Hilt integration, and screenshot (Roborazzi) tests |
| `r8-analyzer` | `android/performance/r8-analyzer` | Optimise R8/ProGuard keep rules and app size |
