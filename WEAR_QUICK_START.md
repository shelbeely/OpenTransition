# Quick Start Guide: Wear OS Companion App

## What is the Wear OS Companion App?

The OpenTransition Wear OS app is a **smart companion** that puts key features on your wrist:

```
┌─────────────────────────────────────────────────┐
│            YOUR TRANSITION JOURNEY              │
└─────────────────────────────────────────────────┘
                      │
         ┌────────────┴────────────┐
         │                         │
         ▼                         ▼
┌─────────────────┐       ┌─────────────────┐
│   📱 PHONE      │◄─────►│   ⌚ WATCH      │
│                 │       │                 │
│  Mobile App     │       │   Wear App      │
│  ─────────────  │       │  ─────────────  │
│                 │       │                 │
│ • Store photos  │       │ • Trigger       │
│ • Full features │       │   photos        │
│ • Edit data     │       │ • View count    │
│ • Backup/sync   │       │ • Quick check   │
│ • Settings      │       │ • Manual sync   │
│                 │       │                 │
│ THE FULL APP    │       │ THE REMOTE      │
└─────────────────┘       └─────────────────┘
```

## How They Work Together

### 🎯 Think of it Like This:

**Mobile App** = Your complete toolbox
- Has everything you need
- Full control over all features
- Stores all your data

**Wear App** = Your favorite tools on your belt
- Quick access to what you use most
- Lightweight and fast
- Works with the main toolbox

### 📝 Practical Example

**Morning Routine (Before Wear App):**
1. Wake up
2. Remember to take progress photo
3. Find phone
4. Unlock phone
5. Open OpenTransition app
6. Navigate to camera
7. Position phone for selfie
8. Try to hold steady
9. Take photo
10. Phone moves, angle changes

**Morning Routine (With Wear App):**
1. Wake up
2. Tap watch button
3. Phone camera opens automatically
4. Place phone in consistent spot
5. Take photo hands-free
6. Consistent angle every time ✨

## Key Features Explained

### 1. 📸 Photo Trigger (The Main Feature)

**What it does:**
Your watch sends a message to your phone saying "open the camera now!"

**Why it's useful:**
- **Consistency** - Same angle every time
- **Hands-free** - Don't need to hold phone
- **Quick** - Tap watch, done
- **No fumbling** - Phone can be already positioned

**Real-world use:**
Place your phone on a shelf at eye level, tap your watch, and take a perfectly positioned selfie without touching the phone.

### 2. 📊 Milestone Counter

**What it does:**
Shows how many milestones you've recorded.

**Why it's useful:**
- **Motivation** - Quick reminder of progress
- **Glanceable** - See it with a wrist flick
- **No phone needed** - Check anytime

**Real-world use:**
Feeling down? Glance at your watch: "147 days since I started. I've come so far!"

### 3. 📅 Latest Milestone

**What it does:**
Shows your most recent milestone title and date.

**Why it's useful:**
- **Memory** - Remember recent wins
- **Context** - Know where you are
- **Inspiration** - See your latest achievement

**Real-world use:**
"Latest: Started HRT - Nov 15, 2024" - instant smile!

### 4. 🔄 Manual Sync

**What it does:**
Forces watch to get fresh data from phone.

**Why it's useful:**
- **Update** - Get latest milestones
- **Verify** - Confirm connection works
- **Refresh** - Clear old cached data

**Real-world use:**
Just added a milestone on your phone? Tap sync on watch to see the updated count.

### 5. 🔗 Connection Status

**What it does:**
Shows if phone app is connected and reachable.

**Why it's useful:**
- **Awareness** - Know if features will work
- **Troubleshooting** - Diagnose issues quickly
- **Peace of mind** - See green = all good

**Real-world use:**
"Connected" = tap photo button will work. "Disconnected" = phone too far or app not open.

## What the Wear App is NOT

### ❌ It's NOT a Photo Viewer
- Screen too small for photos
- Privacy concern (watch easier to glance at)
- Photos stay safely on phone

### ❌ It's NOT an Editor
- Too complex for small screen
- Use phone for editing milestones
- Watch is view-only

### ❌ It's NOT Standalone
- Requires phone app to work
- Can't function independently
- True companion, not replacement

### ❌ It's NOT Always Online
- Only talks to YOUR phone
- No internet connection
- No cloud sync from watch

## How Data Flows

### Scenario 1: Trigger Photo

```
1. You tap "Take Photo" on watch
         ↓
2. Watch finds your phone
         ↓
3. Watch sends "trigger_photo" message
         ↓
4. Phone receives message
         ↓
5. Phone opens camera app
         ↓
6. You take the photo on phone
         ↓
7. Photo saved in phone app (as usual)
```

### Scenario 2: View Milestones

```
1. You create milestone on phone
         ↓
2. Phone packages milestone data
         ↓
3. Phone syncs to watch automatically
         ↓
4. Watch receives data
         ↓
5. Watch saves to local cache
         ↓
6. Watch updates display
         ↓
7. You see updated count on watch
```

## Privacy & What Data is Shared

### ✅ Shared with Watch (Safe)
- Milestone titles ("Started HRT")
- Milestone dates (Nov 15, 2024)
- Total count (147)
- Sync timestamps

### ❌ NOT Shared (Stays on Phone)
- **Photos** - Too large, too sensitive
- **Detailed notes** - Privacy
- **Personal info** - Not needed
- **Passwords** - Security

### 🔒 How It's Protected
- Encrypted during transfer
- Stays on YOUR devices only
- No cloud servers involved
- No third parties

## Technical Requirements

### What You Need

**Phone:**
- Android phone with OpenTransition mobile app installed
- Bluetooth enabled
- Within range of watch (usually 30 feet)

**Watch:**
- Wear OS 3.0 or newer
- Paired with phone (via Wear OS app)
- OpenTransition Wear app installed

**Both:**
- Bluetooth active
- Apps installed
- Devices paired

### What's Optional

- WiFi (can help but not required)
- Internet (not needed for watch-phone communication)
- Cloud sync (watch works offline)

## Common Questions

### Q: Will this drain my watch battery?
A: Minimal impact. The app only communicates when you use it. Not constantly syncing.

### Q: Do I need the wear app?
A: No! It's completely optional. The mobile app works perfectly without it.

### Q: Can I use it without my phone nearby?
A: Partially. You can view cached milestone data, but can't trigger photos or sync.

### Q: What if my phone is locked?
A: Camera trigger will still work (phone will unlock to camera).

### Q: Can others see my data on my watch?
A: Only basic info (count, latest milestone title). No photos or details.

### Q: Does it work with all Wear OS watches?
A: Yes, if your watch runs Wear OS 3.0 or newer.

## Getting Started

### Step 1: Install Mobile App
Install the OpenTransition mobile app on your phone (if you haven't already).

### Step 2: Install Wear App
Install the OpenTransition Wear app on your smartwatch.

### Step 3: Wait for Auto-Detection
The apps will automatically find each other. No manual pairing needed!

### Step 4: Check Connection
Open the Wear app. You should see "Connected" status.

### Step 5: Try Photo Trigger
Tap "Take Photo" button. Your phone camera should open!

### Step 6: Check Milestones
If you have milestones, you'll see the count and latest one.

## Troubleshooting

**"Disconnected" Status:**
- Make sure phone app is installed
- Check Bluetooth is on
- Verify watch is paired with phone
- Restart both apps

**Photo Trigger Not Working:**
- Check "Connected" status first
- Make sure phone is in range
- Try manual sync first
- Restart phone app

**Milestone Count is 0:**
- Tap "Sync Now" button
- Wait a few seconds
- Check mobile app has milestones
- Restart watch app

**Old Data Showing:**
- Tap "Sync Now"
- Close and reopen watch app
- Check phone app is updated
- Force close both apps

## Summary

The Wear OS companion app is your **quick-access remote control** for OpenTransition:

### What It Does Best:
- ⚡ **Quick photo triggers**
- 👁️ **Glanceable progress**
- 🎯 **Hands-free operation**
- 💪 **Daily motivation**

### What It Leaves to Phone:
- 📸 **Photo storage & viewing**
- ✏️ **Editing & management**
- 🔧 **Settings & configuration**
- ☁️ **Backup & cloud sync**

### Think of It As:
Your **wrist-based dashboard** - showing key stats and providing quick actions, while your phone remains the central hub for all your detailed transition tracking needs.

**Need more details?** See [WEAR_APP_FEATURES.md](WEAR_APP_FEATURES.md) for the complete feature guide.
