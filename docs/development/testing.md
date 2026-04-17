# Testing

## Test Structure

| Type | Location | Runner |
|------|----------|--------|
| Unit tests | `mobile/src/test/` | JUnit 4 + Robolectric |
| Instrumentation tests | `mobile/src/androidTest/` | AndroidJUnitRunner |

## Running Tests

### Unit Tests

```bash
# Run all mobile unit tests (debug variant)
./gradlew :mobile:testDebugUnitTest

# Run all tests for all modules
./gradlew test

# Run a specific test class
./gradlew :mobile:testDebugUnitTest --tests "com.shelbeely.opentransition.HomeDomainTest"
```

Test reports are generated at:
```
mobile/build/reports/tests/testDebugUnitTest/index.html
```

### Instrumentation Tests

Requires a connected device or running emulator:

```bash
# Run all instrumentation tests
./gradlew :mobile:connectedAndroidTest
```

Reports at:
```
mobile/build/reports/androidTests/connected/index.html
```

### Lint

```bash
# Lint the mobile debug variant
./gradlew :mobile:lintDebug

# Lint everything
./gradlew lint
```

Reports at:
```
mobile/build/reports/lint-results-debug.html
```

## Testing RxJava Code

Use `RxJava TestObserver` and `TestScheduler` to control timing:

```kotlin
val testObserver = domain.state.test()
domain.loadPhotos()
testObserver.assertValue { it is HomeState.Success }
```

Use `androidx.arch.core:core-testing` for `InstantTaskExecutorRule` when testing LiveData.

## Testing Room Database

Use an in-memory Room database in unit/instrumented tests:

```kotlin
val db = Room.inMemoryDatabaseBuilder(
    InstrumentationRegistry.getInstrumentation().context,
    AppDatabase::class.java
).build()
```

## Testing Encrypted Database

1. Enable in `SettingsManager` before calling `DatabaseManager.getDatabase()`
2. Create test data
3. Close and reopen the database instance
4. Verify data persists and is not readable as plaintext

## CI

Unit tests run automatically on every PR via the `pr-debug.yml` GitHub Actions workflow. See [CI/CD](../deployment/ci-cd.md) for details.
