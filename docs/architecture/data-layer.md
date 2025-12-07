# Data Layer

The Data Layer manages all data models, database operations, and persistence in OpenTransition.

## Overview

The data layer consists of:
- **Data Models**: Realm objects representing app entities
- **Database**: Realm Kotlin for local storage
- **Cloud Sync**: Firebase Firestore for remote backup
- **File Storage**: Local file system for photos

## Data Models

### Photo Model

The `Photo` class represents transition photos stored by users.

**File**: `app/src/main/java/com/shelbeely/opentransition/data/Photo.kt`

```kotlin
class Photo : RealmObject {
    @PrimaryKey var id: String = UUID.randomUUID().toString()
    var timestamp: Long = 0L
    var epochDay: Long = 0L
    var filename: String = ""
    var type: Int = TYPE_BODY
    
    companion object {
        const val TYPE_FACE = 0
        const val TYPE_BODY = 1
        const val TYPE_CUSTOM = 2
    }
}
```

**Key Properties**:
- `id`: Unique identifier (UUID)
- `timestamp`: Photo capture time in milliseconds
- `epochDay`: Date as epoch day for grouping
- `filename`: Stored filename on device
- `type`: Photo category (face, body, or custom)

**Photo Types**:
- **Face** (0): Facial progress photos
- **Body** (1): Full body progress photos  
- **Custom** (2): User-defined areas

### Milestone Model

The `Milestone` class represents significant events in the transition journey.

**File**: `app/src/main/java/com/shelbeely/opentransition/data/Milestone.kt`

```kotlin
class Milestone : RealmObject {
    @PrimaryKey var id: String = UUID.randomUUID().toString()
    var title: String = ""
    var description: String = ""
    var epochDay: Long = 0L
    var timestamp: Long = 0L
}
```

**Key Properties**:
- `id`: Unique identifier (UUID)
- `title`: Milestone name
- `description`: Detailed description
- `epochDay`: Date as epoch day
- `timestamp`: Creation time in milliseconds

**Common Milestone Examples**:
- Starting HRT
- Name change
- Coming out milestones
- Medical procedures
- Legal document updates

## Database: Realm Kotlin

### Configuration

Realm is configured in the Application class and opened throughout the app:

```kotlin
// Open default Realm instance
val realm = Realm.openDefault()

// Always close when done
realm.close()
```

### Database Operations

#### Create (Insert)

```kotlin
val realm = Realm.openDefault()
realm.write {
    val photo = Photo().apply {
        id = UUID.randomUUID().toString()
        timestamp = System.currentTimeMillis()
        filename = "photo_${timestamp}.jpg"
        type = Photo.TYPE_BODY
    }
    copyToRealm(photo, UpdatePolicy.ALL)
}
realm.close()
```

#### Read (Query)

```kotlin
val realm = Realm.openDefault()

// Get all photos
val allPhotos = realm.query<Photo>().find()

// Filter by type
val facePhotos = realm.query<Photo>("type == $0", Photo.TYPE_FACE).find()

// Sort by date
val sortedPhotos = realm.query<Photo>()
    .sort("timestamp", Sort.DESCENDING)
    .find()
    
realm.close()
```

#### Update

```kotlin
val realm = Realm.openDefault()
realm.write {
    val photo = query<Photo>("id == $0", photoId).first().find()
    photo?.let {
        it.type = Photo.TYPE_CUSTOM
    }
}
realm.close()
```

#### Delete

```kotlin
val realm = Realm.openDefault()
realm.write {
    val photo = query<Photo>("id == $0", photoId).first().find()
    photo?.let { delete(it) }
}
realm.close()
```

### Reactive Queries

Realm supports reactive queries that automatically update:

```kotlin
val realm = Realm.openDefault()
val photosFlow: Flow<ResultsChange<Photo>> = realm.query<Photo>()
    .sort("timestamp", Sort.DESCENDING)
    .asFlow()

// Observe changes
photosFlow
    .map { it.list }
    .collect { photos ->
        // Update UI with latest photos
    }
```

## File Storage

### Photo Files

Photos are stored in the app's private storage:

```kotlin
object FileUtil {
    fun getImageFile(filename: String): File {
        val directory = File(context.filesDir, "images")
        if (!directory.exists()) directory.mkdirs()
        return File(directory, filename)
    }
    
    fun getTempFile(filename: String): File {
        val directory = File(context.filesDir, "temp")
        if (!directory.exists()) directory.mkdirs()
        return File(directory, filename)
    }
}
```

### File Operations

#### Saving Photos

```kotlin
fun copyPhotoToTempFiles(uri: Uri): String? {
    try {
        val inputStream = contentResolver.openInputStream(uri)
        val filename = "photo_${System.currentTimeMillis()}.jpg"
        val outputFile = FileUtil.getImageFile(filename)
        
        inputStream?.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }
        return filename
    } catch (e: Exception) {
        return null
    }
}
```

#### Deleting Photos

```kotlin
fun deletePhoto(filename: String): Boolean {
    val file = FileUtil.getImageFile(filename)
    return if (file.exists()) {
        file.delete()
    } else false
}
```

## Cloud Sync: Firebase

### Firestore Structure

```
users/
  {userId}/
    photos/
      {photoId}/
        - timestamp
        - epochDay
        - filename
        - type
    milestones/
      {milestoneId}/
        - title
        - description
        - epochDay
        - timestamp
    settings/
      - theme
      - lockType
      - startDate
      - ...
```

### Syncing Data

```kotlin
class FirebaseSettingUtil {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    fun syncPhotos() {
        val userId = auth.currentUser?.uid ?: return
        val photosRef = firestore.collection("users")
            .document(userId)
            .collection("photos")
        
        val realm = Realm.openDefault()
        val photos = realm.query<Photo>().find()
        
        photos.forEach { photo ->
            val photoData = hashMapOf(
                "id" to photo.id,
                "timestamp" to photo.timestamp,
                "epochDay" to photo.epochDay,
                "type" to photo.type
                // Note: Actual photo files stored separately
            )
            photosRef.document(photo.id).set(photoData)
        }
        realm.close()
    }
}
```

## Data Import/Export

### Export Format

Data is exported as a ZIP file containing:
- `data.json`: JSON file with all metadata
- Photo files: All images referenced in the data

### JSON Structure

```json
{
  "settings": {
    "theme": "pink",
    "lockType": "normal",
    "startDate": 1234567890
  },
  "photos": [
    {
      "id": "uuid-here",
      "timestamp": 1234567890000,
      "epochDay": 19000,
      "filename": "photo_123.jpg",
      "type": 1
    }
  ],
  "milestones": [
    {
      "id": "uuid-here",
      "title": "Started HRT",
      "description": "Began hormone therapy today!",
      "epochDay": 19000,
      "timestamp": 1234567890000
    }
  ]
}
```

### Import Process

1. Extract ZIP file
2. Read `data.json`
3. Parse JSON and validate
4. Copy photo files to storage
5. Write data to Realm database
6. Update settings

## Data Validation

### Photo Validation

```kotlin
fun validatePhoto(photo: Photo): Boolean {
    return photo.id.isNotEmpty() &&
           photo.filename.isNotEmpty() &&
           photo.timestamp > 0 &&
           photo.type in 0..2
}
```

### Milestone Validation

```kotlin
fun validateMilestone(milestone: Milestone): Boolean {
    return milestone.id.isNotEmpty() &&
           milestone.title.isNotEmpty() &&
           milestone.timestamp > 0
}
```

## Data Migration

Realm handles schema migrations automatically for simple changes. For complex migrations:

```kotlin
val config = RealmConfiguration.Builder(schema = setOf(Photo::class, Milestone::class))
    .schemaVersion(2)
    .migration { context ->
        // Handle migration from version 1 to 2
    }
    .build()
```

## Best Practices

### Always Close Realm

```kotlin
val realm = Realm.openDefault()
try {
    // Use realm
} finally {
    realm.close()
}
```

### Use Transactions for Writes

```kotlin
realm.write {
    // All write operations here
}
```

### Query on Background Thread

```kotlin
Observable.fromCallable {
    val realm = Realm.openDefault()
    val photos = realm.query<Photo>().find()
    realm.close()
    photos
}
.subscribeOn(RxSchedulers.io())
.observeOn(RxSchedulers.main())
.subscribe { photos -> updateUI(photos) }
```

### Handle Errors Gracefully

```kotlin
try {
    val realm = Realm.openDefault()
    // database operations
    realm.close()
} catch (e: Exception) {
    FirebaseCrashlytics.getInstance().recordException(e)
    // Show user-friendly error message
}
```

## Next Steps

- Learn about [Domain Layer](domain-layer.md)
- Understand [UI Layer](ui-layer.md)
- Explore [Photo Tracking Feature](../features/photo-tracking.md)
