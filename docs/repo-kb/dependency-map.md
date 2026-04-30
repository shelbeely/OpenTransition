# Dependency Map

Pass-1 summary of dependencies pulled in by each module. The authoritative source is each module's `build.gradle`. Run `./gradlew :mobile:dependencies` for the resolved graph.

## Versions Defined Centrally

In root [`build.gradle`](../../build.gradle):

| Variable | Value |
|---|---|
| `kotlin_version` | `2.0.20` |
| `realm_version` | `2.3.0` |
| `nav_version` | `2.8.5` |

In root buildscript classpath:

- `androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version`
- `com.android.tools.build:gradle:8.13.0`
- `com.google.firebase:firebase-crashlytics-gradle:2.9.9`
- `com.google.gms:google-services:4.4.0`
- `org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version`
- `org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:$kotlin_version`

Plus the Realm plugin: `io.realm.kotlin` version `$realm_version` (declared `apply false`).

## `:mobile` (key dependencies)

Authoritative: [`mobile/build.gradle`](../../mobile/build.gradle). Highlights only:

- **Project**: `project(':shared')`
- **AndroidX**: `activity-ktx`, `appcompat`, `core-ktx`, `constraintlayout`, `recyclerview`, `exifinterface`, `biometric`, `security-crypto`
- **Compose**: Compose BOM, `material3`, `material-icons-extended`, `foundation`, `ui-tooling-preview`
- **Lifecycle** (`$lifecycle_version`): `lifecycle-viewmodel-ktx`, `lifecycle-viewmodel-savedstate`, `lifecycle-common-java8`, `lifecycle-runtime-compose`
- **Room** (`$room_version`): `room-runtime`, `room-ktx`, `room-compiler` (KSP)
- **Encrypted DB**: `net.zetetic:android-database-sqlcipher:4.5.4`
- **Realm**: Realm Kotlin SDK 2.3.0 (read-only, for TransTracks import)
- **Coil 3**: `io.coil-kt.coil3:coil-compose:3.1.0`, `io.coil-kt.coil3:coil-network-okhttp:3.1.0`
- **Camera + ML**: CameraX, ML Kit Face Detection (see `mobile/build.gradle` for exact coordinates)
- **Firebase**: Auth, Crashlytics, Firestore, Analytics (via `com.google.gms.google-services` plugin)
- **AdMob**: Google Mobile Ads
- **RxJava 3 stack** (legacy, being phased out): RxJava, RxRelay, RxBinding, `kotlinx-coroutines-rx3`
- **Test**: `androidx.arch.core:core-testing`, `androidx.lifecycle:lifecycle-runtime-testing`, AndroidJUnitRunner instrumentation

## `:wear`

Authoritative: [`wear/build.gradle`](../../wear/build.gradle).

- `project(':shared')`
- `org.jetbrains.kotlin:kotlin-stdlib-jdk8`
- `androidx.wear:wear:1.3.0`
- `androidx.wear.compose:compose-material:1.4.1`
- `androidx.wear.compose:compose-foundation:1.4.1`
- `androidx.compose.ui:ui-tooling-preview:1.7.8`
- `androidx.activity:activity-ktx:1.7.2`
- `androidx.core:core-ktx:1.10.1`
- `androidx.constraintlayout:constraintlayout:2.1.4`
- `com.google.android.gms:play-services-wearable:18.1.0`
- `com.google.code.gson:gson:2.10.1`

## `:shared`

Authoritative: [`shared/build.gradle`](../../shared/build.gradle). Intentionally minimal:

- `org.jetbrains.kotlin:kotlin-stdlib-jdk8`
- `com.google.android.gms:play-services-wearable:18.1.0`
- `com.google.code.gson:gson:2.10.1`

## Updates

- Dependabot is configured: [`.github/dependabot.yml`](../../.github/dependabot.yml).
- New dependencies must pass [`boycott-check.yml`](../../.github/workflows/boycott-check.yml) and should be checked against the GitHub Advisory Database before adoption.

## Notes

- A full per-version table is intentionally **not** maintained here — it would go stale instantly. Re-read the `build.gradle` files for exact versions.
- See [`audit-report/14-dependencies-and-supply-chain.md`](../../audit-report/14-dependencies-and-supply-chain.md) for the supply-chain assessment.
