# 02 — Architecture Assessment

## 1. Inferred architecture pattern

The mobile app is a **layered MVC-with-RxJava-relays** design rather than any modern named pattern. There
is no use of Hilt, Dagger, Koin, or any DI framework. There is no `ViewModel` (AAC) or `StateFlow` /
`LiveData` for state holders.

The de-facto layering is:

```
   ┌─────────────────────────────────────────────────┐
   │  ui/ (Fragments + ComposeView interop + Adapters)│
   └────────────────┬────────────────┬───────────────┘
                    │                │
                    │ subscribes to  │ accepts(Action)
                    ▼                ▼
   ┌─────────────────────────────────────────────────┐
   │ domain/ (Manager objects + RxRelay PublishRelay) │
   │   • DomainManager (constructed in Application)  │
   │   • SettingsDomain, HomeDomain, etc.            │
   └────────────────┬────────────────────────────────┘
                    │
                    ▼
   ┌─────────────────────────────────────────────────┐
   │ data/ + util/ + database/                        │
   │   • Realm (still primary)                       │
   │   • Room+SQLCipher (declared, mostly unused)    │
   │   • Firebase Firestore (settings only)          │
   │   • SharedPreferences (PrefUtil + EncryptedSP)  │
   └─────────────────────────────────────────────────┘
```

This pattern is internally consistent and reasonable for a 2018-era Kotlin codebase. It is not
"Clean Architecture", and it does not follow the modern AAC `ViewModel` + `StateFlow` recipe.

| Layer | Implementation | Rating |
|-------|----------------|-------:|
| Presentation | `Fragment` + `View Binding` + `ComposeView` interop where Compose is used | 6 |
| State | `BehaviorSubject` / `PublishRelay` (RxRelay 3.x) | 6 |
| Business logic | "Domains" (`HomeDomain`, `SettingsDomain`, ...) — really plain managers | 5 |
| Data | Direct `Realm.openDefault()` calls from adapters/fragments (no repository layer) | 4 |

🟠 **Concerns**:
- Adapters open Realm instances directly (`GalleryAdapter.kt:47`, `HomeGalleryAdapter.kt:58`,
  `MilestonesAdapter.kt:38`). This couples the view layer to the persistence engine and makes the
  Realm→Room migration significantly harder than it should be.
- `MobileWearableListenerService.kt:172-188` opens its own `RealmConfiguration.Builder(...)` outside
  `DatabaseManager` — second example of the same anti-pattern, this time bypassing the encryption setting.

## 2. Dependency injection

| Aspect | Status | Evidence |
|--------|--------|----------|
| Framework | **None** | No `@Inject`, no `@HiltAndroidApp`, no Koin module anywhere. |
| Manual DI | Yes — `TransTracksApp.domainManager` is the root container; reads via `TransTracksApp.instance.domainManager.xxxDomain` | `mobile/.../TransTracksApp.kt:30`, `mobile/.../ui/MainActivity.kt:146`. |
| Service locator pattern | Heavy use — `SettingsManager` is a top-level `object` accessed from anywhere | `mobile/.../util/settings/SettingsManager.kt:35` |
| Consistency between `:mobile` and `:wear` | N/A — `:wear` has no DI either; clients are constructed inline in each `Activity.onCreate` | `wear/.../MainActivity.kt:59-61`. |

🟢 **Pro**: avoiding Hilt keeps build times down and avoids generated-code surprises.
🟠 **Con**: makes testing very hard. There is no seam to inject a fake `Realm`, fake `MessageClient`, or
fake `SettingsManager`. Visible in the test suite — only pure-Kotlin serialization/utility tests exist.

**Rating — DI consistency: 5 / 10** (consistent in style, but the pattern is fragile and untestable).

## 3. Module-boundary analysis — sharing vs duplication

**`:shared` content (3 files):**

| File | Purpose | Used by |
|------|---------|---------|
| `WearableConstants.kt` | All Data Layer paths/keys/capability strings | both `:mobile` & `:wear` ✅ |
| `models/MilestoneData.kt` | `Parcelable` cross-process model | both ✅ |
| `util/WearableHelper.kt` | `sendPhotoTrigger`, `sendSyncRequest`, `syncMilestones`, `parseMilestones` | both ✅ |

**Code that *should* be in `:shared` but is duplicated/orphaned:**

- 🟠 **Theme & colour palette**: Mobile's `Color.kt` defines `pinkColorPrimary = #FF85D4` etc.; Wear's
  `WearTheme.kt:27-40` repeats those exact hex values. Should be `:shared` constants. (`WearTheme.kt:27-85`
  vs `mobile/src/main/res/values/colors.xml:14-30`.)
- 🟠 **Gson instance**: created at `WearableHelper.kt:25` and again at `wear/MainActivity.kt:177` and
  again inside `MobileWearableListenerService` indirectly via `WearableHelper`. Three independent `Gson`
  instances; trivial RAM impact but indicates lack of a sharing convention.
- 🟠 **Audio file naming convention**: `AudioRecordActivity.kt:106` uses `"audio_yyyyMMdd_HHmmss.3gp"`;
  the mobile side reads it but doesn't validate. No shared filename helper.
- 🔴 **Capability strings declared in `:shared` but never actually registered.** The constants exist in
  `WearableConstants.kt` and are *consumed* by capability lookups, but neither app's Manifest declares
  the capability resource that the Wearable Data Layer needs to advertise them. Sharing a constant
  doesn't help if no one uses it correctly. (See `05-mobile-wear-integration.md`.)

**Rating — code-sharing effectiveness: 4 / 10**. The mechanism is right (a `:shared` library module);
the content is too thin and the most consequential constants are not actually plumbed through.

## 4. Navigation

| App | Mechanism | Evidence |
|-----|-----------|----------|
| `:mobile` | **Jetpack Navigation 2.8.5 with Safe Args** in XML (`main_nav.xml`); `NavHostFragment` hosted by `activity_main.xml`; `navigation-compose` is on the classpath but **never used** in source. | `mobile/build.gradle:162-165`, `mobile/src/main/res/navigation/main_nav.xml`, `mobile/src/main/java/com/shelbeely/opentransition/ui/MainActivity.kt:149-156`. |
| `:wear` | **Manual Activity-to-Activity `startActivity(Intent(...))`** — `MainActivity` → `CameraControlActivity` / `AudioRecordActivity`. No `SwipeDismissableNavHost`, no Navigation library at all. | `wear/.../MainActivity.kt:67-75`. |

🟠 **Concerns**:
- `androidx.navigation:navigation-compose:2.8.5` is declared but unused — dead dependency.
- The Wear app does not implement Wear OS's standard swipe-to-dismiss back navigation; users have to use
  the system back gesture only. This is a Wear OS UX violation.

**Rating — navigation consistency: 4 / 10** (two apps, two completely different approaches; one is missing
the platform's expected gesture).

## 5. State management

| Surface | Mechanism | Evidence |
|---------|-----------|---------|
| Mobile (legacy Fragments) | RxJava 3 `Observable`/`BehaviorSubject` from "domains"; `CompositeDisposable` per Fragment | `mobile/.../ui/MainActivity.kt:82, 234-241` |
| Mobile (Compose surfaces) | `setContent { ... }` inside ComposeView lambdas; state is plumbed via UI-state holders not `ViewModel` | e.g. `mobile/.../ui/recordaudio/RecordAudioView.kt:51` |
| Mobile settings observation | `themeUpdated`, `lockTypeUpdated`, `userSettingsUpdated` exposed as `Observable<...>` from `SettingsManager` | `mobile/.../util/settings/SettingsManager.kt:36-45` |
| Wear | None — bare `lateinit var` references in each Activity, mutated from listeners | `wear/.../MainActivity.kt:33-46` |

❌ No `ViewModel` (AAC) anywhere in the codebase.
❌ No `StateFlow` / `SharedFlow`.
❌ No `LiveData`.

This means none of the standard process-death/configuration-change protections from AAC `ViewModel`
apply. The Activity-level state in `:mobile` survives configuration changes only because rotation is
disabled (`android:screenOrientation="portrait"` + `configChanges="keyboardHidden|orientation|screenLayout"`
in `AndroidManifest.xml:51-55`), masking the issue.

**Rating — state management: 4 / 10**.

## 6. Concurrency model

| Mechanism | Where | Evidence |
|-----------|-------|----------|
| **RxJava 3** (primary) | All Fragments/Adapters; `CompositeDisposable` in `MainActivity` | `mobile/build.gradle:211-212`, `mobile/.../ui/MainActivity.kt:82` |
| **RxRelay 3** | All "domain" actions/state | `mobile/build.gradle:204` |
| **`rxbinding3`** | Some Fragments | `mobile/build.gradle:198-202` |
| **Coroutines** (limited) | Only in DB migration helpers and one settings flow | `mobile/.../database/migration/RealmToRoomMigration.kt:60`, `mobile/.../ui/settings/SettingsFragment.kt:900-916` |
| **`kotlinx-coroutines-rx3` bridge** | Available on classpath, not used in production code | `mobile/build.gradle:217` |
| **Wear** | `Thread`/`Handler` implicitly through `Activity` + Play Services `Task`s; no coroutines | `wear/.../AudioRecordActivity.kt:157-162` (postDelayed loop) |

**Scope hygiene findings:**
- 🟠 **`viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO)`** is used in `SettingsFragment.kt:900`
  inside fragments where `viewLifecycleOwner` may be torn down before `IO` completes. Switching to
  `lifecycleScope.launch { withContext(Dispatchers.IO) { ... } }` is safer.
- 🟠 The recursive `Handler`-style `durationText.postDelayed({ updateDuration() }, 1000)` loop in
  `wear/AudioRecordActivity.kt:157-162` is not cancelled in `onDestroy`/`onPause`. If the activity
  is finished mid-recording, the callback chain still posts.
- 🟢 Disposables are correctly added via `+=` operator and cleared in `MainActivity.onDestroy` /
  `onAttachedToWindow` lifecycle.
- ❌ No `SupervisorJob` or `CoroutineExceptionHandler` anywhere — when a coroutine throws (e.g. the
  ad-hoc `lifecycleScope.launch { ... }` blocks), it crashes the process.

**Rating — concurrency hygiene: 5 / 10**. Two paradigms coexist (Rx and coroutines) without a shared
convention; Wear concurrency is bare-bones; no error handler.

## 7. Compose vs Views status

| File group | Renderer | Notes |
|------------|----------|-------|
| `ui/MainActivity.kt`, all `Fragment`s | XML Views via `viewBinding` | Primary path |
| `ui/.../*Screen.kt` (~15 files) | Compose, hosted via `ComposeView` set inside `*Ui.kt` glue classes | Compose-only by name; many are still wrapped in legacy adapters |
| `ui/recordaudio/RecordAudioView.kt` | `ComposeView` inside an XML Fragment — **mixed** | Hybrid pattern |
| `ui/widget/*View.kt` (waveform, formant chart) | Plain custom Views with Canvas | Legacy |
| `:wear` activities | XML Views via `viewBinding`; `WearTheme.kt` exists but is **never invoked** | 🔴 dead code |

The Compose surface area is real (31 `@Composable`-containing files in `:mobile`) but each is wrapped in
fragment/adapter scaffolding rather than driving the screen — i.e. Compose is being used as a
"rich-cell" inside a RecyclerView/Fragment, not as the navigation host.

🟠 **Risk**: this is a partial migration mid-flight. There is no documentation of the target end-state
or which screen is next. This kind of half-migration tends to ossify.

## 8. Summary ratings

| Dimension | Score | Reasoning |
|-----------|------:|-----------|
| **Modularity** | 4 | Three modules, but `:shared` is too thin; no `:domain`, `:data`, `:ui` split. |
| **Separation of concerns** | 4 | Adapters open the database; `SettingsFragment` mixes UI/IO/migration. |
| **Scalability** | 4 | A new feature requires touching the singleton `SettingsManager` (588 LOC) or the 961-LOC `SettingsFragment`. Painful at scale. |
| **Testability** | 3 | No DI seams. Test suite is limited to pure utilities and serialization. |
| **Code-sharing effectiveness (mobile↔wear)** | 4 | Mechanism (`:shared`) is right; content (3 files) is too thin; capability strings declared in shared but never registered by either side. |
| **Architecture overall** | **4 / 10** | Functional layered design with three half-finished migrations creating duplication and confusion. |
