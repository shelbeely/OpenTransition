# OpenTransition Monorepo Audit Report

**Audit date:** 2026-04-22
**Commit SHA:** `dd22666efb42e7f3f2d7e83a9f57ab7c8123852e`
**Branch:** `copilot/audit-monorepo-analysis`
**Scope:** Read-only audit of the entire monorepo (`:mobile`, `:wear`, `:shared`, build / CI / docs).

> ⚠️ **No production code was modified.** This audit is analysis only.
> Every finding cites file paths and line ranges as evidence.

---

## Document Index

| # | Document | What's inside |
|---|----------|---------------|
| 00 | [`00-executive-summary.md`](00-executive-summary.md) | One-page health snapshot, sub-scores, top 5 strengths, top 5 critical issues, "1-week shortlist", maturity stage. |
| 01 | [`01-monorepo-overview.md`](01-monorepo-overview.md) | Gradle setup, module graph, SDK/Kotlin/AGP versions, LOC tables, largest files. |
| 02 | [`02-architecture-assessment.md`](02-architecture-assessment.md) | Inferred architecture, DI, navigation, state, concurrency; ratings. |
| 03 | [`03-mobile-app-review.md`](03-mobile-app-review.md) | Deep dive on the handheld app (UI, lifecycle, storage, permissions, networking, a11y). |
| 04 | [`04-wear-os-app-review.md`](04-wear-os-app-review.md) | Deep dive on the Wear OS companion (Compose vs Views, Tiles/Complications, ambient, rotary, battery). |
| 05 | [`05-mobile-wear-integration.md`](05-mobile-wear-integration.md) | Cross-device contract: capabilities, payloads, retry, conflict resolution. |
| 06 | [`06-code-quality-report.md`](06-code-quality-report.md) | Per-module quality ratings (Kotlin idiom, Compose, coroutines, resources). |
| 07 | [`07-issues-and-bugs.md`](07-issues-and-bugs.md) | Catalogued issues (`ISSUE-001…`) with severity, location, platform, remediation. |
| 08 | [`08-unfinished-features.md`](08-unfinished-features.md) | TODOs, stubs, dead-end paths, abandoned resources. |
| 09 | [`09-redundancy-and-dead-code.md`](09-redundancy-and-dead-code.md) | Duplication, unused code, redundant deps, legacy paths. |
| 10 | [`10-security-review.md`](10-security-review.md) | Secrets, signing, exported components, encryption, peer trust. |
| 11 | [`11-performance-and-battery.md`](11-performance-and-battery.md) | Startup, jank, leaks, image loading, Wear-specific power. |
| 12 | [`12-testing-and-ci.md`](12-testing-and-ci.md) | Test coverage, CI matrix, release automation. |
| 13 | [`13-documentation-review.md`](13-documentation-review.md) | README, MONOREPO, ARCHITECTURE, KDoc, contradictions. |
| 14 | [`14-dependencies-and-supply-chain.md`](14-dependencies-and-supply-chain.md) | Inventory, outdated, license, Renovate/Dependabot. |
| 15 | [`15-release-and-distribution.md`](15-release-and-distribution.md) | Variants, signing, Play Store coupling, versioning. |
| 16 | [`16-prioritized-action-plan.md`](16-prioritized-action-plan.md) | Ordered backlog (Now / Next / Later) tagged by platform. |

---

## Tools / approach

- **Static, read-only inspection** of the repo at the SHA above using `view`, `grep`, `glob`, and `find`.
- **No builds, no tests, no linters were executed** during this audit (consistent with the read-only mandate).
  Where ratings depend on tool config (e.g. Detekt, ktlint), the configuration is inspected — not the report
  output — and that limitation is noted.
- **Severity emoji:** 🔴 Critical · 🟠 High · 🟡 Medium · 🟢 Low
- **Platform tags:** `[MOBILE]` `[WEAR]` `[SHARED]` `[INTEGRATION]`
- **Rating scale (1–10):** 10 exemplary · 8–9 solid · 6–7 functional w/ concerns · 4–5 needs work · 2–3 broken · 1 dangerous.
- **`[NEEDS HUMAN REVIEW]`** marks anything where intent could not be confirmed from source alone.

## Glossary

| Term | Meaning |
|------|---------|
| **Data Layer** | Google Play Services Wearable API for phone↔watch comms (`DataClient`, `MessageClient`, `CapabilityClient`, `NodeClient`). |
| **Capability** | A named string a Wear/mobile app declares so peers can find it. Declared in `res/values/wear.xml` or via `addLocalCapability`. |
| **Standalone Wear app** | A Wear OS app marked `com.google.android.wearable.standalone=true` that works without a phone companion. |
| **Realm** | Realm Kotlin SDK 2.3.0 — used here for both legacy backups and (still) most production reads. |
| **Room** | AndroidX Room 2.6.1 — declared as the "new" persistence layer in docs but largely unused at runtime. |
| **SQLCipher** | net.zetetic SQLite encryption library wired into Room as an opt-in encryption layer. |
| **CameraX** | AndroidX camera library used for the face-detection viewfinder. |
| **UMP** | User Messaging Platform — Google's GDPR/IDFA consent SDK used for AdMob compliance. |
| **`ttbackup`** | Custom ZIP-based export format inherited from the upstream TransTracks app. |

---

## Critical-finding teaser

The most consequential findings — explained in detail in subsequent documents — are:

1. 🔴 **Wearable capabilities are never registered** (no `wear.xml`, no `addLocalCapability` call) — every
   `getCapability(CAPABILITY_MOBILE_APP, FILTER_REACHABLE)` will return zero nodes, so every wear-side
   button (Take Photo, Sync, Camera control, Audio send) silently fails. See `05-mobile-wear-integration.md`.
2. 🔴 **Play Store deploy uploads to the wrong package name** (`com.drspaceboo.transtracks` vs the app's
   actual `com.shelbeely.opentransition`). See `15-release-and-distribution.md` and `.github/workflows/ci.yml:86`.
3. 🔴 **Two databases in production simultaneously** — Room (encrypted) and Realm (legacy) — with all UI
   still reading from Realm. The "Encrypted Database" feature toggle in Settings is largely cosmetic. See
   `09-redundancy-and-dead-code.md` and `10-security-review.md`.
4. 🟠 **`AudioAnalysisUtil` returns hard-coded fake formant numbers** while marketing copy claims
   "pitch/formant analysis". User-visible correctness defect. See `07-issues-and-bugs.md`.
5. 🟠 **The Wear app uses XML Views + `Activity` with a Compose theme that is never invoked** — `WearTheme.kt`
   is dead code. Compose dependencies are pulled in but do nothing. See `04-wear-os-app-review.md`.
