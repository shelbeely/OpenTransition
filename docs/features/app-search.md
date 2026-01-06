# AppSearch Integration

*Developer documentation for the AppSearch feature.*

## Overview

The AppSearch integration enables system-wide search of app content, allowing users to find their photos and milestones directly from Android's system search. This feature uses AndroidX AppSearch to index content and make it discoverable outside the app.

## What is AppSearch?

AppSearch is a high-performance, on-device search library that allows apps to index their content for system-wide search. When users search from their device's settings or search interface, indexed content from OpenTransition can appear in the results.

## Features

### Content Indexing
- **Photos**: Indexed by type (face, body, audio) and date
- **Milestones**: Indexed by title and description
- **Automatic Ranking**: System prioritizes results by relevance
- **Real-time Updates**: Content is indexed as it's created or updated

### Search Capabilities
Users can find content by:
- Photo type (e.g., "face photo", "body photo", "audio")
- Date ranges
- Milestone titles and descriptions
- Relevant keywords

## Implementation

### Architecture

The AppSearch implementation consists of three main components:

#### 1. Document Classes

Document classes define how app content is represented in the search index:

**PhotoDocument.kt** - Represents photos in the search index:
```kotlin
@Document
data class PhotoDocument(
    @Namespace val namespace: String,
    @Id val id: String,
    @Score val score: Int,
    @LongProperty val timestamp: Long,
    @LongProperty val epochDay: Long,
    @StringProperty val type: String,
    @StringProperty val typeName: String,
    @StringProperty val filePath: String
)
```

**MilestoneDocument.kt** - Represents milestones in the search index:
```kotlin
@Document
data class MilestoneDocument(
    @Namespace val namespace: String,
    @Id val id: String,
    @Score val score: Int,
    @LongProperty val timestamp: Long,
    @LongProperty val epochDay: Long,
    @StringProperty val title: String,
    @StringProperty val description: String
)
```

#### 2. AppSearchManager

The `AppSearchManager` class manages the AppSearch session and handles all indexing operations:

**Key responsibilities:**
- Initialize and manage AppSearch session
- Set schema for document types
- Index photos and milestones
- Remove content from index
- Search indexed content
- Handle session lifecycle

**Location:** `mobile/src/main/java/com/shelbeely/opentransition/appsearch/AppSearchManager.kt`

#### 3. SearchIndexingUtil

The `SearchIndexingUtil` provides a simple API for indexing content throughout the app:

**Location:** `mobile/src/main/java/com/shelbeely/opentransition/util/SearchIndexingUtil.kt`

### Initialization

AppSearch is initialized in the `TransTracksApp.onCreate()` method:

```kotlin
class TransTracksApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize AppSearch for system-wide content search
        AppSearchManager.getInstance(this)
    }
}
```

### Usage

#### Indexing Content

To index photos and milestones, use the `SearchIndexingUtil` methods:

```kotlin
// Index a photo
SearchIndexingUtil.indexPhoto(context, photoEntity, typeName)

// Index a milestone
SearchIndexingUtil.indexMilestone(context, milestoneEntity)
```

**Example - Indexing a new photo:**
```kotlin
val photo = PhotoEntity(
    id = UUID.randomUUID().toString(),
    timestamp = System.currentTimeMillis(),
    epochDay = LocalDate.now().toEpochDay(),
    type = Photo.TYPE_FACE,
    filePath = "/path/to/photo.jpg"
)

// Index with human-readable type name
SearchIndexingUtil.indexPhoto(context, photo, "Face")
```

**Example - Indexing a new milestone:**
```kotlin
val milestone = MilestoneEntity(
    id = UUID.randomUUID().toString(),
    timestamp = System.currentTimeMillis(),
    epochDay = LocalDate.now().toEpochDay(),
    title = "Started HRT",
    description = "Beginning hormone replacement therapy"
)

SearchIndexingUtil.indexMilestone(context, milestone)
```

#### Removing Content from Index

When content is deleted, remove it from the search index:

```kotlin
// Remove a photo
SearchIndexingUtil.removePhoto(context, photoId)

// Remove a milestone
SearchIndexingUtil.removeMilestone(context, milestoneId)
```

### Integration Points

#### When to Index

Content should be indexed at the following points:

1. **Photo Creation**
   - After capturing a new photo
   - After importing photos
   - When photo metadata is updated

2. **Milestone Creation**
   - After creating a new milestone
   - When milestone is edited

3. **Bulk Operations**
   - After importing backup data
   - When restoring from backup

#### When to Remove from Index

Content should be removed from the index when:

1. Photo or milestone is deleted
2. User requests to clear app data
3. During app uninstallation (automatic)

## Data Model

### Photo Documents

| Field | Type | Description |
|-------|------|-------------|
| `namespace` | String | Always "photos" |
| `id` | String | Unique photo ID |
| `score` | Int | Relevance score (default: 1) |
| `timestamp` | Long | Photo timestamp in milliseconds |
| `epochDay` | Long | Date as epoch day |
| `type` | String | Photo type code ("0", "1", "2") |
| `typeName` | String | Human-readable type ("Face", "Body", "Audio") |
| `filePath` | String | Path to photo file |

### Milestone Documents

| Field | Type | Description |
|-------|------|-------------|
| `namespace` | String | Always "milestones" |
| `id` | String | Unique milestone ID |
| `score` | Int | Relevance score (default: 2, higher than photos) |
| `timestamp` | Long | Milestone timestamp in milliseconds |
| `epochDay` | Long | Date as epoch day |
| `title` | String | Milestone title (searchable) |
| `description` | String | Milestone description (searchable) |

## Dependencies

AppSearch requires the following dependencies in `mobile/build.gradle`:

```gradle
// AndroidX AppSearch
implementation("androidx.appsearch:appsearch:1.1.0-alpha05")
implementation("androidx.appsearch:appsearch-local-storage:1.1.0-alpha05")
kapt("androidx.appsearch:appsearch-compiler:1.1.0-alpha05")
```

See [Dependencies Documentation](../development/dependencies.md) for the complete list.

## Privacy Considerations

### Data Storage
- All AppSearch data is stored **locally on the device**
- No data is sent to external servers
- Data is automatically cleared when the app is uninstalled

### Search Visibility
- Content is only searchable on the user's device
- System search respects app permissions
- Content is not visible to other apps

### User Control
- Users can disable system search in Android settings
- Clearing app data removes all indexed content
- App lock and privacy features work independently of search

## User Experience

### Accessing Search Results

Users can access indexed content through:

1. **Android System Search** (Settings → Search)
2. **Device Search Interface** (varies by manufacturer)
3. **Google Search App** (if configured)

### Search Result Display

When users tap on a search result:
- The app opens to the relevant screen
- Direct navigation to the photo or milestone
- Seamless integration with app navigation

## Performance

### Indexing Performance
- Indexing is performed asynchronously
- Does not block the UI thread
- Silent failure if indexing fails (non-critical feature)

### Storage Impact
- Minimal storage overhead
- Efficient binary storage format
- Automatic cleanup of old entries

### Best Practices
1. Index content immediately after creation
2. Remove content from index when deleted
3. Handle indexing errors gracefully
4. Don't block user actions on indexing

## Testing

### Manual Testing

1. **Index Content:**
   - Create photos and milestones in the app
   - Verify content is indexed (check logs)

2. **Search Testing:**
   - Go to Android Settings → Search
   - Enable "Show app content"
   - Search for photo types or milestone titles
   - Verify results appear

3. **Removal Testing:**
   - Delete a photo or milestone
   - Verify it no longer appears in search

### Automated Testing

*Note: AppSearch testing requires instrumented tests on a device/emulator.*

```kotlin
@Test
fun testPhotoIndexing() {
    val photo = createTestPhoto()
    SearchIndexingUtil.indexPhoto(context, photo, "Face")
    
    // Wait for indexing
    Thread.sleep(1000)
    
    // Perform search and verify results
    val results = searchForPhotos("face")
    assertTrue(results.contains(photo.id))
}
```

## Troubleshooting

### Content Not Appearing in Search

**Possible causes:**
1. System search is disabled in Android settings
2. Indexing failed (check logs)
3. App search permissions not granted
4. Content was not properly indexed

**Solutions:**
1. Enable system search: Settings → Search → Show app content
2. Verify AppSearch initialization in `TransTracksApp`
3. Check logcat for indexing errors
4. Re-index content by recreating it

### Indexing Failures

**Common issues:**
- AppSearch session not initialized
- Invalid document format
- Insufficient storage space
- Android version compatibility

**Debugging:**
```kotlin
try {
    SearchIndexingUtil.indexPhoto(context, photo, typeName)
} catch (e: Exception) {
    Log.e("AppSearch", "Indexing failed", e)
}
```

## Future Enhancements

- [ ] Search within the app using AppSearch
- [ ] Advanced search filters (date ranges, types)
- [ ] Search suggestions and autocomplete
- [ ] Index additional content (audio analysis, custom types)
- [ ] Search analytics and insights
- [ ] Multi-language search support

## Related Documentation

- [Architecture Overview](../architecture/overview.md) - System architecture
- [Data Layer](../architecture/data-layer.md) - Data models and database
- [Photo Tracking](photo-tracking.md) - Photo feature documentation
- [Milestones](milestones.md) - Milestone feature documentation
- [Dependencies](../development/dependencies.md) - Dependency management
- [Shortcuts](shortcuts.md) - App Shortcuts feature

## References

- [AndroidX AppSearch Documentation](https://developer.android.com/reference/androidx/appsearch/package-summary)
- [AppSearch Guide](https://developer.android.com/guide/topics/search/appsearch)
- [System Search Best Practices](https://developer.android.com/guide/topics/search/searchable-config)
