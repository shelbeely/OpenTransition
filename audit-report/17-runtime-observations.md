# 17 — Runtime Observations (Emulator-Driven)

> Added in response to the user's follow-up: *"Can we also use the emulator to run the app and create
> further reports?"* This document records what was observed when the actual debug APK was installed
> on a freshly booted emulator and exercised. **No production code was modified.**

## Test environment

| Aspect | Value |
|--------|-------|
| Date | 2026-04-22 |
| Emulator binary | `emulator 36.5.10.0` |
| AVD | `medium_phone` (Android 36 / API 36, Google APIs PlayStore, x86_64) |
| Acceleration | KVM ✅ |
| Display | headless (`-no-window -gpu swiftshader_indirect -no-snapshot`) |
| Form factor | 1080 × 2400 phone |
| Mobile APK installed | `mobile/build/outputs/apk/debug/mobile-debug.apk` (74 MB, versionCode 128, versionName 1.3.128) |
| Wear APK | built (`wear-debug.apk`, 27 MB) but **could not be installed on the phone AVD** — `INSTALL_FAILED_MISSING_SHARED_LIBRARY: Package com.shelbeely.opentransition.wear requires unavailable shared library com.google.android.wearable; failing!` (expected — Wear APKs require a Wear AVD; one was not in the cmdline-tools profile catalogue) |

## What was exercised

| Action | Result |
|--------|--------|
| Cold install via `adb install -r mobile-debug.apk` | ✅ `Success` |
| Launch via `am start -n com.shelbeely.opentransition/.MainActivityDefault` | ✅ resumed activity within 3 s |
| First-run "Welcome" dialog | ✅ rendered and dismissable |
| Tap "Looks Good" → home screen | ✅ Face / Body / Audio gallery + start-day appear |
| Tap "Audio Gallery" → audio gallery screen | ✅ rendered (empty state) |
| `KEYCODE_BACK` | ✅ returned to home |
| `:mobile:lintDebug` | ✅ ran cleanly (`0 errors, 235 warnings, 1 hint`) |

Screenshots captured under `audit-report/runtime-artifacts/`:
- `01-mobile-launch.png` — Welcome dialog (first run)
- `02-mobile-home.png` — Home screen with the four primary entry points
- `03-mobile-audio-gallery.png` — Audio gallery (empty state)

## Findings only visible at runtime

### 🔴 R-RUN-1 — Two LAUNCHER icons in debug builds

```
$ adb shell cmd package query-activities --components -a android.intent.action.MAIN \
                                                       -c android.intent.category.LAUNCHER
com.shelbeely.opentransition/.MainActivityDefault
com.shelbeely.opentransition/leakcanary.internal.activity.LeakLauncherActivity
```

LeakCanary's `LeakLauncherActivity` is automatically registered as `LAUNCHER` in any app that includes
the LeakCanary dependency. Because LeakCanary is in the *debug* configuration only, this is invisible
in release builds — but it does mean:

- Every developer + CI emulator + tester gets two app icons. ✅ acceptable trade-off but worth knowing.
- The `cmd package resolve-activity --brief` call returns `ResolverActivity` (the Android picker) rather
  than the real entry point. This is what bit my first launch attempt — `monkey -c LAUNCHER 1` opened
  the LeakCanary activity instead of the app.

🟡 **Minor** — can be muted by overriding LeakCanary's manifest entry with
`<activity android:name="leakcanary.internal.activity.LeakLauncherActivity" tools:node="remove"/>`
in `mobile/src/debug/AndroidManifest.xml`.

### 🟠 R-RUN-2 — 191-frame Choreographer skip during first frame

```
04-22 21:09:51  Choreographer  Skipped 33 frames!  ...
04-22 21:09:56  Choreographer  Skipped 106 frames!  ...
04-22 21:09:57  Choreographer  Skipped 61 frames!  ...
04-22 21:10:00  Choreographer  Skipped 191 frames!  ...
04-22 21:10:01  Choreographer  Skipped 31 frames!  ...
```

Five separate "skipped frames" warnings during launch, totalling **>420 frames (~7 s)** of dropped
content. The headline 191-frame burst (~3.2 s) lines up with the moment Realm opens for the first
time, the Welcome dialog inflates, and Firebase Crashlytics initializes — all on the main thread.

This corroborates the static finding in `11-performance-and-battery.md §3`: **main-thread Realm I/O is
the dominant launch-time jank source**. On a real low-end device the user would see a frozen splash
for several seconds.

### 🟡 R-RUN-3 — 150 MB Pss after launch on an empty database

```
$ adb shell dumpsys meminfo com.shelbeely.opentransition

** MEMINFO in pid 5845 [com.shelbeely.opentransition] **
                   Pss  Private  Private  ...
        TOTAL   150525     2220     4052
```

150 MB Pss is **high for a freshly-launched app with no user data**. Typical breakdown in the dump:
- `.apk mmap`: 8.7 MB Pss (Compose + Material3 + Camera2 libs paged in)
- `.art mmap`: 1 MB Pss
- `.so mmap`: 2 MB Pss (SQLCipher native lib)
- `Native Heap`: 914 KB Pss
- `Dalvik Heap`: 423 KB Pss + 12 MB allocated

The remaining ~135 MB is graphics (Surface, GraphicBufferAllocator) — typical for a foregrounded
phone-sized Activity, not unique to this app. ✅ not actionable.

### 🟡 R-RUN-4 — Hidden API access warnings (third-party library hygiene)

```
hiddenapi: Accessing hidden field Ljava/util/Collections$SynchronizedCollection;->mutex
        from Lj$/util/DesugarCollections; (domain=app) using reflection: denied
hiddenapi: Accessing hidden method Landroid/view/WindowManagerGlobal;->getInstance()
        from Lcurtains/internal/WindowManagerSpy; using reflection: allowed
hiddenapi: Accessing hidden field Landroid/app/ActivityThread;->mH
        from Lleakcanary/ServiceWatcher; using reflection: allowed
```

- **`Lj$/util/DesugarCollections`** — the `desugar_jdk_libs:2.0.3` runtime is hitting a *denied* hidden
  API on Android 14+. Should switch to `desugar_jdk_libs:2.1.x` which is updated for hidden-API
  changes. 🟡
- **`curtains/...`** and **`leakcanary/ServiceWatcher`** — debug-only paths. ✅
- No app-code hidden-API access detected. ✅

### 🟢 R-RUN-5 — Debug AdMob doesn't crash

The debug build correctly uses Google's public test AdMob ad-unit IDs. The app loads
without an "Ads SDK couldn't initialize" error path firing. ✅

### 🟢 R-RUN-6 — No crashes during exercised flows

- No `AndroidRuntime` E logs during launch / dismiss-welcome / navigate-to-audio-gallery.
- No `FATAL EXCEPTION`.
- No `ANR`.
- No `StrictMode` violations logged by default (StrictMode isn't enabled).

✅ Stable on the happy path.

### 🟡 R-RUN-7 — `MeasurementService` (Privacy Sandbox) interaction

```
* com.google.android.adservices.api: Asc MeasurementService
  <- com.shelbeely.opentransition/u0a217 (com.shelbeely.opentransition):
       Active count 38: time 1.0%
```

Google Play Services' Privacy Sandbox `MeasurementService` is invoked from the app via the AdMob SDK.
This is normal AdMob behaviour on Android 14+ but worth knowing for the data-safety form.

## What was not exercised (and could not be without code changes or special setup)

| Untested flow | Reason |
|---------------|-------|
| Camera capture | Emulator camera is the test pattern; would launch but produce no useful artifact. CameraX permission grant flow could be tested. |
| Audio recording | `RECORD_AUDIO` not granted; can be granted via `adb shell pm grant`. |
| Mobile↔Wear Data Layer | No Wear AVD installed (would require `sdkmanager system-images;android-34;android-wear;x86`). |
| Firebase auth flow | Stubbed `google-services.json`; sign-in would fail. |
| .ttbackup import | No fixture file in repo. |
| Encrypted database flow | Toggle exists but, per `ISSUE-004`, doesn't change observable behaviour. |
| Decoy vault | Same. |

## Could the runtime tests be extended further?

Yes, in priority order:

1. **Grant runtime permissions and exercise audio + camera**:
   `adb shell pm grant com.shelbeely.opentransition android.permission.RECORD_AUDIO`
   `adb shell pm grant com.shelbeely.opentransition android.permission.CAMERA`
2. **Snapshot DB after some interactions** — pull `/data/data/com.shelbeely.opentransition/files/`
   over `adb pull` (debug-only because `android:debuggable="true"`).
3. **Install a Wear OS AVD** — requires downloading `system-images;android-34;android-wear;x86`,
   creating a Wear AVD via `avdmanager`, and pairing the two via `adb` companion-app pairing protocol.
   This would let us empirically confirm that ISSUE-001 (capability registration) is the actual root
   cause of the broken integration.
4. **Run `:mobile:connectedDebugAndroidTest`** — the boilerplate test takes ~2 min; would establish a
   baseline for CI parity.
5. **Macrobenchmark** — measure cold start with `androidx.benchmark:benchmark-macro-junit4` on the
   same emulator. (Adds a new module; not within "no code changes" mandate.)

## Tooling that would have helped (suggestions for future audits)

- A Wear OS AVD profile in `cmdline-tools` so the Wear app could be exercised in this same harness.
- A `:mobile:installDebug && :mobile:packageDebugAndroidTestArtifacts && am instrument` script.
- `adb shell setprop debug.firebase.analytics.app com.shelbeely.opentransition` + Crashlytics test
  crash to confirm the release crash pipeline.

## Summary of runtime-only findings

| ID | Severity | Finding |
|----|---------|--------|
| R-RUN-1 | 🟡 | Two LAUNCHER icons in debug (LeakCanary) |
| R-RUN-2 | 🟠 | 191-frame Choreographer skip at launch (main-thread Realm) — corroborates ISSUE-012 |
| R-RUN-3 | 🟢 | 150 MB Pss is normal for a Compose+Camera2-linked phone app |
| R-RUN-4 | 🟡 | `desugar_jdk_libs:2.0.3` triggers denied hidden-API access; bump to 2.1 |
| R-RUN-5 | 🟢 | AdMob test path works in debug |
| R-RUN-6 | 🟢 | No crashes on happy path |
| R-RUN-7 | 🟡 | AdMob → Privacy Sandbox interaction, document for data-safety form |

These are appended to the central issue tracker in `07-issues-and-bugs.md` as informational `R-RUN-#`
entries (not in the issue summary table).
