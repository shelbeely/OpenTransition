# 16 — Prioritized Action Plan

> Backlog ordered by ROI (impact ÷ effort). Each item links to an `ISSUE-###` from
> `07-issues-and-bugs.md` and a platform tag.

## NOW (≤ 1 week of focused work)

| Item | Tag | Issue | Why |
|------|-----|-------|-----|
| **1. Fix Play Store deploy package name** | `[CI]` | ISSUE-002 | Single-line config change; without it, no release pipeline works correctly. |
| **2. Register Wearable Capabilities** | `[INTEGRATION]` `[MOBILE]` `[WEAR]` | ISSUE-001 | Two `wear.xml` resource files. Unblocks every Wear feature. |
| **3. Implement `handleSyncRequest` (mobile→wear milestones)** | `[MOBILE]` `[INTEGRATION]` | ISSUE-003 | Wire `WearableHelper.syncMilestones` from the existing service. |
| **4. Decide & act on Realm/Room duplication** | `[MOBILE]` | ISSUE-004 | Pick (a) finish migration or (b) hide the toggle. Either prevents misleading users. |
| **5. Honest Settings copy for "Encrypt my database"** | `[MOBILE]` | ISSUE-004 / ISSUE-013 | If (4b), update `ENCRYPTED_DATABASE.md` and Settings string to describe scope accurately. |
| **6. Honest Wear feature copy** | `[DOCS]` | ISSUE-024 | Update `WEAR_APP_FEATURES.md` to match the implemented surface. |
| **7. Fix CI secret name drift** | `[CI]` | ISSUE-020 | Standardise `SECRETS_PROPERTIES_64` and `GOOGLE_SERVICES_JSON_64`. |
| **8. Add `:wear:assembleDebug` to PR-debug workflow** | `[CI]` `[WEAR]` | ISSUE-019 | One step. Catches wear-only regressions. |
| **9. Cancel `Handler.postDelayed` in Wear audio activity** | `[WEAR]` | ISSUE-009 | One-line fix for benign leak. |
| **10. Disable LeakCanary's launcher activity icon in debug** | `[MOBILE]` | (runtime obs.) | Debug APK shows two app icons; `<activity-alias android:enabled="false">` patch trivially fixes. |

## NEXT (1 month)

| Item | Tag | Issue |
|------|-----|-------|
| **11. Implement Wear `WearableListenerService` handler bodies** | `[WEAR]` | ISSUE-007 |
| **12. Switch Wear audio shipping to `ChannelClient`** | `[WEAR]` `[INTEGRATION]` | ISSUE-010 |
| **13. Route mobile audio handler through `DatabaseManager`** | `[MOBILE]` | ISSUE-013 |
| **14. Add source-node validation in `MobileWearableListenerService`** | `[MOBILE]` | ISSUE-011 |
| **15. Filename canonicalization for incoming audio** | `[MOBILE]` | (sec note in `10-security-review.md §7`) |
| **16. Replace fake formant analysis with real DSP — or label as estimates** | `[MOBILE]` | ISSUE-005 |
| **17. Move main-thread Realm reads off-thread** | `[MOBILE]` | ISSUE-012 |
| **18. Pick one image library (Coil); remove Picasso** | `[MOBILE]` | (see `09-`) |
| **19. Migrate off archived `firebase-ui-auth`** | `[MOBILE]` | (see `14-`) |
| **20. Upgrade SQLCipher to 4.6+ for 16 KB alignment** | `[MOBILE]` | (Aligned16KB lint) |
| **21. Bundle Wear AAB in the deploy step** | `[CI]` `[WEAR]` | ISSUE-019 |
| **22. Wear hold `PARTIAL_WAKE_LOCK` while recording** | `[WEAR]` | (see `04-`) |
| **23. Add a `CoroutineExceptionHandler` baseline** | `[MOBILE]` | (see `06-`) |
| **24. Add `WearableHelper` round-trip tests in `:shared`** | `[SHARED]` | (see `12-`) |
| **25. Fail-loud release signing** | `[MOBILE]` `[CI]` | (see `15-`) |
| **26. Schema version field on every Data Layer payload** | `[INTEGRATION]` `[SHARED]` | (see `05-`) |
| **27. Synchronise wear & mobile `versionCode`** | `[WEAR]` | (see `15-`) |
| **28. Adopt Detekt + ktlint** | `[MOBILE]` `[WEAR]` | (see `06-`) |
| **29. Adopt Gradle version catalog (`libs.versions.toml`)** | `[ALL]` | (see `14-`) |
| **30. Pre-allocate Paint/Path in `FormantChartView`** | `[MOBILE]` | DrawAllocation lint |
| **31. Remove unused deps (`navigation-compose`, `lifecycle-runtime-compose`, dead Wear Compose)** | `[MOBILE]` `[WEAR]` | ISSUE-015 / ISSUE-016 |
| **32. Add `CONTRIBUTING.md` and pairing/dev-setup doc for wear** | `[DOCS]` | (see `13-`) |
| **33. Fix Dependabot reviewer to current org** | `[CI]` | ISSUE-021 |

## LATER (quarter+)

| Item | Tag | Issue |
|------|-----|-------|
| **34. Split `SettingsFragment.kt` (961 LOC)** | `[MOBILE]` | ISSUE-022 |
| **35. Move XML Activity → Compose (single-Activity Compose host)** | `[MOBILE]` | (see `03-`) |
| **36. Migrate Wear UI from Views → Wear Compose (use the dead `WearTheme`)** | `[WEAR]` | ISSUE-016 |
| **37. Add Tile + Complication for Wear** | `[WEAR]` | (see `04-` table) |
| **38. Implement Health Connect / Health Services if vocal-tracking matures** | `[BOTH]` | (`04-` §7) |
| **39. Add Baseline Profile + Macrobenchmark** | `[MOBILE]` | (see `11-`) |
| **40. End-to-end RxJava → Coroutines + Flow migration** | `[MOBILE]` | architectural |
| **41. Add screenshot tests (Roborazzi)** | `[MOBILE]` | (see `12-`) |
| **42. Adopt Hilt or Koin (or formalise the manual DI)** | `[MOBILE]` | (see `02-` §2) |
| **43. F-Droid `foss` flavor (drop AdMob/Crashlytics/Firebase)** | `[MOBILE]` | (see `15-`) |
| **44. Dependency lockfile + verification metadata** | `[ALL]` | (see `14-`) |
| **45. Move colour palette + date helpers to `:shared`** | `[SHARED]` | (see `09-`) |

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
