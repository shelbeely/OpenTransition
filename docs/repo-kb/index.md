# Repository Knowledge Base

Durable, evidence-based knowledge base for the **OpenTransition** Android monorepo. Built with the [`repo-knowledge-base`](../../.github/skills/repo-knowledge-base/SKILL.md) agent skill.

## Project Summary

OpenTransition is a private, secure Kotlin/Android photo-journal app for tracking a gender transition journey. It is the active successor to the retired TransTracks app. The repository is a Gradle multi-module Android monorepo containing a phone/tablet app, a Wear OS companion app, and a shared library.

Evidence: [`README.md`](../../README.md), [`ARCHITECTURE.md`](../../ARCHITECTURE.md), [`MONOREPO.md`](../../MONOREPO.md), [`settings.gradle`](../../settings.gradle).

## Main Technologies

| Area | Evidence | Notes |
|---|---|---|
| Language | `build.gradle` (`kotlin_version = '2.0.20'`) | Kotlin 2.0.20 |
| UI | `mobile/build.gradle` (Compose BOM + appcompat + ConstraintLayout + RecyclerView) | Incremental XML Views → Jetpack Compose migration |
| Navigation | `build.gradle` (`nav_version = "2.8.5"`), `mobile/build.gradle` (SafeArgs plugin) | Jetpack Navigation 2.8.5 + SafeArgs |
| Async | `mobile/build.gradle` (Coroutines, RxJava 3, RxRelay, kotlinx-coroutines-rx3) | Coroutines + Flow primary; RxJava 3 legacy |
| Local persistence | `mobile/build.gradle` (Room 2.6.1, SQLCipher 4.5.4), `build.gradle` (`realm_version = '2.3.0'`) | Room + optional SQLCipher; Realm Kotlin SDK kept read-only for TransTracks import |
| Image loading | `mobile/build.gradle` (Coil 3.1.0) | Coil 3 Compose + OkHttp |
| Camera & ML | `mobile/build.gradle` (CameraX, ML Kit Face Detection) | CameraX + face detection |
| Cloud / Auth | `mobile/build.gradle` (Firebase plugins, AdMob) | Firebase Auth/Firestore/Crashlytics/Analytics, Google Mobile Ads |
| Wear comms | `wear/build.gradle`, `shared/build.gradle` (`play-services-wearable:18.1.0`) | Wearable Data Layer API |
| Build | `build.gradle` (AGP 8.13.0), `mobile/build.gradle` (KSP 2.0.20-1.0.25) | Gradle wrapper, AGP 8, KSP for annotation processors |

## Module Layout

| Module | Build file | Role |
|---|---|---|
| `:mobile` | [`mobile/build.gradle`](../../mobile/build.gradle) | Phone/tablet app — applicationId `com.shelbeely.opentransition` |
| `:wear` | [`wear/build.gradle`](../../wear/build.gradle) | Wear OS app — applicationId `com.shelbeely.opentransition.wear` |
| `:shared` | [`shared/build.gradle`](../../shared/build.gradle) | Android library shared by `:mobile` and `:wear` |

Modules declared in [`settings.gradle`](../../settings.gradle): `include ':mobile', ':wear', ':shared'`.

## Start Here

1. [Quickstart for Agents](./quickstart-for-agents.md)
2. [Repo Map](./repo-map.md)
3. [Architecture](./architecture.md)
4. [Build & Release](./build-and-release.md)
5. [Testing](./testing.md)
6. [Configuration & Secrets](./configuration.md)
7. [Security & Risk](./security-and-risk.md)
8. [Maintenance Guide](./maintenance-guide.md)

Index pages by area:

- [Features](./features/index.md)
- [Components](./components/index.md)
- [APIs / Contracts](./apis/index.md)
- [Data](./data/index.md)
- [Workflows](./workflows/index.md)
- [Decisions](./decisions/index.md)
- [Open Questions](./questions/index.md)
- [Files](./files/index.md)

## Current Coverage

See [`_state/coverage.md`](./_state/coverage.md). This is **pass 1** (huge-repo first pass) — root architecture, build, CI/CD, configuration, and agent instructions are documented; per-file/per-feature documentation is intentionally deferred.

## Open Questions

See [`_state/unknowns.md`](./_state/unknowns.md) and [`questions/index.md`](./questions/index.md).
