# Wear OS Companion App - Features & Functionality

## What is the Wear OS Companion App?

The **OpenTransition Wear OS app** is a companion application designed to run on Android smartwatches (Wear OS 3.0+). It works together with the main mobile app to provide quick access to key features right from your wrist.

## Core Philosophy

The Wear OS app is designed as a **true companion** - it doesn't try to replicate all features of the mobile app, but instead focuses on:

1. **Quick Actions** - Fast access to frequently-used features
2. **Glanceable Information** - See your progress at a glance
3. **Notifications** - Stay updated on your journey
4. **Privacy** - Sensitive data stays on your phone

## Key Features

### 1. 📸 Quick Photo Trigger

**What it does:**
- Tap a button on your watch to trigger the camera on your phone
- Useful when you want to take consistent selfies without touching your phone
- Maintains the same angle and distance for better progress tracking

**How it works:**
- Tap "Take Photo" on your watch
- Your phone's camera opens automatically
- Photo is saved to your mobile app as usual

**Use case:** 
Take your daily/weekly progress photo without picking up your phone - great for maintaining consistency in lighting and angle.

### 2. 📊 Milestone Summary

**What it does:**
- Shows total number of milestones
- Displays your most recent milestone
- Quick view of your progress

**How it works:**
- Data syncs from your phone automatically
- Cached locally on watch for offline viewing
- Updates when new milestones are added

**Use case:**
Quick reminder of how far you've come during your day without opening your phone.

### 3. 🔄 Manual Sync

**What it does:**
- Request fresh data from your phone
- Ensures you have the latest milestone information
- Confirms connection to mobile app

**How it works:**
- Tap "Sync Now" button
- Watch sends request to phone
- Phone responds with latest data

**Use case:**
Force a refresh after adding new milestones on your phone.

### 4. 🔗 Connection Status

**What it does:**
- Shows whether your phone is connected and reachable
- Disables features when phone is not available
- Real-time connection monitoring

**How it works:**
- Uses Bluetooth and WiFi for communication
- Automatically detects when phone app is installed
- Updates status when connection changes

**Use case:**
Know at a glance if your watch can communicate with your phone.

## How It's a Companion (Not Standalone)

The Wear OS app is designed to **complement**, not replace, the mobile app:

### Mobile App Does (Full Features):
- ✅ Store all photos and data
- ✅ Full milestone management (create, edit, delete)
- ✅ Photo gallery and comparison
- ✅ Data backup and sync
- ✅ Settings and customization
- ✅ Privacy features (app lock, disguised mode)
- ✅ Firebase integration
- ✅ Cloud sync

### Wear App Does (Quick Access):
- ✅ Trigger photo capture remotely
- ✅ View milestone summary
- ✅ Request data sync
- ✅ Connection status
- ⏳ View recent milestones (planned)
- ⏳ Quick stats/progress (planned)
- ⏳ Receive notifications (planned)

### Wear App Does NOT:
- ❌ Store photos (too large for watch storage)
- ❌ Edit milestones (use phone for editing)
- ❌ Display photo gallery (screen too small)
- ❌ Manage settings (use phone)
- ❌ Work without phone app (requires companion)

## Technical: How They Communicate

### Wearable Data Layer API
The apps communicate using Google's **Wearable Data Layer**, which provides:

1. **Messages** - One-way commands (e.g., "take a photo")
2. **Data Items** - Synchronized data (e.g., milestone list)
3. **Capabilities** - Device detection (e.g., "is phone app installed?")

### Communication Examples:

**Photo Trigger (Watch → Phone):**
```
User taps button on watch
    ↓
Watch sends message: "trigger_photo"
    ↓
Phone receives message
    ↓
Phone opens camera app
```

**Milestone Sync (Phone → Watch):**
```
User creates milestone on phone
    ↓
Phone packages milestone data
    ↓
Phone syncs to watch via Data Layer
    ↓
Watch receives data update
    ↓
Watch updates display
```

**Connection Detection:**
```
Watch checks for "mobile_app" capability
    ↓
If found: Enable buttons, show "Connected"
    ↓
If not found: Disable buttons, show "Disconnected"
```

## Privacy & Security

### What Data is Shared:
- ✅ Milestone titles and dates
- ✅ Milestone count
- ✅ Sync timestamps

### What Data is NOT Shared:
- ❌ Photos (too large, too sensitive)
- ❌ Personal notes/descriptions (privacy)
- ❌ Authentication tokens
- ❌ Cloud sync credentials

### Security Features:
- Encrypted in transit (handled by Google Play Services)
- Data cached only on your watch (not cloud)
- No third-party servers involved
- Bluetooth/WiFi communication only (no internet required)

## User Experience

### First Time Setup:
1. Install mobile app on phone
2. Install Wear app on watch
3. Apps automatically detect each other
4. No manual pairing needed (uses Bluetooth pairing)

### Daily Use:
1. Glance at watch for milestone count
2. Tap to trigger photo when ready
3. Continue using mobile app for detailed work

### When Phone is Not Nearby:
- Cached milestone data still visible
- Photo trigger disabled (needs phone)
- Sync disabled (needs phone)
- View-only mode

## Future Enhancements (Planned)

### Short Term:
- [ ] **Milestone List View** - Scroll through recent milestones
- [ ] **Progress Stats** - Days since first milestone, total photos
- [ ] **Customizable Photo Type** - Choose face/body/custom

### Medium Term:
- [ ] **Notification Support** - Milestone reminders on watch
- [ ] **Voice Notes** - Quick voice memos from watch
- [ ] **Watch Complications** - Show stats on watch face

### Long Term:
- [ ] **Standalone Mode** - Basic features without phone
- [ ] **Quick Sharing** - Share milestones from watch
- [ ] **Themes** - Match mobile app themes

## Comparison: Before vs After

### Before (No Wear App):
- Pick up phone to take progress photo
- Unlock phone
- Open OpenTransition app
- Navigate to camera
- Take photo
- Risk inconsistent angles/lighting

### After (With Wear App):
- Tap watch button
- Phone camera opens automatically
- Take photo
- Consistent positioning every time

## Use Cases

### 1. Daily Progress Photos
**Scenario:** You want to take a photo at the same time every day.
**How Wear Helps:** Quick trigger from watch without interrupting your routine.

### 2. Motivation Check
**Scenario:** You need a quick reminder of your progress.
**How Wear Helps:** Glance at watch to see milestone count and latest achievement.

### 3. Hands-Free Operation
**Scenario:** You're in front of a mirror and want hands-free photo capture.
**How Wear Helps:** Trigger camera with watch, no need to reach for phone.

### 4. Privacy in Public
**Scenario:** You want to check your progress discreetly.
**How Wear Helps:** Quick glance at watch more subtle than opening phone app.

## Limitations

### Technical:
- Requires Wear OS 3.0+ (older versions not supported)
- Requires Android phone with OpenTransition mobile app
- Bluetooth/WiFi range limited (typically 30 feet)
- Watch battery life impacted by frequent syncing

### Design:
- Small screen limits detailed information
- No photo viewing (screen too small, privacy concern)
- No editing features (too complex for watch UI)
- Read-mostly interface (write operations on phone)

## Summary

The Wear OS companion app is designed to be a **quick-access remote control** for your OpenTransition journey:

- 🎯 **Purpose:** Quick actions and glanceable stats
- 🔗 **Relationship:** Companion to mobile app, not replacement
- 🛡️ **Privacy:** Minimal data, maximum convenience
- ⚡ **Speed:** Faster access to common actions
- 👁️ **Glanceable:** See progress without phone

Think of it as a **remote control for your transition tracking** - keeping the detailed work on your phone while putting the most common actions on your wrist.
