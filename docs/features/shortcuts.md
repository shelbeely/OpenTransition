# App Shortcuts

*Developer documentation for the App Shortcuts feature.*

## Overview

App Shortcuts provide quick access to key app features directly from the launcher. Users can long-press the app icon to see available shortcuts and jump directly to specific screens or actions within OpenTransition.

## What are App Shortcuts?

App Shortcuts are a standard Android feature that allows apps to expose deep links to specific actions or content. OpenTransition implements both **static shortcuts** (always available) and **dynamic shortcuts** (updated with recent content).

## Features

### Static Shortcuts
Static shortcuts are defined in XML and always available in the launcher:

1. **Take Photo** - Quick access to camera
2. **View Gallery** - Quick access to photo gallery
3. **View Milestones** - Quick access to milestones
4. **Record Audio** - Quick access to audio recording

### Dynamic Shortcuts
Dynamic shortcuts are programmatically created to show recent content:

1. **Recent Photos** - Up to 3 most recent photos
2. **Recent Milestones** - Up to 3 most recent milestones

## Implementation

### Architecture

The shortcuts implementation consists of:

1. **Static Shortcuts XML** - Defines permanent shortcuts
2. **ShortcutManagerUtil** - Manages dynamic shortcuts
3. **MainActivity** - Handles shortcut activation
4. **Shortcut Icons** - Vector drawables for each shortcut

### Static Shortcuts

Static shortcuts are defined in `mobile/src/main/res/xml/shortcuts.xml`:

```xml
<shortcuts xmlns:android="http://schemas.android.com/apk/res/android">
    <shortcut
        android:shortcutId="take_photo"
        android:enabled="true"
        android:icon="@drawable/ic_shortcut_camera"
        android:shortcutShortLabel="@string/shortcut_take_photo_short"
        android:shortcutLongLabel="@string/shortcut_take_photo_long">
        <intent
            android:action="android.intent.action.VIEW"
            android:targetPackage="com.shelbeely.opentransition"
            android:targetClass="com.shelbeely.opentransition.ui.MainActivity">
            <extra android:name="shortcut_action" android:value="take_photo" />
        </intent>
    </shortcut>
    
    <!-- Additional shortcuts... -->
</shortcuts>
```

#### Static Shortcuts Defined

| Shortcut ID | Short Label | Long Label | Action | Icon |
|-------------|-------------|------------|--------|------|
| `take_photo` | Take Photo | Take a Photo | Opens camera | ic_shortcut_camera |
| `view_gallery` | Gallery | View Gallery | Opens gallery | ic_shortcut_gallery |
| `view_milestones` | Milestones | View Milestones | Opens milestones | ic_shortcut_milestones |
| `record_audio` | Record Audio | Record Audio | Opens audio recording | ic_shortcut_audio |

### Dynamic Shortcuts

Dynamic shortcuts are managed by `ShortcutManagerUtil`:

**Location:** `mobile/src/main/java/com/shelbeely/opentransition/shortcuts/ShortcutManagerUtil.kt`

#### Key Methods

**Update Recent Photos:**
```kotlin
fun updateRecentPhotos(context: Context, recentPhotos: List<PhotoEntity>)
```

Creates shortcuts for up to 3 most recent photos with:
- Short label: Photo type and date (e.g., "Face Jan 06, 2026")
- Long label: Full description (e.g., "Face photo from Jan 06, 2026")
- Icon: Camera icon
- Intent: Opens MainActivity with photo ID

**Update Recent Milestones:**
```kotlin
fun updateRecentMilestones(context: Context, recentMilestones: List<MilestoneEntity>)
```

Creates shortcuts for up to 3 most recent milestones with:
- Short label: First 20 characters of title
- Long label: Full milestone title
- Icon: Milestones icon
- Intent: Opens MainActivity with milestone ID

**Report Shortcut Usage:**
```kotlin
fun reportShortcutUsed(context: Context, shortcutId: String)
```

Reports when a shortcut is used to help Android rank shortcuts by usage.

**Remove All Dynamic Shortcuts:**
```kotlin
fun removeAllDynamicShortcuts(context: Context)
```

Clears all dynamic shortcuts (useful when clearing app data).

### Shortcut Icons

Custom vector drawable icons are provided for each shortcut:

**Location:** `mobile/src/main/res/drawable/`

- `ic_shortcut_camera.xml` - Camera icon for photo shortcuts
- `ic_shortcut_gallery.xml` - Gallery grid icon
- `ic_shortcut_milestones.xml` - Star/milestone icon
- `ic_shortcut_audio.xml` - Microphone icon

### MainActivity Integration

`MainActivity` handles shortcut activation in `onCreate()`:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Handle shortcut intent
    intent?.getStringExtra("shortcut_action")?.let { action ->
        when (action) {
            "take_photo" -> navigateToCamera()
            "view_gallery" -> navigateToGallery()
            "view_milestones" -> navigateToMilestones()
            "record_audio" -> navigateToAudioRecording()
            "view_photo" -> {
                val photoId = intent.getStringExtra("photo_id")
                navigateToPhoto(photoId)
            }
            "view_milestone" -> {
                val milestoneId = intent.getStringExtra("milestone_id")
                navigateToMilestone(milestoneId)
            }
        }
    }
}
```

## Usage

### Creating Dynamic Shortcuts

Update dynamic shortcuts when relevant content changes:

**Example - Update shortcuts with recent photos:**
```kotlin
// Get recent photos from database
val recentPhotos = photoRepository.getRecentPhotos(limit = 3)

// Update shortcuts
ShortcutManagerUtil.updateRecentPhotos(context, recentPhotos)
```

**Example - Update shortcuts with recent milestones:**
```kotlin
// Get recent milestones from database
val recentMilestones = milestoneRepository.getRecentMilestones(limit = 3)

// Update shortcuts
ShortcutManagerUtil.updateRecentMilestones(context, recentMilestones)
```

### When to Update Dynamic Shortcuts

Dynamic shortcuts should be updated:

1. **On App Startup** - Refresh with latest content
2. **After Creating Content** - Add new photo/milestone to shortcuts
3. **After Deleting Content** - Update if deleted item was in shortcuts
4. **Periodically** - During app usage to keep shortcuts fresh

### Best Practices

1. **Limit to 3 items** - ShortcutManager has a maximum limit of dynamic shortcuts
2. **Use relevant titles** - Keep short labels under 20 characters
3. **Report usage** - Call `reportShortcutUsed()` when shortcuts are activated
4. **Handle missing content** - Gracefully handle if shortcut points to deleted content
5. **Clear when appropriate** - Remove shortcuts when clearing app data

## Configuration

### AndroidManifest.xml

Shortcuts must be declared in the manifest:

```xml
<activity
    android:name=".ui.MainActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
    
    <!-- Shortcuts metadata -->
    <meta-data
        android:name="android.app.shortcuts"
        android:resource="@xml/shortcuts" />
</activity>
```

### Strings Resources

Shortcut labels are defined in `mobile/src/main/res/values/strings.xml`:

```xml
<!-- Static Shortcuts -->
<string name="shortcut_take_photo_short">Take Photo</string>
<string name="shortcut_take_photo_long">Take a Photo</string>
<string name="shortcut_view_gallery_short">Gallery</string>
<string name="shortcut_view_gallery_long">View Gallery</string>
<string name="shortcut_view_milestones_short">Milestones</string>
<string name="shortcut_view_milestones_long">View Milestones</string>
<string name="shortcut_record_audio_short">Record Audio</string>
<string name="shortcut_record_audio_long">Record Audio</string>
<string name="shortcut_disabled_message">This shortcut is currently unavailable</string>
```

## User Experience

### Accessing Shortcuts

**On Most Launchers:**
1. Long-press the OpenTransition app icon
2. A menu appears with available shortcuts
3. Tap a shortcut to perform the action

**Creating Home Screen Shortcuts:**
1. Long-press the app icon
2. Long-press a shortcut in the menu
3. Drag it to the home screen
4. Release to create a standalone shortcut icon

### Shortcut Behavior

**Static Shortcuts:**
- Always present when long-pressing the app icon
- Navigate to the specified screen
- Available even if app hasn't been opened recently

**Dynamic Shortcuts:**
- Show recent photos or milestones
- Updated based on app usage
- May change as new content is created
- Ranked by Android based on usage patterns

## Limitations

### Android Version Support

- **Static Shortcuts:** Android 7.1+ (API 25+)
- **Dynamic Shortcuts:** Android 7.1+ (API 25+)
- **Pinned Shortcuts:** Android 8.0+ (API 26+)

### Shortcut Limits

| Type | Maximum Count |
|------|---------------|
| Static Shortcuts | 4-5 (system dependent) |
| Dynamic Shortcuts | 5-15 (system dependent) |
| Combined Total | Varies by Android version |

OpenTransition uses:
- 4 static shortcuts
- Up to 3 dynamic shortcuts

### Best Practices for Limits

1. **Prioritize important actions** - Use static shortcuts for most-used features
2. **Keep dynamic count low** - 3 recent items is optimal
3. **Don't exceed limits** - System will truncate excess shortcuts
4. **Test on multiple devices** - Limits vary by manufacturer

## Privacy Considerations

### Sensitive Content

Shortcut labels may be visible:
- In the launcher's shortcuts menu
- On the home screen if pinned
- In system logs
- To accessibility services

**Privacy measures:**
- Shortcut labels use generic terms (e.g., "Face Jan 06")
- No sensitive details in labels
- Icons don't show actual photo content
- Respects app lock settings

### App Lock Integration

When app lock is enabled:
- Shortcuts still appear in the menu
- Tapping a shortcut opens the app
- Lock screen is shown before accessing content
- User must authenticate to view content

## Testing

### Manual Testing

**Test Static Shortcuts:**
1. Long-press the app icon
2. Verify all 4 static shortcuts appear
3. Tap each shortcut
4. Verify correct screen opens

**Test Dynamic Shortcuts:**
1. Create 3+ photos
2. Call `updateRecentPhotos()`
3. Long-press app icon
4. Verify recent photos appear as shortcuts
5. Tap a dynamic shortcut
6. Verify correct photo opens

**Test Pinned Shortcuts (Android 8.0+):**
1. Long-press app icon
2. Long-press a shortcut
3. Drag to home screen
4. Tap the pinned shortcut
5. Verify it works correctly

### Automated Testing

```kotlin
@Test
fun testUpdateRecentPhotos() {
    val photos = createTestPhotos(count = 5)
    
    ShortcutManagerUtil.updateRecentPhotos(context, photos)
    
    val shortcuts = ShortcutManagerCompat.getDynamicShortcuts(context)
    assertEquals(3, shortcuts.size) // Maximum of 3
    assertEquals("recent_photo_${photos[0].id}", shortcuts[0].id)
}

@Test
fun testShortcutIntent() {
    val intent = Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        putExtra("shortcut_action", "view_gallery")
    }
    
    val scenario = ActivityScenario.launch<MainActivity>(intent)
    
    scenario.onActivity { activity ->
        // Verify gallery screen is displayed
        assertTrue(activity.isGalleryDisplayed())
    }
}
```

## Troubleshooting

### Shortcuts Not Appearing

**Possible causes:**
1. Android version too old (< 7.1)
2. Launcher doesn't support shortcuts
3. Manifest configuration missing
4. Shortcuts XML has errors

**Solutions:**
1. Check Android version: Settings → About phone
2. Test on different launcher (e.g., Google Pixel Launcher)
3. Verify manifest contains shortcuts metadata
4. Validate XML syntax in `shortcuts.xml`

### Dynamic Shortcuts Not Updating

**Common issues:**
- `ShortcutManagerCompat.setDynamicShortcuts()` not called
- Maximum shortcuts exceeded
- Invalid intent configuration
- App not in foreground when updating

**Debugging:**
```kotlin
val maxShortcuts = ShortcutManagerCompat.getMaxShortcutCountPerActivity(context)
Log.d("Shortcuts", "Max shortcuts allowed: $maxShortcuts")

val currentShortcuts = ShortcutManagerCompat.getDynamicShortcuts(context)
Log.d("Shortcuts", "Current shortcuts: ${currentShortcuts.size}")
```

### Shortcut Icons Not Displaying

**Possible causes:**
- Icon resource not found
- Invalid vector drawable
- API level compatibility issues

**Solutions:**
1. Verify icon exists in `res/drawable/`
2. Test vector drawable in layout preview
3. Use `IconCompat.createWithResource()` for compatibility
4. Provide PNG fallback for older devices

## Future Enhancements

- [ ] Pinned shortcuts for favorite milestones
- [ ] Conversation shortcuts for specific photo types
- [ ] Shortcuts for comparison views
- [ ] Shortcuts for export/backup
- [ ] Adaptive icons for shortcuts
- [ ] Localized shortcut labels
- [ ] Sharing shortcuts (Android 10+)

## Related Documentation

- [Navigation](../architecture/navigation.md) - App navigation flow
- [Photo Tracking](photo-tracking.md) - Photo feature documentation
- [Milestones](milestones.md) - Milestone feature documentation
- [AppSearch](app-search.md) - Content search integration
- [UI Layer](../architecture/ui-layer.md) - UI architecture

## References

- [Android App Shortcuts Guide](https://developer.android.com/guide/topics/ui/shortcuts)
- [ShortcutManager API](https://developer.android.com/reference/android/content/pm/ShortcutManager)
- [ShortcutManagerCompat](https://developer.android.com/reference/androidx/core/content/pm/ShortcutManagerCompat)
- [Shortcuts Best Practices](https://developer.android.com/guide/topics/ui/shortcuts/best-practices)
