# Dependencies

*This page documents the key dependencies used in OpenTransition.*

## Key Dependencies

### Core Libraries

- **Kotlin** 2.0.20 - Primary programming language
- **Realm Kotlin** 2.3.0 - Database (backwards compatibility for imports)
- **Room** - Modern Android database (primary storage)
- **RxJava 3** - Reactive programming
- **Firebase SDK** - Cloud services and analytics
- **Material Design Components** - UI components

### Feature Libraries

#### AndroidX AppSearch
Used for system-wide content search integration:

```gradle
implementation("androidx.appsearch:appsearch:1.1.0-alpha05")
implementation("androidx.appsearch:appsearch-local-storage:1.1.0-alpha05")
kapt("androidx.appsearch:appsearch-compiler:1.1.0-alpha05")
```

**Purpose:** Enables photos and milestones to appear in Android's system search.

**Documentation:** [AppSearch Feature](../features/app-search.md)

#### AndroidX Core (ShortcutManagerCompat)
Used for app shortcuts:

```gradle
implementation("androidx.core:core-ktx:1.12.0")
```

**Purpose:** Provides quick access shortcuts from the launcher.

**Documentation:** [Shortcuts Feature](../features/shortcuts.md)

### Testing Libraries

- **JUnit** - Unit testing framework
- **Espresso** - UI testing
- **MockK** - Mocking library for Kotlin
- **Room Testing** - Database testing utilities

## Version Management

See `mobile/build.gradle` for the complete dependency list and version numbers.

### Updating Dependencies

When updating dependencies:

1. Check for breaking changes in release notes
2. Update version in `build.gradle`
3. Test thoroughly
4. Update this documentation if needed

### Security Considerations

Always check dependencies for security vulnerabilities:

```bash
./gradlew dependencyUpdates
```

## Related Documentation

- [Building the App](../getting-started/building.md) - Build instructions
- [Development Setup](../getting-started/development-setup.md) - Environment setup
- [Architecture Overview](../architecture/overview.md) - System architecture
