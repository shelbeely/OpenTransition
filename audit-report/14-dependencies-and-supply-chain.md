# 14 — Dependencies and Supply Chain

## Approach

This project does **not** use a Gradle version catalog (`gradle/libs.versions.toml`). Versions are
declared inline in each `build.gradle` (with a few `ext` strings in the root). Inventory below is
extracted from `build.gradle`, `mobile/build.gradle`, `wear/build.gradle`, `shared/build.gradle`.

The task instructions allow consultation of the GitHub Advisory Database; current code adds no new
dependency, so a `gh-advisory-database` query is not required for change validation. Versions flagged
🟠 / 🔴 below are based on Maven Central freshness checks.

## Build-system versions

| Tool | Version | Status |
|------|---------|--------|
| Gradle | **8.13** (`gradle-wrapper.properties`) | 🟢 Current |
| AGP | **8.13.0** | 🟢 Current |
| Kotlin | **2.0.20** | 🟡 Kotlin 2.1+ available; mid-cycle. |
| KSP | **2.0.20-1.0.25** | 🟢 matches Kotlin |
| JDK | sourceCompatibility / targetCompatibility = 1.8 | 🟠 Java 17 is now standard for AGP 8; 1.8 still works but compiler emits old bytecode. |
| Kotlin JVM target | 1.8 | 🟠 same |
| `compileSdk` | 36 | 🟢 |
| `minSdk` | mobile 21, wear 30 | 🟢 reasonable |
| `targetSdk` | 36 (both modules) | 🟢 |
| Realm Kotlin | 2.3.0 | 🟢 |
| Compose BOM | 2025.05.01 | 🟢 |

## Full dependency inventory (mobile)

<details>
<summary>Click to expand (~60 dependencies)</summary>

| GAV | Version | Purpose | Health |
|-----|---------|---------|--------|
| `androidx.activity:activity-ktx` | 1.9.3 | Activity Result APIs | 🟢 |
| `androidx.appcompat:appcompat` | 1.6.1 | Legacy AppCompat | 🟡 1.7 available |
| `androidx.compose.material3:material3` (BOM-managed) | 1.x via BOM 2025.05.01 | M3 Compose | 🟢 |
| `androidx.compose.foundation:foundation` | BOM | Compose foundation | 🟢 |
| `androidx.compose.ui:ui-tooling-preview` | BOM | Preview | 🟢 |
| `androidx.compose.ui:ui-tooling` (debug) | BOM | Tooling | 🟢 |
| `androidx.compose.ui:ui-test-junit4` (test) | BOM | Compose tests | 🟢 (unused) |
| `androidx.compose.ui:ui-test-manifest` (debug) | BOM | Test manifest | 🟢 (unused) |
| `io.coil-kt.coil3:coil-compose` | 3.1.0 | Image loading (Compose) | 🟢 |
| `io.coil-kt.coil3:coil-network-okhttp` | 3.1.0 | Coil network | 🟢 |
| `androidx.arch.core:core-testing` (test) | 2.2.0 | InstantTaskExecutorRule | 🟢 (unused) |
| `androidx.constraintlayout:constraintlayout` | 2.1.4 | Layouts | 🟢 |
| `androidx.core:core-ktx` | 1.10.1 | Core KTX | 🟡 1.13 available |
| `androidx.lifecycle:lifecycle-viewmodel-ktx` | 2.8.7 | ViewModel | 🟢 |
| `androidx.lifecycle:lifecycle-viewmodel-savedstate` | 2.8.7 | SavedState | 🟢 |
| `androidx.lifecycle:lifecycle-common-java8` | 2.8.7 | Java 8 helpers | 🟢 |
| `androidx.lifecycle:lifecycle-runtime-compose` | 2.8.7 | Compose lifecycle | 🟡 unused (ISSUE-015 family) |
| `androidx.lifecycle:lifecycle-runtime-testing` (test) | 2.8.7 | Tests | 🟢 (unused) |
| `androidx.recyclerview:recyclerview` | 1.3.2 | RecyclerView | 🟢 |
| `androidx.exifinterface:exifinterface` | 1.3.6 | EXIF | 🟢 |
| `androidx.biometric:biometric` | 1.1.0 | Biometric prompt | 🟡 1.2.0-alpha05 has API improvements |
| `androidx.security:security-crypto` | 1.1.0-alpha06 | Encrypted prefs | 🟠 alpha in production |
| `androidx.room:room-runtime` | 2.6.1 | Room | 🟡 2.7+ available |
| `androidx.room:room-ktx` | 2.6.1 | Room KTX | 🟡 |
| `androidx.room:room-compiler` (ksp) | 2.6.1 | Room KSP | 🟡 |
| `net.zetetic:android-database-sqlcipher` | **4.5.4** | SQLCipher | 🔴 deprecated artifact (rebrand to `net.zetetic:sqlcipher-android` since 4.6.0); flagged for **Aligned16KB** by Lint |
| `androidx.sqlite:sqlite` | 2.4.0 | SQLite frame | 🟢 |
| `androidx.navigation:navigation-fragment-ktx` | 2.8.5 | Nav | 🟢 |
| `androidx.navigation:navigation-ui-ktx` | 2.8.5 | Nav UI | 🟢 |
| `androidx.navigation:navigation-compose` | 2.8.5 | Compose nav | 🟡 **unused** (ISSUE-015) |
| `androidx.navigation:navigation-testing` (androidTest) | 2.8.5 | Tests | 🟢 (unused) |
| `com.google.android.gms:play-services-ads` | 22.4.0 | AdMob | 🟡 23.6+ available |
| `com.google.android.gms:play-services-auth` | 20.7.0 | Sign-In | 🟡 21.x available; deprecated in favor of Credential Manager |
| `com.google.android.gms:play-services-wearable` | 18.1.0 | Wearable | 🟢 minor lag |
| `com.google.code.gson:gson` | 2.10.1 | JSON | 🟢 (but version drift with shared) |
| `com.firebaseui:firebase-ui-auth` | 8.0.2 | Firebase Auth UI | 🔴 project archived 2024 |
| `com.google.firebase:firebase-bom` | 32.3.1 | Firebase BOM | 🟡 33.x available |
| `com.google.firebase:firebase-analytics-ktx` | (BOM) | Analytics | 🟢 |
| `com.google.firebase:firebase-auth-ktx` | (BOM) | Auth | 🟢 |
| `com.google.firebase:firebase-crashlytics-ktx` | (BOM) | Crashlytics | 🟢 |
| `com.google.firebase:firebase-firestore-ktx` | (BOM) | Firestore | 🟢 |
| `com.google.android.material:material` | 1.13.0 | Material 3 components | 🟢 |
| `androidx.dynamicanimation:dynamicanimation` | 1.0.0 | Spring physics | 🟢 |
| `androidx.camera:camera-core` | 1.3.1 | CameraX | 🟡 1.4+ available |
| `androidx.camera:camera-camera2` | 1.3.1 | | 🟡 |
| `androidx.camera:camera-lifecycle` | 1.3.1 | | 🟡 |
| `androidx.camera:camera-view` | 1.3.1 | | 🟡 |
| `com.google.mlkit:face-detection` | 16.1.6 | ML Kit face | 🟢 |
| `com.jakewharton.rxbinding3:rxbinding(-recyclerview/material/appcompat)` | 3.1.0 | RxBinding | 🟡 unmaintained but stable |
| `com.jakewharton.rxrelay3:rxrelay` | 3.0.1 | RxRelay | 🟢 |
| `com.squareup.picasso:picasso` | **2.8** | Image loading | 🟠 unmaintained; redundant with Coil (ISSUE in `09-`) |
| `com.squareup.leakcanary:leakcanary-android` (debug) | 2.12 | Leak detection | 🟢 |
| `io.reactivex.rxjava3:rxjava` | 3.1.8 | Rx | 🟢 |
| `io.reactivex.rxjava3:rxandroid` | 3.0.2 | Rx + Android | 🟢 |
| `io.realm.kotlin:library-base` | 2.3.0 | Realm | 🟢 |
| `org.jetbrains.kotlin:kotlin-stdlib-jdk8` | 2.0.20 | Kotlin stdlib | 🟢 |
| `org.jetbrains.kotlinx:kotlinx-coroutines-rx3` | 1.7.3 | Rx ↔ coroutines | 🟡 1.10+ available |
| `com.android.tools:desugar_jdk_libs` | 2.0.3 | Java 8+ desugaring | 🟢 |
| `junit:junit` (test) | 4.13.2 | JUnit | 🟢 |
| `androidx.test:runner` (androidTest) | 1.5.2 | | 🟡 1.6 available |
| `androidx.test.espresso:espresso-core` (androidTest) | 3.5.1 | | 🟡 3.6 available |

</details>

## Wear-only dependencies

| GAV | Version | Status |
|-----|---------|--------|
| `androidx.wear:wear` | 1.3.0 | 🟢 |
| `com.google.android.support:wearable` | 2.9.0 | 🟠 legacy, redundant with `androidx.wear:wear` |
| `com.google.android.wearable:wearable` (compileOnly) | 2.9.0 | 🟢 system-provided |
| `androidx.wear.compose:compose-material` | 1.4.1 | 🟢 (but unused — ISSUE-016) |
| `androidx.wear.compose:compose-foundation` | 1.4.1 | 🟢 (but unused) |
| `androidx.compose.ui:ui-tooling-preview` | 1.7.8 | 🟢 (unused) |
| `androidx.compose.ui:ui-tooling` (debug) | 1.7.8 | 🟢 |
| `androidx.activity:activity-ktx` | 1.7.2 | 🟡 mobile uses 1.9.3 — version skew between modules |
| `androidx.core:core-ktx` | 1.10.1 | 🟢 |
| `androidx.constraintlayout:constraintlayout` | 2.1.4 | 🟢 |
| `com.google.android.gms:play-services-wearable` | 18.1.0 | 🟢 |
| `com.google.code.gson:gson` | 2.10.1 | 🟡 mobile pulls 2.13.1 transitively elsewhere — confirm |

🟠 **Wear-mobile version skew**:
- `activity-ktx`: 1.7.2 (wear) vs 1.9.3 (mobile)
- `core-ktx`: 1.10.1 (both — match) ✅
- `gson`: 2.10.1 (wear) vs 2.10.1 declared on mobile (also match)

## Shared module

`shared/build.gradle` is a thin Android library; only declares the Gson dep. No version skew within
`:shared`. ✅

## Outdated majors / archived packages

| Package | Status | Action |
|---------|--------|--------|
| `com.firebaseui:firebase-ui-auth` 8.0.2 | 🔴 archived | Migrate to FirebaseAuth + Credential Manager |
| `net.zetetic:android-database-sqlcipher` 4.5.4 | 🔴 superseded by `net.zetetic:sqlcipher-android` 4.6+ | Upgrade artifact name; fixes `Aligned16KB` |
| `com.squareup.picasso` 2.8 | 🟠 unmaintained since 2021 | Replace with Coil |
| `com.google.android.support:wearable` 2.9.0 | 🟠 legacy in favour of `androidx.wear:wear` | Audit usage and drop |
| `com.jakewharton.rxbinding3` 3.1.0 | 🟡 unmaintained but stable | Acceptable until RxJava is fully replaced by coroutines |
| `androidx.security:security-crypto` 1.1.0-alpha06 | 🟠 alpha in production | Wait for stable, or vendor the small wrapper |

## Compose / Kotlin / AGP alignment

- Kotlin **2.0.20** ↔ Compose Compiler plugin `org.jetbrains.kotlin.plugin.compose:2.0.20` ✅
- Compose BOM **2025.05.01** ↔ Wear Compose **1.4.1** — 🟢 compatible.
- AGP **8.13.0** ↔ Gradle **8.13** — 🟢.
- KSP **2.0.20-1.0.25** ↔ Kotlin 2.0.20 — 🟢.

✅ Excellent alignment overall.

## License compatibility

Project license: **GPLv3** (`LICENSE`).

Sample of dependency licenses:
- AndroidX, Material Components, Compose, CameraX → Apache 2.0 ✅ compatible
- Realm Kotlin → Apache 2.0 ✅
- SQLCipher → BSD-style ✅
- Firebase Android SDKs → Apache 2.0 ✅
- Gson → Apache 2.0 ✅
- RxJava 3 → Apache 2.0 ✅
- Coil 3 → Apache 2.0 ✅
- Picasso → Apache 2.0 ✅
- ML Kit Face Detection → Google Privacy Policy + Apache 2.0 client lib ✅

🟢 No GPL-incompatible runtime dependencies detected.

## Lockfile / dependency verification

- ❌ **No `gradle/dependency-locking.gradle.kts`** or `gradle.lockfile` files.
- ❌ **No `gradle/verification-metadata.xml`**.
- 🟢 Gradle wrapper is validated in CI (`gradle/wrapper-validation-action@v3`).

🟡 For a privacy-sensitive app, dependency verification (`./gradlew --write-verification-metadata
sha256`) and lockfiles would meaningfully reduce supply-chain risk.

## Renovate / Dependabot configuration

- ✅ `.github/dependabot.yml` exists.
- 🟡 Reviewer is `TransTracks` (upstream organization) — see ISSUE-021. Dependabot PRs likely go
  unreviewed because no one in that org will see them.
- ✅ Schedule appears reasonable (weekly).

## Summary recommendations

| Pri | Action |
|----:|--------|
| 1 🔴 | Migrate `firebase-ui-auth` → FirebaseAuth + Credential Manager (archived dep) |
| 2 🔴 | Upgrade SQLCipher to `net.zetetic:sqlcipher-android:4.6+` (16 KB alignment) |
| 3 🟠 | Replace Picasso with Coil 3 |
| 4 🟠 | Drop `com.google.android.support:wearable` once `androidx.wear` is verified to cover all uses |
| 5 🟡 | Adopt a Gradle version catalog (`libs.versions.toml`) — eliminates version skew between modules |
| 6 🟡 | Bump Java/Kotlin target to 17 (matches AGP recommendation) |
| 7 🟡 | Bump Firebase BOM to 33.x, Play Services to current |
| 8 🟡 | Adopt dependency locking + verification metadata |
| 9 🟡 | Fix Dependabot reviewer (ISSUE-021) |
