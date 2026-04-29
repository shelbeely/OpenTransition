# Audit-report changelog

All notable changes to the `audit-report/` folder are documented here.

## 2026-04-29 — Refresh against `9c18daf` (47 commits since baseline)

**Driver:** "Analyze the repo and give me an updated audit report" (task request).

**Scope:** Read-only delta against the 2026-04-22 baseline at `dd22666efb42e7f3f2d7e83a9f57ab7c8123852e`. The 17 baseline documents (`00`–`17`) were left **unchanged** apart from short pointer banners on `00` and `16`. The authoritative current-state document is **`18-refresh-2026-04-29.md`**.

### Added

- `18-refresh-2026-04-29.md` — supersedes the per-finding status across docs `00`–`17` with a delta-focused refresh; refreshed sub-scores; refreshed criticals; refreshed action plan.
- `CHANGELOG.md` — this file.

### Changed (banners only, no content rewrites)

- `README.md` — index now links to `18-refresh-2026-04-29.md` and notes which baseline doc each refreshed section corresponds to.
- `00-executive-summary.md` — banner at top pointing readers to the refresh.
- `16-prioritized-action-plan.md` — banner at top pointing readers to the refreshed action plan in §8 of the refresh.

### Re-run signal

| Check | 2026-04-22 | 2026-04-29 |
|-------|------------|------------|
| `:mobile:assembleDebug` APK size | 74 MB | **132 MB** (ISSUE-036) |
| `:wear:assembleDebug` APK size | 27 MB | 26 MB |
| `:mobile:testDebugUnitTest` | n/a (was not run) | **138 / 138 passed** |
| `:shared:test` | n/a | **6 / 6 passed** |
| `:mobile:lintDebug` | 0 errors / 235 warnings | **❌ 2 errors / 188 warnings** (ISSUE-035; both in `HomeScreen.kt`) |

### High-level summary of deltas

- **Closed:** ISSUE-001, 002, 003, 007, 009, 010, 011, 015, 017, 019, 020, 023, 026, 028; signing-config fail-loud.
- **Carried forward:** ISSUE-004, 005, 008, 012, 013, 014, 016, 018, 022, 024.
- **New:** ISSUE-029 (Material3 alpha pin), ISSUE-030 (activity-ktx skew), ISSUE-031 (README voice copy), ISSUE-032 (no KDoc on voice UI), ISSUE-033 (AudioDecoder/AudioAnalysisUtil untested), ISSUE-034 (`material-icons-extended` usage unverified), ISSUE-035 (lint errors in `HomeScreen.kt`), ISSUE-036 (APK size growth).

See `18-refresh-2026-04-29.md` for full evidence.

---

## 2026-04-22 — Initial audit at `dd22666`

Initial 17-document monorepo audit. See `README.md` for the original index.
