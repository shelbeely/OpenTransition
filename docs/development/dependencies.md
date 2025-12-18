# Dependencies

## Key Dependencies

OpenTransition uses modern Android libraries and frameworks to deliver a robust, secure, and user-friendly experience.

### Core Language & Build

- **Kotlin**: 2.0.20 - Modern programming language for Android
- **Android Gradle Plugin**: 8.13.0 - Build system
- **Compile SDK**: 36 (Android 15)
- **Target SDK**: 36 (Android 15)
- **Min SDK**: 21 (Android 5.0 Lollipop)

### UI Framework

- **Material Design Components**: 1.13.0 - Material Design 3 (M3) implementation
- **Dynamic Animation**: 1.0.0 - Spring physics for M3 Expressive animations
- **ConstraintLayout**: 2.1.4 - Advanced layout system
- **RecyclerView**: 1.3.2 - Efficient list displays

**➡️ See [Material Design Components](material-design-components.md) for detailed information**

### Data Layer

- **Realm Kotlin**: 2.3.0 - Local database for photo and milestone data
- **RxJava**: 3.1.8 - Reactive programming for data streams
- **RxAndroid**: 3.0.2 - Android-specific RxJava bindings
- **RxBinding**: 3.1.0 - RxJava bindings for Android UI widgets
- **RxRelay**: 3.0.1 - RxJava Subjects that cannot error out

### Navigation

- **Navigation Component**: 2.8.5 - Android Jetpack Navigation
  - `navigation-fragment-ktx`
  - `navigation-ui-ktx`
  - `navigation-safe-args-gradle-plugin`

### Firebase & Backend

- **Firebase BOM**: 32.3.1 (manages all Firebase versions)
- **Firebase Analytics**: Included via BOM
- **Firebase Auth**: Included via BOM
- **Firebase Crashlytics**: Included via BOM
- **Firebase Firestore**: Included via BOM
- **FirebaseUI Auth**: 8.0.2 - Pre-built authentication UI

### Google Services

- **Play Services Ads**: 22.4.0 - AdMob integration
- **Play Services Auth**: 20.7.0 - Google Sign-In
- **Play Services Wearable**: 18.1.0 - Wear OS communication

### Camera & ML

- **CameraX**: 1.3.1 - Modern camera implementation
  - `camera-core`
  - `camera-camera2`
  - `camera-lifecycle`
  - `camera-view`
- **ML Kit Face Detection**: 16.1.6 - Face detection for smart cropping

### Utilities

- **Gson**: 2.10.1 - JSON serialization/deserialization
- **Picasso**: 2.8 - Image loading and caching
- **Kotlinx Coroutines**: 1.7.3 - Asynchronous programming
- **ExifInterface**: 1.3.6 - Photo metadata handling

### Development & Debugging

- **LeakCanary**: 2.12 (debug builds only) - Memory leak detection

### Wear OS Module

The Wear OS companion app has its own dependencies:

- **Wear**: 1.3.0 - Wear OS base library
- **Wear Compose Material**: 1.3.0 - Material Design for Wear
- **Wear Compose Foundation**: 1.3.0 - Wear OS Compose foundation
- **Play Services Wearable**: 18.1.0 - Communication with mobile app

### Shared Module

The shared module (for communication between mobile and Wear) uses:

- **Play Services Wearable**: 18.1.0 - Data Layer API
- **Gson**: 2.10.1 - Data serialization

## Dependency Management

All dependencies are declared in module-specific `build.gradle` files:

- **Mobile app**: `/mobile/build.gradle`
- **Wear OS app**: `/wear/build.gradle`
- **Shared module**: `/shared/build.gradle`
- **Root project**: `/build.gradle` (defines versions and common configuration)

## Version Catalog

Common versions are defined in `/build.gradle`:

```gradle
ext.kotlin_version = '2.0.20'
ext.realm_version = '2.3.0'
ext.nav_version = "2.8.5"
```

## Updating Dependencies

When updating dependencies:

1. Check compatibility with the current Android Gradle Plugin version
2. Test thoroughly, especially for major version updates
3. Update all related libraries together (e.g., all CameraX libraries)
4. Review release notes for breaking changes
5. Test on multiple Android versions (especially min SDK 21 and latest)

## Additional Resources

- [Material Design Components Documentation](material-design-components.md)
- [Architecture Overview](../architecture/overview.md)
- [Building the App](../getting-started/building.md)
