# Dependencies

All dependency versions are defined in `mobile/build.gradle` and `build.gradle` (root).

## Core

| Dependency | Version | Notes |
|-----------|---------|-------|
| Kotlin | 2.0.20 | |
| Android Gradle Plugin | 8.13.0 | |
| KSP | 2.0.20-1.0.25 | Annotation processing (replaces KAPT for new processors) |

## AndroidX

| Dependency | Version |
|-----------|---------|
| `androidx.activity:activity-ktx` | 1.7.2 |
| `androidx.appcompat:appcompat` | 1.6.1 |
| `androidx.constraintlayout:constraintlayout` | 2.1.4 |
| `androidx.core:core-ktx` | 1.10.1 |
| `androidx.lifecycle:lifecycle-viewmodel-ktx` | 2.6.2 |
| `androidx.recyclerview:recyclerview` | 1.3.2 |
| `androidx.exifinterface:exifinterface` | 1.3.6 |
| `androidx.biometric:biometric` | 1.1.0 |
| `androidx.security:security-crypto` | 1.1.0-alpha06 |
| `androidx.dynamicanimation:dynamicanimation` | 1.0.0 |

## Navigation

| Dependency | Version |
|-----------|---------|
| `androidx.navigation:navigation-fragment-ktx` | 2.8.5 (`$nav_version`) |
| `androidx.navigation:navigation-ui-ktx` | 2.8.5 |
| SafeArgs Gradle plugin | 2.8.5 |

## Database

| Dependency | Version | Notes |
|-----------|---------|-------|
| `androidx.room:room-runtime` | 2.6.1 | Primary database |
| `androidx.room:room-ktx` | 2.6.1 | Coroutines support |
| `net.zetetic:android-database-sqlcipher` | 4.5.4 | Optional AES-256 encryption |
| `androidx.sqlite:sqlite` | 2.4.0 | SQLite layer for SQLCipher |
| `io.realm.kotlin:library-base` | 2.3.0 (`$realm_version`) | Backwards compat — TransTracks import only |

## Camera & ML

| Dependency | Version |
|-----------|---------|
| `androidx.camera:camera-core` | 1.3.1 |
| `androidx.camera:camera-camera2` | 1.3.1 |
| `androidx.camera:camera-lifecycle` | 1.3.1 |
| `androidx.camera:camera-view` | 1.3.1 |
| `com.google.mlkit:face-detection` | 16.1.6 |

## Reactive

| Dependency | Version |
|-----------|---------|
| `io.reactivex.rxjava3:rxjava` | 3.1.8 |
| `io.reactivex.rxjava3:rxandroid` | 3.0.2 |
| `com.jakewharton.rxrelay3:rxrelay` | 3.0.1 |
| RxBinding (`rxbinding`, `rxbinding-recyclerview`, etc.) | 3.1.0 |
| `org.jetbrains.kotlinx:kotlinx-coroutines-rx3` | 1.7.3 |

## Firebase

| Dependency | Notes |
|-----------|-------|
| Firebase BOM | 32.3.1 |
| `firebase-analytics-ktx` | |
| `firebase-auth-ktx` | |
| `firebase-crashlytics-ktx` | Disabled in debug builds |
| `firebase-firestore-ktx` | |
| `com.firebaseui:firebase-ui-auth` | 8.0.2 |

## Google Play Services

| Dependency | Version |
|-----------|---------|
| `play-services-ads` | 22.4.0 |
| `play-services-auth` | 20.7.0 |
| `play-services-wearable` | 18.1.0 |

## UI

| Dependency | Version |
|-----------|---------|
| `com.google.android.material:material` | 1.13.0 |
| `com.squareup.picasso:picasso` | 2.8 |

## Utilities

| Dependency | Version |
|-----------|---------|
| `com.google.code.gson:gson` | 2.10.1 |
| `com.android.tools:desugar_jdk_libs` | 2.0.3 |

## Debug Only

| Dependency | Version |
|-----------|---------|
| `com.squareup.leakcanary:leakcanary-android` | 2.12 |

## Test

| Dependency | Version |
|-----------|---------|
| `junit:junit` | 4.13.2 |
| `androidx.test:runner` | 1.5.2 |
| `androidx.test.espresso:espresso-core` | 3.5.1 |
| `androidx.arch.core:core-testing` | 2.2.0 |
| `androidx.lifecycle:lifecycle-runtime-testing` | 2.6.2 |
| `androidx.navigation:navigation-testing` | 2.8.5 |
