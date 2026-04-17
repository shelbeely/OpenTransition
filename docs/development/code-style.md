# Code Style

## General Guidelines

OpenTransition follows the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) and Android community best practices. Key project-specific rules:

- **No Jetpack Compose** — the UI uses XML Views + ViewBinding throughout. Do not introduce Compose.
- **No new KAPT annotation processors** — use KSP instead (already configured).
- **No new DI frameworks** — dependencies are passed manually via `DomainManager`.

## Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Classes | `PascalCase` | `HomeDomain`, `GalleryAdapter` |
| Functions | `camelCase` | `loadPhotos()`, `updateUI()` |
| Constants | `UPPER_SNAKE_CASE` | `TYPE_FACE`, `CODE_SALT` |
| XML layouts | `snake_case` with screen prefix | `fragment_home.xml`, `item_photo.xml` |
| XML IDs | `snake_case` | `@+id/photo_image_view` |
| Resource strings | `snake_case` | `@string/add_photo` |

## UI Layer

- Use **ViewBinding** — never `findViewById`.
- Keep Fragments thin; business logic lives in Domain classes.
- Observe Domain state via RxJava `Observable`/`BehaviorSubject`.
- Dispose subscriptions using `CompositeDisposable` in `onDestroyView`.

```kotlin
private val disposables = CompositeDisposable()

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    disposables.add(
        domain.state
            .observeOn(RxSchedulers.main())
            .subscribe { updateUI(it) }
    )
}

override fun onDestroyView() {
    disposables.clear()
    super.onDestroyView()
}
```

## Domain Layer

- Each Domain class exposes its state via a `BehaviorSubject`.
- Domain classes must not reference Android `Context` directly — pass it as a parameter when needed.
- Use `RxSchedulers.io()` for database/file operations and `RxSchedulers.main()` for UI updates.

```kotlin
class HomeDomain {
    private val _state = BehaviorSubject.create<HomeState>()
    val state: Observable<HomeState> = _state

    fun loadPhotos() {
        Observable.fromCallable { /* db query */ }
            .subscribeOn(RxSchedulers.io())
            .observeOn(RxSchedulers.main())
            .subscribe(
                { photos -> _state.onNext(HomeState.Success(photos)) },
                { error -> _state.onNext(HomeState.Error(error.message ?: "")) }
            )
    }
}
```

## Data Layer

- **Room entities** are the source of truth for app data.
- **Realm classes** in `data/` are kept solely for TransTracks import backwards compatibility — do not add new Realm code.
- Always annotate Room entities with `@Entity` and use `@PrimaryKey`.
- Wrap database writes in coroutines or RxJava background schedulers — never on the main thread.

## Error Handling

- Never swallow exceptions silently.
- Log unexpected errors to Firebase Crashlytics: `FirebaseCrashlytics.getInstance().recordException(e)`.
- Surface user-friendly errors via the Domain state.

## Comments

- Add comments only when they explain *why*, not *what*.
- Mark backwards-compatibility code with `// BACKWARDS COMPATIBILITY: <reason>`.
- Avoid TODO comments in committed code — open a GitHub issue instead.

## Imports

- Remove unused imports.
- Do not use star imports (`import com.example.*`).

## Formatting

- Use Android Studio's default formatter (4-space indentation).
- Maximum line length: 120 characters.
- Add a blank line between functions.

## Testing

See [Testing](testing.md) for how to run tests. When writing tests:

- Unit test files live in `mobile/src/test/`.
- Name tests with the `should` convention: `fun loadPhotos_should_emitSuccessState_whenDatabaseReturnsData()`.
- Use `TestScheduler` or `RxJava TestObserver` for reactive code.
