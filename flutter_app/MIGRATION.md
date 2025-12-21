# Migration from Android to Flutter

This document outlines the migration strategy from the Android (Kotlin/Java) version to the Flutter cross-platform version.

## Overview

The Flutter migration is a complete rewrite of the OpenTransition application to support both iOS and Android from a single codebase. This is a massive undertaking that involves rewriting all features from scratch while maintaining feature parity with the Android version.

## Migration Strategy

### Phase-by-Phase Approach

We're taking an incremental approach, building the Flutter app feature by feature:

1. **Core Infrastructure** - Database, storage, authentication
2. **Security Features** - Lock screen, encryption, biometric auth
3. **Photo Management** - Camera, gallery, photo types
4. **Milestone Tracking** - CRUD operations, timeline view
5. **Advanced Features** - Firebase sync, backup/export, ML features
6. **Cross-Platform** - iOS-specific adjustments
7. **Testing & Polish** - Comprehensive testing, performance optimization

### Technology Mapping

| Android | Flutter | Notes |
|---------|---------|-------|
| Kotlin/Java | Dart | Primary language |
| Room | sqflite + sqflite_sqlcipher | Database |
| SharedPreferences | shared_preferences | Simple storage |
| EncryptedSharedPreferences | flutter_secure_storage | Encrypted storage |
| CameraX | camera | Camera functionality |
| Picasso | cached_network_image / Image | Image loading |
| Firebase | firebase_* packages | Same services |
| Biometric | local_auth | Biometric auth |
| ML Kit | google_mlkit_* | Face detection |
| Material Components | Material Design 3 | UI framework |
| Navigation Component | Navigator / go_router | Navigation |
| Wear OS Data Layer | N/A | Not supported in initial release |

### Features Comparison

#### Implemented in Flutter (Basic Structure)
- ✅ App structure and navigation
- ✅ Splash screen
- ✅ Lock screen (basic)
- ✅ Settings UI
- ✅ Database service
- ✅ Storage service
- ✅ Authentication service

#### To Be Implemented
- ⏳ Photo capture and management
- ⏳ Milestone CRUD operations
- ⏳ Gallery views and filters
- ⏳ Encryption (database password)
- ⏳ Decoy vault
- ⏳ Backup/Export
- ⏳ TransTracks import
- ⏳ Firebase sync
- ⏳ ML face detection
- ⏳ Themes and customization
- ⏳ Ads integration

#### Not Planned for Initial Release
- ❌ Wear OS companion app (limited Flutter support)
- ❌ Audio analysis features (if any existed)

## Data Migration

### Database Migration

Users will need to export their data from the Android version and import it into the Flutter version. We will provide:

1. **Export from Android** - Export database to JSON format
2. **Import to Flutter** - Import JSON into new SQLite database
3. **Photo file transfer** - Copy photo files to new app directory

### Migration Tool

A migration utility will be created to:
- Export Android Room database to JSON
- Validate data integrity
- Import into Flutter SQLite database
- Transfer photo files
- Maintain photo references

## Development Timeline

### Sprint 1: Foundation (Current)
- [x] Project setup
- [x] Basic screens
- [x] Core services
- [ ] Navigation implementation
- [ ] State management setup

### Sprint 2: Photo Features
- [ ] Camera integration
- [ ] Photo storage
- [ ] Gallery view
- [ ] Photo types (face/body/custom)
- [ ] Photo CRUD operations

### Sprint 3: Milestone Features
- [ ] Milestone CRUD
- [ ] Timeline view
- [ ] Milestone types
- [ ] Date picker integration

### Sprint 4: Security
- [ ] PIN lock implementation
- [ ] Biometric integration
- [ ] Database encryption
- [ ] Decoy vault

### Sprint 5: Advanced Features
- [ ] Backup/Export
- [ ] Import from TransTracks
- [ ] Firebase integration
- [ ] ML face detection

### Sprint 6: iOS & Polish
- [ ] iOS testing and fixes
- [ ] Performance optimization
- [ ] UI/UX refinements
- [ ] Accessibility improvements

### Sprint 7: Testing & Launch
- [ ] Unit tests
- [ ] Integration tests
- [ ] Beta testing
- [ ] App store submission

## Known Challenges

1. **Wear OS Support** - Flutter's Wear OS support is limited. May need to keep Android Wear app.
2. **Database Encryption** - SQLCipher implementation differs from Android
3. **ML Kit** - Different API surface, need careful testing
4. **Platform Differences** - iOS vs Android permissions and behaviors
5. **Photo Management** - Different file systems and permissions
6. **Migration Complexity** - Need robust data migration tools

## Testing Strategy

1. **Unit Tests** - All business logic and data services
2. **Widget Tests** - UI components and screens
3. **Integration Tests** - End-to-end user flows
4. **Platform Testing** - Both iOS and Android
5. **Migration Testing** - Data migration from Android version
6. **Performance Testing** - App launch, photo loading, database queries

## Success Criteria

- [ ] Feature parity with Android version
- [ ] Runs on both iOS and Android
- [ ] Smooth data migration from Android
- [ ] Passes all tests
- [ ] Performance meets or exceeds Android version
- [ ] Positive beta user feedback
- [ ] Security audit passed

## Resources

- [Flutter Documentation](https://docs.flutter.dev/)
- [Material Design 3](https://m3.material.io/)
- [Firebase for Flutter](https://firebase.google.com/docs/flutter/setup)
- [Android to Flutter Migration Guide](https://docs.flutter.dev/get-started/flutter-for/android-devs)
