# 01 — Monorepo Overview

## 1. Gradle setup

| Aspect | Value | Evidence |
|--------|-------|----------|
| Root build file | Groovy DSL | `build.gradle:1-37` |
| Settings file | Groovy DSL, 1 line, 3 modules | `settings.gradle:1` |
| Version catalog (`libs.versions.toml`) | ❌ Not present | `gradle/` only contains the wrapper |
| `buildSrc/` | ❌ Not present | — |
| Convention plugins | ❌ Not present (each module declares its own plugins inline) | `mobile/build.gradle:1-13`, `wear/build.gradle:1-5`, `shared/build.gradle:1-5` |
| `gradle.properties` flags | `org.gradle.parallel=true`, `org.gradle.caching=true`, `android.useAndroidX=true`, `android.enableJetifier=true`, `android.nonTransitiveRClass=false`, `android.nonFinalResIds=false` | `gradle.properties:9-19` |
| Dependency-lock files | ❌ None (no `gradle.lockfile` / `verification-metadata.xml`) | — |
| Wrapper validation | ✅ `gradle/wrapper-validation-action@v3` in CI | `.github/workflows/ci.yml:43-51` |
| JVM args | `-Xmx1536m` | `gradle.properties:15` |

🟡 **Concerns**:
- Dependency versions are scattered across `build.gradle` (root) and the per-module files using `ext` properties (`kotlin_version`, `nav_version`, `realm_version`). A `libs.versions.toml` catalog is the modern pattern and would remove the duplication that already exists for AndroidX Activity (`1.9.3` in `:mobile`, `1.7.2` in `:wear`) and Compose UI (`2025.05.01` BOM in `:mobile`, hard-coded `1.7.8` in `:wear`).
- `android.enableJetifier=true` is on. Jetifier is rarely necessary on AGP 8 and slows builds noticeably; should be flipped off and verified.

## 2. Module graph

```
                    ┌────────────────┐
                    │   :shared      │   (com.android.library)
                    │   ~170 LOC     │
                    │   Gson + GMS   │
                    │   wearable     │
                    └────▲─────▲─────┘
                         │     │
        implementation   │     │  implementation
                         │     │
              ┌──────────┴─┐ ┌─┴──────────────┐
              │  :mobile   │ │   :wear        │
              │ (handheld) │ │ (companion)    │
              │ ~19,914 LOC│ │ ~880 LOC       │
              │ Kotlin     │ │ Kotlin         │
              │ minSdk 21  │ │ minSdk 30      │
              └────────────┘ └────────────────┘
```

- `:mobile` and `:wear` are independent `com.android.application`s; neither depends on the other.
- `:shared` is the only common code path. **No `:domain`, `:data`, `:ui`, `:core` or feature modules.**
- The mobile app's "wear" code lives in `mobile/src/main/java/com/shelbeely/opentransition/wear/` (single file: `MobileWearableListenerService.kt`) and never references `:wear` directly — only `:shared`.

## 3. SDK / toolchain matrix

| Module | minSdk | targetSdk | compileSdk | Java source/target | Kotlin jvmTarget |
|--------|------:|----------:|-----------:|--------------------|------------------|
| `:mobile` | 21 | 36 | 36 | 1.8 / 1.8 (with `coreLibraryDesugaring`) | 1.8 |
| `:wear`   | 30 | 36 | 36 | 1.8 / 1.8 (no desugar) | 1.8 |
| `:shared` | 21 | 36 | 36 | 1.8 / 1.8 (no desugar) | 1.8 |

Toolchain pins (`build.gradle:1-19`):

| Tool | Version |
|------|---------|
| Android Gradle Plugin | **8.13.0** |
| Kotlin | **2.0.20** |
| Compose Compiler plugin | `org.jetbrains.kotlin.plugin.compose:2.0.20` |
| KSP | `2.0.20-1.0.25` (`mobile/build.gradle:9`) |
| Realm Kotlin SDK | **2.3.0** |
| Navigation (Safe Args) | **2.8.5** |
| Firebase Crashlytics plugin | 2.9.9 |
| Google Services plugin | 4.4.0 |
| Gradle wrapper | (validated in CI; explicit version in `gradle/wrapper/gradle-wrapper.properties`) |

🟡 **Concerns**:
- JVM target is **1.8** across all modules but `compileSdk = 36` (Android 14+). AGP 8 best practice is JVM 17. Bumping is a one-line per-module change and avoids future toolchain headaches.
- `:mobile` enables `coreLibraryDesugaring`; `:shared` does not but compiles `java.time.LocalDate` users (e.g. `MilestoneData`'s `Long` epoch wrapper avoids this — fine — but if `:shared` ever gains `java.time.*` it will silently break on API < 26).

## 4. Package managers / dependency resolution

- **Single resolution strategy:** plain `repositories { google(); mavenCentral() }` in `allprojects {}` (`build.gradle:26-31`). No `RepositoriesMode.FAIL_ON_PROJECT_REPOS` enforcement in `settings.gradle`.
- **No version catalog**, so AGP cannot enforce alignment.
- **No dependency locking** — every CI build resolves fresh from the network.
- **Renovate**: not configured. **Dependabot**: configured for Gradle, weekly, but reviewer is `TransTracks` — likely a stale upstream value (`.github/dependabot.yml:11-13`). 🟡

## 5. Directory structure (with annotations)

```
OpenTransition/
├── build.gradle                  # root build script (versions live here as ext.*)
├── settings.gradle               # `include ':mobile', ':wear', ':shared'`
├── gradle.properties             # parallel/caching/jetifier toggles
├── gradle/wrapper/               # gradle wrapper jar+properties
├── secrets.properties.example    # AdMob test IDs + CODE_SALT (committed)
├── secrets.properties            # gitignored; stub created by CI
├── local.properties              # gitignored; sdk.dir
├── keys/
│   └── debug-keystore.jks        # committed debug keystore (intentional)
│                                 # release keystore is injected via CI secret KEYSTORE_64
├── mobile/                       # handheld Android app
│   ├── build.gradle              # 240 lines, declares all mobile deps
│   ├── google-services.json      # gitignored; stub from CI
│   ├── proguard-rules.pro        # 30 lines, mostly Realm + Picasso keep rules
│   └── src/
│       ├── main/AndroidManifest.xml
│       ├── main/java/com/shelbeely/opentransition/
│       │   ├── TransTracksApp.kt           # Application; SP-migration on upgrade
│       │   ├── BuildConfig (generated)
│       │   ├── data/                       # Realm models (Photo, Milestone, AudioAnalysis) + FileProvider
│       │   ├── database/                   # Room + SQLCipher (largely unused at runtime)
│       │   │   ├── AppDatabase.kt
│       │   │   ├── DatabaseManager.kt
│       │   │   ├── KeystoreManager.kt
│       │   │   ├── room/dao/{Photo,Milestone,AudioAnalysis}Dao.kt
│       │   │   ├── room/entities/...Entity.kt
│       │   │   └── migration/{RealmToRoomMigration,RealmBackupImporter}.kt
│       │   ├── domain/                     # plain manager+RxRelay "domains" (not Clean Architecture)
│       │   ├── background/                 # Camera/CameraX/StoragePermission handlers, FaceDetection
│       │   ├── ui/
│       │   │   ├── MainActivity.kt          # 552 LOC
│       │   │   ├── home/   gallery/   selectphoto/ ...
│       │   │   ├── settings/                # 961-LOC SettingsFragment (god-object)
│       │   │   ├── theme/                   # Compose theme, motion tokens
│       │   │   └── widget/                  # Custom Views (waveform, formant chart, etc.)
│       │   ├── util/                        # 35+ utility files; SettingsManager singleton
│       │   └── wear/MobileWearableListenerService.kt  # 207 LOC; partly TODOs
│       └── main/res/
│           ├── layout/   menu/   navigation/main_nav.xml
│           ├── values/{strings,colors,dimens,styles,attrs,...}.xml
│           ├── drawable/   font/   anim/   xml/{provider_paths,backup_descriptor,data_extraction_rules}.xml
│           └── values-v31/themes.xml        # Material You overrides
├── wear/                          # Wear OS companion
│   ├── build.gradle               # declares Compose but only XML Views are rendered
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml    # standalone=false; no <wear.xml> capability resource
│       ├── java/com/shelbeely/opentransition/wear/
│       │   ├── MainActivity.kt              # 236 LOC, plain Activity + findViewById
│       │   ├── CameraControlActivity.kt
│       │   ├── AudioRecordActivity.kt
│       │   ├── WearableListenerService.kt   # mostly empty bodies
│       │   └── theme/WearTheme.kt           # Compose theme — never invoked anywhere
│       └── res/{layout,drawable,mipmap-anydpi-v26,values}/
├── shared/
│   ├── build.gradle
│   └── src/main/java/com/shelbeely/opentransition/shared/
│       ├── WearableConstants.kt              # paths/keys/capabilities
│       ├── models/MilestoneData.kt           # Parcelable; sole shared model
│       └── util/WearableHelper.kt            # Gson-based send/receive helpers
├── docs/                                    # MkDocs source for shelbeely.github.io site
├── mkdocs.yml
├── ARCHITECTURE.md  MONOREPO.md  WEAR_APP_FEATURES.md  ENCRYPTED_DATABASE.md  LOGO_REPLACEMENT_GUIDE.md  README.md
├── screenshots/                             # 40 PNGs, refreshed by `ui-screenshots.yml`
├── scripts/test_opentransition.py
└── .github/
    ├── workflows/{ci,pr-debug,build-release,copilot-debug-build,copilot-setup-steps,
    │              cleanup-caches,deploy-docs,ui-screenshots,boycott-check}.yml
    ├── actions/android-setup/               # composite action used by all workflows
    ├── dependabot.yml                       # weekly; reviewer = "TransTracks" (stale)
    ├── copilot-instructions.md
    └── skills/                              # Anthropic skill bundle (pdf/docx/pptx/xlsx)
```

## 6. Lines of code per module

Counts include only files actually compiled into the APK/AAR (`src/main/java`, `src/main/res`).

| Module | Kotlin files | Kotlin LOC | XML res files | XML res LOC | Test (Kotlin) LOC |
|--------|-------------:|-----------:|--------------:|------------:|------------------:|
| `:mobile` | 144 | **19,914** | ~80 | ~2,584 | ~600 (10 unit + 1 instrumented) |
| `:wear`   | 5   | **881**    | ~10 | ~397 | 0 |
| `:shared` | 3   | **168**    | 0 | 0 | 0 |
| **Total** | **152** | **20,963** | **~90** | **~2,981** | **~600** |

🔴 No Java files in production code. ✅
🟡 No Compose `.kt` count in `:wear` because Compose is declared but unused.
🟡 Tests only cover serialization, simple utilities, and one example instrumented class — see `12-testing-and-ci.md`.

## 7. Largest files / god-object candidates

Top 10 by LOC under `mobile/src/main/java`:

| LOC | File | Notes |
|----:|------|-------|
| **961** | `ui/settings/SettingsFragment.kt` | 🔴 god-object; mixes UI + IO + migration + dialog flows. |
| **602** | `ui/settings/SettingsScreen.kt` | Compose surface for SettingsFragment. |
| **588** | `util/settings/SettingsManager.kt` | Top-level `object` singleton; storage + Firebase sync + RxRelays. |
| **552** | `ui/MainActivity.kt` | Hosts navigation, handles intents, file imports, AdMob consent, keystore changes. |
| **465** | `ui/gallery/GalleryAdapter.kt` | RecyclerView adapter that opens its own Realm + does deletion. |
| **322** | `ui/settings/SettingsConflictDialog.kt` | Conflict-resolution dialog for Firebase sync. |
| **319** | `ui/home/HomeGalleryAdapter.kt` | Same Realm pattern as GalleryAdapter. |
| **300** | `ui/home/HomeFragment.kt` | |
| **295** | `ui/gallery/GalleryFragment.kt` | |
| **294** | `domain/SettingsDomain.kt` | RxRelay-based "domain" — actually mostly a controller. |

In `:wear`, the largest file is `AudioRecordActivity.kt` at 249 LOC.
In `:shared`, the largest is `WearableHelper.kt` at 80 LOC.

🟠 **Action item:** `SettingsFragment.kt` is the single most-edited surface in the repo and is the
biggest concentration of risk. Splitting it (e.g. into per-section sub-fragments or per-section composables
behind a single VM) is high-ROI work. (See `06-code-quality-report.md` and `16-prioritized-action-plan.md`.)
