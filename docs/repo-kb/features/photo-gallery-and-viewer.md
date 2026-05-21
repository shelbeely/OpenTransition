# Feature: Photo Gallery and Viewer

## Summary

A grid view (Gallery) groups saved photos by date with a sticky date-title header, and a single-photo viewer (SinglePhoto) shows one image full-screen with metadata. Audio items are listed alongside photos but render with a distinct adapter row.

## Entry points

| Surface | Component | Path |
|---|---|---|
| Gallery grid | `GalleryFragment` + `GalleryScreen` (Compose) | [`ui/gallery/GalleryFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/gallery/GalleryFragment.kt) / [`ui/gallery/GalleryScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/gallery/GalleryScreen.kt) |
| Home gallery (recent strip) | `HomeFragment` + `HomeGalleryAdapter` | [`ui/home/`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/home/) |
| Single-photo viewer | `SinglePhotoFragment` + `SinglePhotoScreen` | [`ui/singlephoto/`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/singlephoto/) |
| Edit metadata | `EditPhotoFragment` + `EditPhotoScreen` | [`ui/editphoto/`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/editphoto/) |

## Adapters and row types

| Adapter | Path | Row types |
|---|---|---|
| `GalleryAdapter` | [`ui/gallery/GalleryAdapter.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/gallery/GalleryAdapter.kt) | `GalleryDateTitleItem`, `GalleryPhotoItem`, `GalleryAudioItem` |
| `HomeGalleryAdapter` | [`ui/home/HomeGalleryAdapter.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/home/HomeGalleryAdapter.kt) | `HomeDateSummaryItem`, `HomeGalleryPhotoItem`, `HomeGalleryAudioItem` |

Custom views used: `SquareImageView`, `SquareConstraintLayout`, `SquareChildrenLinearLayout` ([`ui/widget/`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/widget/)).

## State holders

- `HomeDomain` ([`domain/HomeDomain.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/domain/HomeDomain.kt)) — drives the home strip.
- `EditPhotoDomain` ([`domain/EditPhotoDomain.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/domain/EditPhotoDomain.kt)) — single-photo edit.

## Persistence touched

| Source | Reader |
|---|---|
| Realm `Photo` (legacy adapters read this directly) | `GalleryAdapter`, `HomeGalleryAdapter`, `MilestonesAdapter`, `SinglePhotoFragment` — see [`audit-report/07-issues-and-bugs.md`](../../../audit-report/07-issues-and-bugs.md) ISSUE-004 for the file-and-line index. |
| Room `photos` table (newer paths) | `PhotoDao` via `DatabaseManager.getDatabase(context).photoDao()` |
| Image files on disk | Loaded via Picasso (`com.squareup.picasso:picasso`); see [`util/FileUtil.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/FileUtil.kt) |

## External APIs

- **Picasso** for image loading and caching.
- **No** Firebase / network calls in the gallery path; images live entirely on-device.

## Known issues (audit cross-reference)

- 🔴 ISSUE-004 — gallery reads bypass `DatabaseManager` and hit Realm directly, contradicting the encrypted-DB toggle.
- 🟡 ISSUE-012 — `Realm.openDefault()` on the main thread inside RecyclerView adapters.
- 🟡 `GalleryAdapter.kt:201` TODO: silent deletion failure.
- 🔴 `:mobile:lintDebug` failure: `context.getString` inside a `@Composable` in `HomeScreen.kt:133, 137` ([`audit-report/18-refresh-2026-04-29.md`](../../../audit-report/18-refresh-2026-04-29.md) ISSUE-035).

## Invariants

- Photos are grouped by `epochDay` descending, then `timestamp` descending — matches `PhotoDao.getAllPhotos()` SQL ([`PhotoDao.kt:20`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/dao/PhotoDao.kt)).
- Date headers are inserted by the adapter when `epochDay` changes between adjacent items.

## Tests

None directly cover the gallery as of this pass.
