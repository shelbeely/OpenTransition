# Architecture Overview

OpenTransition follows a clean architecture pattern with clear separation of concerns across three main layers: **Data**, **Domain**, and **UI**.

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                      UI Layer                           │
│  (Fragments, Activities, ViewModels, Adapters)         │
│  - MainActivity                                         │
│  - Various Fragments (Home, Gallery, Settings, etc.)   │
│  - ViewBinding for UI                                   │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                    Domain Layer                         │
│  (Business Logic, Use Cases, State Management)         │
│  - HomeDomain                                           │
│  - AssignPhotosDomain                                   │
│  - SettingsDomain                                       │
│  - AddEditMilestoneDomain                              │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                     Data Layer                          │
│  (Models, Database, Repository Pattern)                │
│  - Realm Database                                       │
│  - Photo & Milestone Models                            │
│  - Firebase Integration                                 │
└─────────────────────────────────────────────────────────┘
```

## Design Patterns

### 1. MVVM (Model-View-ViewModel)

OpenTransition uses a variant of MVVM with a Domain layer:

- **Model**: Data classes representing Photos, Milestones, etc.
- **View**: XML layouts and Fragments
- **ViewModel**: Domain classes that manage UI state and business logic
- **Domain Layer**: Acts as a ViewModel but is called "Domain" in this codebase

### 2. Repository Pattern

While not explicitly called repositories, the Domain classes act as repositories:
- Abstract data access logic
- Provide clean API for UI layer
- Handle data transformations
- Manage background operations

### 3. Reactive Programming

Uses RxJava 3 for:
- Asynchronous operations
- Event streams
- State management
- UI updates

### 4. Dependency Injection

Simple singleton pattern via `DomainManager`:
```kotlin
class DomainManager {
    val homeDomain = HomeDomain()
    val assignPhotosDomain = AssignPhotosDomain()
    val settingsDomain = SettingsDomain()
    val addEditMilestoneDomain = AddEditMilestoneDomain()
    val editPhotoDomain = EditPhotoDomain()
}
```

## Core Components

### Application Class

**`TransTracksApp`**: Application singleton

- Initializes MobileAds
- Initializes AppSearch for system-wide content search
- Manages DomainManager
- Handles app version updates
- Manages Firebase settings synchronization

```kotlin
class TransTracksApp : Application() {
    val domainManager = DomainManager()
    val adConsentStatus = BehaviorSubject.createDefault(ConsentStatus.UNKNOWN)
    val firebaseSettingUtil: FirebaseSettingUtil by lazy(LazyThreadSafetyMode.NONE) {
        FirebaseSettingUtil()
    }
    
    override fun onCreate() {
        super.onCreate()
        // Initialize AppSearch for system-wide search
        AppSearchManager.getInstance(this)
    }
}
```

### Main Activity

**`MainActivity`**: Single activity architecture

- Hosts Navigation Component
- Manages media picker
- Handles Firebase Auth
- Manages screen lock/security
- Processes backup imports
- Handles app shortcuts activation

### Navigation

Uses **Android Navigation Component**:
- Single Activity with multiple Fragments
- Type-safe arguments with SafeArgs plugin
- Global actions for common navigation patterns
- Back stack management

### Database

**Realm Kotlin**: Local persistence

- Object-oriented database
- Reactive queries
- Easy migration
- Excellent performance

### Cloud Services

**Firebase Integration**:
- **Authentication**: User accounts with email/Google sign-in
- **Firestore**: Cloud data sync
- **Crashlytics**: Crash reporting
- **Analytics**: Usage tracking (opt-in)

## Package Structure

```
com.shelbeely.opentransition/
├── data/                           # Data models and providers
│   ├── Photo.kt                   # Photo data model
│   ├── Milestone.kt               # Milestone data model
│   └── TransTracksFileProvider.kt # Content provider
│
├── domain/                         # Business logic layer
│   ├── DomainManager.kt           # Domain instances manager
│   ├── HomeDomain.kt              # Home screen logic
│   ├── AssignPhotosDomain.kt      # Photo assignment logic
│   ├── SettingsDomain.kt          # Settings management
│   ├── AddEditMilestoneDomain.kt  # Milestone editing
│   └── EditPhotoDomain.kt         # Photo editing logic
│
├── ui/                             # User interface layer
│   ├── MainActivity.kt            # Main activity
│   ├── home/                      # Home screen
│   ├── gallery/                   # Gallery view
│   ├── milestones/                # Milestones management
│   ├── settings/                  # Settings screen
│   ├── selectphoto/               # Photo selection
│   ├── assignphoto/               # Photo assignment
│   ├── editphoto/                 # Photo editing
│   ├── singlephoto/               # Single photo view
│   ├── lock/                      # Lock screen
│   └── widget/                    # Custom widgets
│
├── appsearch/                      # AppSearch integration
│   ├── AppSearchManager.kt        # Search session manager
│   ├── PhotoDocument.kt           # Photo search document
│   └── MilestoneDocument.kt       # Milestone search document
│
├── shortcuts/                      # App Shortcuts
│   └── ShortcutManagerUtil.kt     # Shortcut management
│
├── util/                           # Utility classes
│   ├── FileUtil.kt                # File operations
│   ├── RxSchedulers.kt            # RxJava schedulers
│   ├── SearchIndexingUtil.kt      # Content indexing utility
│   └── settings/                  # Settings utilities
│       ├── SettingsManager.kt     # Settings management
│       ├── Theme.kt               # Theme definitions
│       ├── LockType.kt            # Lock type enum
│       └── PrefUtil.kt            # SharedPreferences helper
│
├── background/                     # Background tasks
│   ├── CameraHandler.kt           # Camera operations
│   └── StoragePermissionHandler.kt # Permission handling
│
└── TransTracksApp.kt              # Application class
```

## Data Flow

### Typical User Action Flow

1. **User Interaction** → Fragment receives input
2. **Fragment** → Calls Domain method
3. **Domain** → Updates Realm database
4. **Realm** → Notifies observers via RxJava
5. **Domain** → Emits state updates
6. **Fragment** → Updates UI

### Example: Adding a Photo

```kotlin
// 1. User selects photo in UI (Fragment)
pickMediaLauncher.launch(PickVisualMediaRequest(type))

// 2. MainActivity receives result
pickMediaLauncher = registerForActivityResult(PickMultipleVisualMedia()) { uris ->
    // Navigate to assign photos
    navController.navigate(MainNavDirections.actionGlobalAssignPhotos(...))
}

// 3. AssignPhotosFragment gets data
val args: AssignPhotosFragmentArgs by navArgs()

// 4. Domain processes the request
assignPhotosDomain.addPhotos(photoUris, type, date)

// 5. Domain writes to Realm
realm.write {
    copyToRealm(Photo(...), UpdatePolicy.ALL)
}

// 6. UI observes changes and updates
assignPhotosDomain.state
    .observeOn(RxSchedulers.main())
    .subscribe { state -> updateUI(state) }
```

## Threading Model

### RxSchedulers

Centralized scheduler management:

```kotlin
object RxSchedulers {
    fun main(): Scheduler = AndroidSchedulers.mainThread()
    fun io(): Scheduler = Schedulers.io()
    fun computation(): Scheduler = Schedulers.computation()
}
```

### Threading Rules

- **UI updates**: Main thread only
- **Database operations**: IO thread
- **File operations**: IO thread
- **Network calls**: IO thread
- **Heavy computations**: Computation thread

## State Management

### Domain State Pattern

Each Domain class manages its own state:

```kotlin
sealed class HomeState {
    object Loading : HomeState()
    data class Success(val photos: List<Photo>) : HomeState()
    data class Error(val message: String) : HomeState()
}
```

State is exposed via RxJava observables:

```kotlin
class HomeDomain {
    private val _state = BehaviorSubject.create<HomeState>()
    val state: Observable<HomeState> = _state
    
    fun loadPhotos() {
        _state.onNext(HomeState.Loading)
        // Load data...
        _state.onNext(HomeState.Success(photos))
    }
}
```

## Error Handling

### Layered Error Handling

1. **Data Layer**: Catch database/network errors
2. **Domain Layer**: Transform to domain errors
3. **UI Layer**: Display user-friendly messages

### Firebase Integration

- **Crashlytics**: Automatic crash reporting
- **Analytics**: Track error events
- **Custom logging**: Context-aware error messages

## Security Architecture

### App Lock System

Three lock types:
- **Off**: No lock
- **Normal**: Standard app icon, PIN/pattern lock
- **Trains**: Disguised app icon, PIN/pattern lock

Lock implementation:
- Hashed PIN/pattern storage
- Configurable auto-lock delay
- Secure flag prevents screenshots when locked

### Data Security

- **Local storage**: Realm encrypted database (optional)
- **Password hashing**: Salted hash for PIN/pattern
- **Secure flag**: Prevents screenshots in sensitive screens
- **Firebase Auth**: Industry-standard authentication

## Performance Optimizations

### Image Loading

- **Picasso**: Efficient image loading and caching
- **Memory management**: Aggressive bitmap recycling
- **Lazy loading**: Load images on demand

### Database Queries

- **Reactive queries**: Only load visible data
- **Pagination**: Limit query results
- **Indexing**: Proper field indexing in Realm

### Memory Management

- **LeakCanary**: Memory leak detection (debug only)
- **Lifecycle awareness**: Proper subscription disposal
- **CompositeDisposable**: Batch disposal of RxJava subscriptions

## Testing Architecture

### Unit Tests

Location: `app/src/test/`
- Domain logic tests
- Utility function tests
- ViewModel tests

### Instrumentation Tests

Location: `app/src/androidTest/`
- UI tests
- Database tests
- Integration tests

## Next Steps

Learn more about specific layers:

- [Data Layer](data-layer.md) - Models and persistence
- [Domain Layer](domain-layer.md) - Business logic
- [UI Layer](ui-layer.md) - User interface
- [Navigation](navigation.md) - App navigation flow
