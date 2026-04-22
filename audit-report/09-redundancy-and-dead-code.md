# 09 — Redundancy and Dead Code

## 1. Duplicated logic between `:mobile` and `:wear`

| Concern | Mobile location | Wear location | Should live in |
|---------|----------------|---------------|----------------|
| Color palette hex codes | `mobile/src/main/res/values/colors.xml:14-30` | `wear/.../theme/WearTheme.kt:27-85` | `:shared` (constants) or shared resources |
| Gson instance | `shared/.../WearableHelper.kt:25` | constructed inline in `wear/.../MainActivity.kt:177` and indirectly via `WearableHelper` | `:shared` only (one instance) |
| Audio filename convention | `mobile/.../wear/MobileWearableListenerService.kt:160` reads `KEY_AUDIO_FILENAME` | `wear/.../AudioRecordActivity.kt:106` writes via SimpleDateFormat | `:shared` (formatter helper) |
| Date-key derivation (`yyyyMMdd`) | `mobile/.../util/LocalDates.kt` | `wear/.../AudioRecordActivity.kt:204` (SimpleDateFormat inline) | `:shared` |
| Photo type strings ("face"/"body") | Constants in `:shared/WearableConstants.kt:55-56` | Used both sides ✅ | already correct ✅ |
| MilestoneData ↔ Realm `Milestone` mapping | `mobile/.../wear/MobileWearableListenerService.kt` would do this if implemented; currently doesn't | n/a | `:shared` should define mapping helpers |

🟡 The duplication is small in volume (the Wear app is small overall) but every piece of duplication is
a future bug surface during refactors.

## 2. Unused classes / functions / resources

`grep`-driven static signal (final word should come from `./gradlew :mobile:lintDebug`):

### Likely unused

| Symbol | Where | Why suspect |
|--------|-------|-------------|
| `wear/.../theme/WearTheme.kt` (entire file) | wear | No `setContent { WearTheme { ... } }` anywhere |
| `wear/.../theme/Type.kt` | wear | Same; only used by the dead theme |
| `androidx.navigation:navigation-compose` (dep) | mobile | `grep -rn "rememberNavController\|composable(" mobile/src/main` → 0 hits (ISSUE-015) |
| `arch_version 2.2.0` `androidx.arch.core:core-testing` | mobile (test scope) | Used by 0 test classes (`grep -rn "InstantTaskExecutorRule"` → 0) |
| `androidx.lifecycle:lifecycle-runtime-compose:2.8.7` | mobile | `grep -rn "collectAsStateWithLifecycle"` → 0 hits |
| `Photo.Companion.fromJson` legacy reader | mobile | Only called by the .ttbackup importer; arguably needed for back-compat ✅ keep |
| `RealmConfigurations.kt` | mobile | Only used during migration. Keep for compat. |
| `domain/UserDomain.kt` (if present) | mobile | Need to confirm with Lint — uses Firebase Auth state |

### Definitely used (rule out for the cleanup pass)

`Photo`, `Milestone`, `AudioAnalysis` Realm models — actively used by the UI. Cannot delete until ISSUE-004
is resolved.

## 3. Orphaned modules / source sets

- `:mobile/src/test/java/.../ExampleUnitTest.kt` — boilerplate, can be deleted (`grep -rn "ExampleUnitTest"` → 1 hit, the file itself).
- `:mobile/src/androidTest/java/.../ExampleInstrumentedTest.kt` — same.
- No orphan modules; `settings.gradle` lists exactly the three that exist.

## 4. Overlapping utilities

| Function | Implementations |
|----------|-----------------|
| Date formatting | `mobile/.../util/LocalDates.kt` *and* multiple `SimpleDateFormat` instantiations across `wear/.../AudioRecordActivity.kt`, `mobile/.../wear/MobileWearableListenerService.kt`, `mobile/.../util/Files.kt`. 🟡 |
| Realm config | `mobile/.../util/RealmConfigurations.kt` *and* `mobile/.../wear/MobileWearableListenerService.kt:172-188` (inline). 🟠 (also a security issue — see ISSUE-013) |
| File copy / save | `mobile/.../util/FileUtil.kt` *and* the inline `audioFile.writeBytes(audioData)` in `MobileWearableListenerService.kt:188`. 🟢 |
| Schedulers | `mobile/.../util/RxSchedulers.kt` (with named instances) but `Schedulers.io()` is used directly in some places (`grep -rn "Schedulers.io()" mobile/src/main` ≥ 5). 🟡 |

## 5. Redundant dependencies

| Functionality | Library 1 | Library 2 | Verdict |
|--------------|-----------|-----------|---------|
| Image loading | **Picasso 2.8** (`mobile/build.gradle:207`) | **Coil 3.1.0** (`mobile/build.gradle:127-128`) | 🟠 Two image libs at once. |
| JSON | **Gson 2.10.1 / 2.13.1** | (none, kotlinx.serialization not used) | ✅ One lib, but version drift between modules. |
| Async | **RxJava 3** (`mobile/build.gradle:211-212`) | **kotlinx.coroutines** (`mobile/build.gradle:217`) | Coexistence is intentional (mid-migration). 🟡 |
| Persistence | **Realm 2.3.0** | **Room 2.6.1 + SQLCipher 4.5.4** | 🔴 Both writing in production. (ISSUE-004) |
| Wear support | **`com.google.android.support:wearable:2.9.0` (legacy)** | **`androidx.wear:wear:1.3.0` (modern)** | 🟠 Pick one. |
| Wearable communication | **`play-services-wearable:18.1.0`** | (none) | ✅ |

🔴 The **two-database situation** is the worst overlap by far — it's not just dependency bloat, it's
correctness/security risk (ISSUE-004, ISSUE-013).

🟠 **Picasso + Coil**: pick one. Coil is the modern Kotlin/Compose-native answer. Picasso has not had a
release since 2021.

## 6. Legacy code paths still shipping

| Path | Notes |
|------|-------|
| All XML `Fragment`s + Navigation graph | Shipping; mid-migration to Compose. The Fragments themselves are not "legacy" — they're the primary path. The intent appears to be Compose, but until that's done they're production. |
| `Photo : RealmObject` + Realm DAOs | Shipping (primary write path). |
| `PhotoEntity : RoomEntity` + Room DAOs | Shipping (write-only via migration utilities). |
| Wear Compose theme | Not shipping (no caller); pure dead weight. |
| `BottomSheetDialogFragment`-based dialogs in `mobile/.../ui/.../*DialogFragment.kt` | Shipping. Material widgets, not legacy. |
| `com.google.android.support:wearable:2.9.0` | Shipping. The actual replacement (`androidx.wear:wear`) is also shipped — both go in. |
| `androidx.security:security-crypto:1.1.0-alpha06` | Alpha lib in production code path. Acceptable for storing the SQLCipher passphrase but worth tracking. |

## 7. Quantitative summary

| Category | Items | Severity |
|----------|------:|---------|
| Dead Kotlin code (high confidence) | ≥ 2 files (`WearTheme.kt`, `Type.kt`) + 1 test boilerplate × 2 modules | 🟡 |
| Dead dependencies | ≥ 2 (`navigation-compose`, `lifecycle-runtime-compose`) | 🟡 |
| Redundant dependency pairs | 3 (Picasso+Coil, legacy+modern wear, Realm+Room) | 🔴 (Realm+Room) / 🟠 (others) |
| Duplicated logic blocks | ≥ 4 (palette, Gson, dates, Realm config) | 🟡 |
| TODO-class comments (from doc 08) | 39 | mixed |

## 8. Recommended cleanup ordering

1. 🔴 Resolve the Realm/Room duplication (ISSUE-004) — biggest win for both correctness and code volume.
2. 🟠 Pick Picasso *or* Coil; remove the other.
3. 🟠 Remove either `com.google.android.support:wearable:2.9.0` *or* `androidx.wear:wear` after auditing
   what each is doing on the wear side.
4. 🟡 Delete `WearTheme.kt`/`Type.kt` (or invoke them and start the wear Compose migration).
5. 🟡 Delete `navigation-compose` and `lifecycle-runtime-compose` dependencies.
6. 🟡 Move the colour palette + date helpers to `:shared`.
7. 🟢 Delete `ExampleUnitTest.kt` / `ExampleInstrumentedTest.kt`.
