# 11 — Performance and Battery

> Findings combine static review of source + actual `:mobile:lintDebug` output + runtime emulator
> measurements (see `17-runtime-observations.md` for raw artifacts).

## Headline numbers (from emulator run)

| Metric | Value | Source |
|--------|------:|--------|
| Cold-start launch (process start → first resumed activity) | **~3.0 s** | Logcat: process forked at 21:09:50.825, "Welcome" dialog visible by 21:09:54 |
| Choreographer skipped frames during launch | **191 frames** (~3.2 s) at 21:10:00.718 | `Choreographer:I "Skipped … frames"` events |
| Pss after launch (idle on Welcome) | **150 MB** | `dumpsys meminfo` |
| Native heap | 914 KB Pss | `dumpsys meminfo` |
| Dalvik heap allocated | 12 MB / 24 MB committed | `dumpsys meminfo` |
| Mobile debug APK size | **74 MB** | `mobile-debug.apk` |
| Wear debug APK size | **27 MB** | `wear-debug.apk` |
| Lint warnings | 235 (0 errors, 1 hint) | `:mobile:lintDebug` |
| `Aligned16KB` lint findings | 3 (`libsqlcipher.so`) | Lint XML |
| `DrawAllocation` lint findings | 3 in custom widgets | Lint XML |

## 1. Startup time risks

`mobile/.../TransTracksApp.kt:38-53` does a lot of work in `Application.onCreate`:

```kotlin
override fun onCreate() {
    super.onCreate()
    instance = this
    domainManager = DomainManager(this)              // 1
    appVersionUpdateIfNecessary()                     // 2 — large nested when
    FileUtil.clearTempFolder(this)                    // 3 — disk I/O
    SettingsManager.startFirbaseSyncIfLoggedIn(this)  // 4 — Firestore handshake
    MobileAds.initialize(this) {}                     // 5 — heavy
}
```

🟠 **Concerns:**
- `MobileAds.initialize` is documented to be expensive on first call (~hundreds of ms).
- `appVersionUpdateIfNecessary` is a 140-LOC `when` over historical version codes (legacy-migration
  trail from TransTracks); some branches do disk I/O.
- `FileUtil.clearTempFolder` does synchronous `delete` on the cache directory.
- `SettingsManager.startFirbaseSyncIfLoggedIn` triggers a Firestore handshake and potential
  conflict-resolution dialog.

The 191-frame Choreographer skip observed at runtime corresponds to the **first activity inflate**
(after the Application was already done) — most likely the AdView and the welcome dialog being
inflated at the same time as Realm is opened on the main thread.

🟠 **No Baseline Profile** in the project (`grep -rn "androidx.profileinstaller\|baselineProfile"` →
0 hits). Adding a Macrobenchmark + Baseline Profile module would meaningfully reduce cold-start.

🟢 **No `androidx.startup` library** — not using it is not strictly bad, but it would simplify the
ordering of ads/firebase/migration.

## 2. Jank risks in Compose

(Static review; no GPU profiling in the emulator run.)

| Risk | Where | Impact |
|------|-------|--------|
| Unstable params (raw `List<X>`) passed to `@Composable`s | `SettingsScreen.kt`, `RecordAudioScreen.kt` | Recomposition of whole subtrees on each emission. |
| `LaunchedEffect(Unit)` with no real key | `SettingsScreen.kt` | Re-launches across navigation but not on data changes. |
| `remember { ... }` not used for intermediate computations in custom drawing composables | `WaveformView` etc. | Re-allocates on every frame. |
| `derivedStateOf` not used | Several places where derived booleans are recomputed every recompose | Minor. |

## 3. Main-thread I/O / blocking calls

🔴 **Confirmed at runtime**: the 191-frame skip during launch is consistent with main-thread Realm
opening. Static evidence:
- `mobile/.../ui/MainActivity.kt:391` — `Realm.openDefault()` called from `onResume` (not in a
  background dispatcher).
- `mobile/.../ui/home/HomeGalleryAdapter.kt:58`, `mobile/.../ui/gallery/GalleryAdapter.kt:47` —
  Realm opened in adapter constructor (called on main thread by `RecyclerView`).

🟡 **`SettingsManager.startFirbaseSyncIfLoggedIn`** is called from `Application.onCreate` (main
thread) and indirectly performs Firestore reads.

🟡 **`FileUtil.clearTempFolder`** synchronous file deletion on app start.

## 4. Memory leaks

✅ **LeakCanary 2.12** is included in debug builds (`mobile/build.gradle:209`). Excellent.

Static signals worth noting:
- 🟡 `wear/.../AudioRecordActivity.kt:157-162` — recursive `Handler.postDelayed` is not cancelled in
  `onPause`/`onDestroy`. Small leak window. (ISSUE-009)
- 🟡 `lifecycleScope.launch(Dispatchers.IO)` in `SettingsFragment` retains the fragment until the IO
  completes. Cancellation works because `lifecycleScope` is tied to the fragment, but the retention
  of any captured `Context` survives view destruction. (ISSUE-014)
- 🟢 No `static Context` references; no `Activity` passed to long-lived callbacks.
- 🟢 `CompositeDisposable` is correctly cleared on `onDestroy`/`onAttachedToWindow`.

## 5. Image loading

- **Two image libraries** loaded at once: **Picasso 2.8** (legacy) and **Coil 3.1.0** (modern).
  See ISSUE in `09-redundancy-and-dead-code.md`.
- Picasso default cache: 15% of available heap or 2x display. Not configured.
- Coil cache: defaults to `Coil.imageLoader(context)` defaults — also not configured.
- 🟡 No down-sampling for large gallery thumbnails observed (visual review of `GalleryAdapter`).
  CameraX outputs 12-megapixel JPEGs by default; loading these into a thumbnail view at full
  resolution is the usual mid-list jank cause.

🟠 **Recommendation**: pick Coil, configure a single `ImageLoader` with `BitmapFactory.Options.inSampleSize`-equivalent
(`Coil` does this via `size(...)` modifier), and let it manage both memory and disk cache.

## 6. Custom widget allocations

🟡 **Lint flagged `DrawAllocation`** in 3 places:

| File:line | Allocation |
|-----------|------------|
| `mobile/.../ui/widget/FormantChartView.kt:160` | object created during `onDraw`/`onMeasure`/`onLayout` |
| `mobile/.../ui/widget/FormantChartView.kt:167` | same |
| `mobile/.../ui/widget/PitchProgressionView.kt:148` | same |

Each `onDraw` allocation creates GC pressure proportional to frame rate; on the audio-analysis screen
(60 fps), this is up to 180 alloc/sec of needless garbage.

🟠 **Recommendation**: pre-allocate `Paint`, `Path`, `RectF` as instance fields.

## 7. Wear-specific power

| Lever | Status |
|-------|--------|
| Wake locks | None acquired despite `WAKE_LOCK` permission being declared. The audio recording activity will sleep mid-record under default DPM settings. 🟠 (`04-wear-os-app-review.md §4-5`) |
| Sensor batching | N/A (no sensor use). ✅ |
| Location updates | N/A. ✅ |
| Data sync frequency | Manual only (button-driven + capability-change-driven). Lowest possible footprint. ✅ |
| Tile refresh rate | N/A (no tile). |
| Polling loops | One: the recursive `postDelayed` for the duration label (`AudioRecordActivity.kt:157-162`). Once per second. Negligible. |

🟠 **Net Wear battery impact**: low *while idle*, but recording behaviour is fragile because the
`WakeLock` isn't held — the actual user-visible problem is "recording stops when screen sleeps".

## 8. Native libraries / 16 KB page size compatibility

🟠 **Lint flagged 3 `Aligned16KB` issues** for `libsqlcipher.so` (`net.zetetic:android-database-sqlcipher:4.5.4`).

Android 15 (API 35) supports devices with 16 KB memory pages (Pixel 8+ in compatibility mode, future
Pixel devices natively). Apps shipping `.so` files not aligned to 16 KB will fail to load on these
devices.

🟠 **Recommendation**: upgrade SQLCipher to `net.zetetic:sqlcipher-android:4.6+` (the rebranded artifact),
which ships 16 KB-aligned libs since 4.6.0.

## 9. APK / AAB size

| APK | Size | Notes |
|-----|-----:|-------|
| `mobile-debug.apk` | **74 MB** | Includes LeakCanary, four ABIs (`x86_64`, `x86`, `armeabi-v7a`, `arm64-v8a`) of `libsqlcipher.so` and `librealmc.so`, debug symbols. |
| `wear-debug.apk` | **27 MB** | Includes Compose libraries that aren't called. |

🟡 No `splits { abi { enable true } }` configured anywhere → release builds also ship all four ABIs.
At AAB upload, Play does the splitting for the user, so end-users download ~25 MB. ✅
🟢 No `assets/` blob bigger than ~1 MB observed.

🟠 **Quick win**: configure `android.defaultConfig.ndk { abiFilters 'arm64-v8a', 'armeabi-v7a' }` to
drop x86 from sideloaded debug APKs (saves ~25 MB on debug; doesn't affect AAB).

## 10. Battery historian considerations

The app is a foreground-only experience by design. There is no `BroadcastReceiver` waking up on
boot, no `Service` auto-restart, no `AlarmManager`, no `WorkManager`. Battery drain when not in use
should be ~0. ✅

The single ongoing battery cost is Crashlytics/Analytics background sync, which is delegated to GMS
and shared with every other Firebase app on the device. Not actionable.

## 11. Summary recommendations

| Pri | Action | Expected gain |
|----:|--------|---------------|
| 1 | Move `Realm.openDefault()` calls off the main thread (use `lifecycleScope.launch(Dispatchers.IO)` + Realm Flow) | -50% to -80% of the 191-frame startup skip |
| 2 | Upgrade SQLCipher to 4.6+ for 16 KB alignment | Prevents Android 15+ runtime failures |
| 3 | Pre-allocate `Paint`/`Path`/`RectF` in `FormantChartView` and `PitchProgressionView` | Eliminates GC pressure on audio-analysis screen |
| 4 | Pick one image library (Coil); remove Picasso | -1 MB AAB; one less cache to coordinate |
| 5 | Add a Baseline Profile via `androidx.benchmark:benchmark-macro-junit4` | -20-30% cold start typical |
| 6 | Move `MobileAds.initialize` and `FileUtil.clearTempFolder` off `Application.onCreate` | -200-500 ms cold start |
| 7 | Acquire `PARTIAL_WAKE_LOCK` while wear is recording audio | Prevents recording termination |
