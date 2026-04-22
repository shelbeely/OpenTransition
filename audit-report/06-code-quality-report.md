# 06 — Code Quality Report

> Per-Gradle-module ratings and findings. Linters were **not** executed (read-only mandate);
> configurations are inspected and reported.

## Top-level summary

| Module | Kotlin idiom | Coroutine hygiene | Compose | Resource hygiene | Detekt/ktlint/Lint config | Complexity | **Overall** |
|--------|------:|------:|------:|------:|------:|------:|------:|
| `:shared` | 8 | N/A (no coroutines) | N/A | N/A | ❌ none | 9 (small) | **7** |
| `:mobile` | 6 | 5 | 6 | 6 | ⚠️ Android Lint default only | 4 | **5** |
| `:wear`   | 5 | 5 | 1 (dead) | 5 | ⚠️ Android Lint default only | 6 (small) | **4** |

## `:shared` — rating 7

**Strengths:**
- ✅ Pure constants + tiny `data class` — idiomatic.
- ✅ `@Parcelize` plugin used.
- ✅ Companion `object` for `WearableConstants`.

**Concerns:**
- 🟡 `WearableHelper.parseMilestones` swallows all exceptions (`shared/.../util/WearableHelper.kt:73-79`):
  ```kotlin
  return try { gson.fromJson(...).toList() } catch (e: Exception) { emptyList() }
  ```
  Catching `Exception` is too broad; even an OOM is masked. At minimum log + bounded catch
  (`JsonParseException`, `JsonSyntaxException`).
- 🟡 No KDoc on the model fields. `MilestoneData` doesn't say what the `type` strings are.
- 🟡 The `@Parcelize` is unused at the actual sync site (Gson string is sent instead) — the annotation
  is a hint but not honoured by the wire format.

## `:mobile` — rating 5

### Kotlin idiom (rating 6)

**Strengths:**
- ✅ Sealed enums for `LockType`, `LockDelay`, `Theme`, `AppColorVariant`.
- ✅ Null safety honoured throughout (no `!!` in core paths beyond `lateinit`).
- ✅ `data class` for entities and DTOs.
- ✅ Extension functions in `util/` (`Realms.kt`, `Lifecycle.kt`, `Files.kt`, etc.) — idiomatic.

**Concerns:**
- 🟠 **Singleton-heavy.** `SettingsManager`, `PrefUtil`, `FileUtil`, `RxSchedulers`, `DatabaseManager`,
  `KeystoreManager` are all `object` singletons. Combined with `TransTracksApp.instance`, the codebase
  is a service-locator over a global namespace. Untestable without robolectric.
- 🟡 **Incoming params named with `val` in lambdas suppressed.** `@Suppress("EnumEntryName")` appears
  on every Kotlin enum because the enum entries break naming conventions to match the `Key` enum —
  acceptable but worth documenting once.
- 🟡 Some functions are 100+ LOC — `MainActivity.processIntent`, `SettingsFragment.startMigration`,
  `RealmBackupImporter.importFromBackup` are the worst offenders.
- 🟢 **No `!!` abuse**, **no raw types**, **no nullable platform types** spotted in core flows.

### Coroutine hygiene (rating 5)

- 🟡 `lifecycleScope.launch(Dispatchers.IO)` instead of `lifecycleScope.launch { withContext(Dispatchers.IO) { ... } }` (`SettingsFragment.kt:900`). Subtle: the former dispatches the whole continuation on IO, the latter switches only the body — usually preferred.
- ❌ No `CoroutineExceptionHandler` anywhere — uncaught exceptions in `lifecycleScope.launch { ... }` end up as `crash` (or worse, swallowed if launched from `viewLifecycleOwner.lifecycleScope` after view destruction).
- ❌ No `SupervisorJob`.
- 🟢 No `GlobalScope` usage. ✅
- 🟢 No `runBlocking` in production. ✅

### Compose (rating 6)

- ✅ `OpenTransitionTheme` correctly forwards through `MaterialTheme`.
- ✅ Uses `derivedStateOf`-style patterns where needed.
- 🟡 Several `@Composable`s accept `Modifier` but don't pass it through — typical mid-migration
  inconsistency. Spot-check: `ui/recordaudio/*Screen.kt`.
- 🟡 `remember` is used, but several `@Composable`s receive non-stable types (e.g. raw `List<X>` rather
  than `ImmutableList`). Recomposition risk on settings screen.
- 🟡 Side effects: `LaunchedEffect(Unit)` is used in `SettingsScreen.kt`; OK, but no `key` other than `Unit` means it never re-launches if state changes.
- 🟡 Compose previews don't enable Live Edit / `@PreviewParameter` — easy improvement.

### Resource hygiene (rating 6)

- ✅ Strings are in `values/strings.xml` and `values/strings.auth.xml`.
- 🟡 `colors.xml` has 30 hand-named colours that overlap with the Compose palette — duplication.
- 🟡 `dimens.xml` exists but several `padding="16dp"` and `textSize="14sp"` literals leak into XML
  layouts. Spot-check via `grep -rn '"[0-9]\+dp"\|"[0-9]\+sp"' mobile/src/main/res/layout | wc -l` would
  give an exact count.
- ✅ ProGuard rules are minimal (`proguard-rules.pro` 30 LOC) — no leaked class names.

### Lint / static analysis config (rating ⚠️)

- ❌ **No Detekt** (no `detekt.yml`, no plugin in any `build.gradle`).
- ❌ **No ktlint** (no `.editorconfig`-style ktlint rules, no plugin).
- ⚠️ **Android Lint default** only — there is no `android.lintOptions { abortOnError true }` block.
  CI runs `./gradlew build` which transitively runs Lint, but failures aren't blocking.
- ⚠️ The CI uploads `mobile/build/reports/lint-results-debug.html` as an artifact (`.github/workflows/ci.yml:35-40`)
  but no human appears to read it.

### Complexity hotspots (rating 4)

| File | LOC | Worst function | Risk |
|------|---:|----------------|------|
| `ui/settings/SettingsFragment.kt` | 961 | `onViewCreated` ~150 LOC; `startMigration` deeply nested | 🔴 god-object |
| `ui/MainActivity.kt` | 552 | `processIntent` 80+ LOC | 🟠 |
| `util/settings/SettingsManager.kt` | 588 | `appVersionUpdateIfNecessary` (in `TransTracksApp.kt`) — 140 LOC of nested `when` | 🟠 |
| `ui/gallery/GalleryAdapter.kt` | 465 | Adapter that opens its own Realm and does deletion | 🟠 |
| `database/migration/RealmBackupImporter.kt` | ~350 | `importFromBackup` linear ZIP-walk + Realm writes | 🟡 acceptable for one-shot import |

## `:wear` — rating 4

### Kotlin idiom (rating 5)

- ⚠️ Heavy use of `findViewById<...>()` and `lateinit var` — pre-`viewBinding` style.
- ⚠️ No `data class` other than via `:shared`.
- ✅ Listener `object` lambdas are concise.

### Coroutine hygiene (rating 5)

- N/A — no coroutines used. Calls Play Services `Task` API directly with `addOnSuccessListener` /
  `addOnFailureListener`. Acceptable for a small Wear app, but a coroutines wrapper (`await()`) would
  remove the callback noise in 3 of the 5 files.

### Compose (rating 1)

- ❌ Compose dependencies pulled in but **WearTheme.kt is dead code** (no caller).
  See `04-wear-os-app-review.md §1`.

### Resource hygiene (rating 5)

- 🔴 **Hard-coded string in source code:** `recordButton.text = "🎤 Record"` and similar literals at
  `wear/.../AudioRecordActivity.kt:122-131` (and also in `MainActivity.kt`). These bypass `strings.xml`
  and break i18n.
- ✅ `wear/src/main/res/values/strings.xml` exists for some strings.

### Lint config (rating ⚠️)

- Same as `:mobile` — Android Lint default only.

## Per-module summary table (repeated for the executive)

| Module | LOC | Files | Tests | Largest file | Lint config | Rating |
|--------|----:|-----:|-----:|--------------|-------------|------:|
| `:shared` | 168 | 3 | 0 | 80 (`WearableHelper.kt`) | none | **7** |
| `:mobile` | 19,914 | 144 | 10 unit, 1 instrumented | 961 (`SettingsFragment.kt`) | Android Lint default | **5** |
| `:wear`   | 881 | 5 | 0 | 249 (`AudioRecordActivity.kt`) | Android Lint default | **4** |

## Top 5 cross-cutting recommendations

1. **Add Detekt** with the project-default config and a CI step that fails on threshold breach. (Lowest-risk
   way to start enforcing complexity ceilings.)
2. **Split `SettingsFragment.kt`** into per-section composables behind a single state holder.
3. **Eliminate hardcoded UI strings in `:wear`** — move to `strings.xml`.
4. **Adopt `viewBinding`** in the Wear app (one toggle in `wear/build.gradle`).
5. **Add a `CoroutineExceptionHandler` baseline** and pass it everywhere `lifecycleScope.launch` is used.
