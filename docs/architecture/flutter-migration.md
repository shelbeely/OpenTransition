# Flutter Migration Assessment

## Short answer

Converting OpenTransition to Flutter would be a **full rewrite** of the Android app. Aside from assets, copy, and the existing data formats (Realm schema and export JSON), very little Kotlin/Android code can be reused. Expect a **multi-week effort** even for an experienced Flutter team because every layer—UI, navigation, storage, sync, ads, and app lock—must be rebuilt with Flutter equivalents.

## Current stack you’d need to replace

- **UI & navigation**: Single-activity + Fragments, Android Navigation Component, ViewBinding, Material 3 styling.
- **State & async**: RxJava 3, domain classes acting as ViewModels.
- **Local storage**: Realm Kotlin for photos and milestones; app-private files for images.
- **Cloud & analytics**: Firebase Auth/Firestore/Crashlytics/Analytics via Android SDKs.
- **Ads & consent**: Google Mobile Ads SDK with per-screen ad IDs and consent handling.
- **Platform features**: Media pickers, file exports/imports (ZIP with data.json + images), app lock/disguised icon, secure flag to block screenshots, permissions flow.

## What Flutter equivalents would need to be built

- **Project foundation**: New Flutter app (Material 3), navigation (Navigator 2.0/go_router), theming, localization.
- **State management**: Choose Riverpod/BLoC/ValueNotifier; reimplement domain logic and state streams.
- **Local data**: Pick a Flutter-friendly store (Isar, Hive, or Drift + SQLite). Implement migrations/importers from existing exports (JSON + image files) and any future Realm backups.
- **Media handling**: Replace Android media pickers with `image_picker`/`file_picker` plus EXIF/date handling to preserve `timestamp` and `epochDay`.
- **Cloud sync**: FlutterFire packages for Auth/Firestore/Crashlytics/Analytics; rebuild sync flows and error handling.
- **Ads**: `google_mobile_ads` plugin; recreate per-screen ad placements and consent prompts.
- **Security & lock**: Implement PIN/pattern storage (with salts), secure screen flag, and disguised icon (Android alt launcher icon via Flutter + platform channel).
- **Import/Export**: Recreate ZIP export format, validate JSON, and copy images to Flutter’s storage layout.
- **Testing & release**: New test pyramid (unit, widget, integration), build/release pipelines, and Play Store setup.

## Rough effort estimate (for parity, 1–2 experienced Flutter engineers)

- Foundation & tooling: **1–2 weeks** (project setup, theming, navigation, CI/build config).
- Data layer & import/export: **1–2 weeks** (DB selection, models, migration from current JSON/Realm exports).
- Core features (photos, milestones, gallery, settings, backup/sync): **3–4 weeks** (UI, state, sync, error handling).
- Platform integrations (camera/picker, ads, lock/disguise, secure flag, analytics/crash): **2–3 weeks**.
- Polish, accessibility, and hardening: **1–2 weeks** (QA, perf, offline checks).

Total: **8–13 weeks** of focused work, assuming existing Firebase project/ad IDs can be reused and requirements stay close to today’s app. New features, iOS support, or major UX changes add time.

## Recommended migration path

1. **Scope parity**: Freeze feature scope; document must-have behaviors (lock flow, export/import contract, ad placements, consent).
2. **Choose stack**: Pick state management and database; decide on FlutterFire, ads, and media plugins up front.
3. **Prototype spike**: Prove photo import/store/display and a simple Firestore sync to de-risk storage and performance.
4. **Build data & sync**: Implement models, migrations, and sync jobs; validate import of a real OpenTransition backup.
5. **Recreate UI flows**: Home, gallery, milestones, settings, backup/sync, and lock screens with Material 3.
6. **Hardening**: Add analytics/crash reporting, ads/consent, accessibility, offline behavior, and secure flag handling.
7. **Test & ship**: Widget/integration tests, manual QA, Play release, and optional migration helper for existing Android users.

## What can be reused

- **Content & assets**: Copy, icons, colors, and strings as design references.
- **Data contracts**: Realm schema and export JSON structure guide the Flutter models and migration/import code.
- **Firebase projects**: Existing Firebase configuration (Auth/Firestore/Crashlytics/Analytics) and ad unit IDs, if retained.

## Risks & open questions

- **Data migration fidelity**: Ensuring lossless import of Realm data and photo files into the new store.
- **Lock & disguise parity**: Flutter support for secure flag and alternate launcher icons requires platform channels and device testing.
- **Performance at scale**: Large image libraries need careful caching and memory management in Flutter.
- **Offline-first expectations**: Confirm required offline behavior before choosing database/sync patterns.
