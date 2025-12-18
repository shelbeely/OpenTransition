# Frequently Asked Questions (FAQ)

## General Questions

### What is OpenTransition?

OpenTransition is a free, private app designed for transgender individuals to document their transition journey through photos and milestones. It helps you track changes over time, celebrate important moments, and keep everything organized in one secure place.

### Is OpenTransition really free?

Yes! OpenTransition is completely free to use. The app includes optional ads that help support development, but all features are available for free with no subscription required.

### Is OpenTransition open source?

Yes! OpenTransition is open source software licensed under GPL v3. This means:
- The source code is publicly available for review
- The community can contribute improvements
- You can verify there's no hidden tracking or data collection
- It's transparent about what it does with your data

Visit our GitHub: [https://github.com/shelbeely/OpenTransition](https://github.com/shelbeely/OpenTransition)

## Privacy & Security

### Where is my data stored?

By default, all your photos and data are stored **locally on your device**. Nothing is uploaded to the internet unless you choose to enable cloud backup.

### Do I need to create an account?

No! You can use OpenTransition completely without creating any account. An account is only needed if you want to use the optional cloud backup feature.

### Can you see my photos?

**Absolutely not.** Your photos stay on your device. We have no access to your photos, data, or personal information. Even if you use cloud backup, your data is stored in your own Google Firebase account, not ours.

### Is my data tracked or sold?

No. OpenTransition does not:
- Track your usage (Analytics is opt-in only)
- Sell your data
- Share your information with third parties
- Collect personal information beyond what's needed for optional features

### How secure is the app lock?

The app lock offers multiple security options:

- **Password Lock**: Uses industry-standard hashing to securely store your password. Your actual password is never stored—only a hashed version is saved, making it very difficult for anyone to access your data without your code.

- **Biometric Lock** (NEW): Uses your device's fingerprint or face recognition for quick and secure access. This option:
  - Uses Android's BiometricPrompt API with BIOMETRIC_STRONG security
  - Still requires a backup password for recovery
  - Only works if your device supports biometric authentication
  - Provides the fastest unlock experience while maintaining security

Choose the lock type that works best for your device and privacy needs. Both options keep your transition journey private and secure.

### What is "Train Mode"?

Train Mode is a disguise feature that changes the app icon to look like a train/railway app. This provides an extra layer of privacy by making the app less obviously related to transgender transition tracking. Perfect for situations where you want to keep your transition private.

## Using the App

### What's the difference between Face, Body, and Custom photo types?

- **Face**: For tracking facial changes (great for seeing HRT effects on your face)
- **Body**: For full-body progress photos
- **Custom**: For any specific area you want to track separately (hands, legs, hair, etc.)

You can use all three types to organize your photos in whatever way makes sense for your journey.

### Can I import photos I've already taken?

Yes! You can import existing photos from your device:
1. Tap the ➕ button
2. Select "Import Photos"
3. Choose the photos you want to add
4. Select the date they were taken
5. Choose the photo type

### How do I compare photos side-by-side?

1. Open the Gallery
2. Tap any photo to view it full screen
3. Swipe left or right to see other photos
4. Photos are automatically sorted by date for easy comparison

### Can I delete photos?

Yes! To delete a photo:
1. Open the photo in full screen
2. Tap the trash/delete icon
3. Confirm deletion

**Note:** Deleted photos are permanently removed and cannot be recovered unless you have a backup.

### How many photos can I store?

There's no hard limit on the number of photos. The only limit is your device's available storage space. We recommend keeping high-quality photos and backing up regularly.

## Backup & Data

### How do I backup my data?

**Local Backup (Recommended):**
1. Go to Settings → Backup & Sync
2. Tap "Export Data"
3. Save the `.ttbackup` file somewhere safe (Google Drive, computer, external drive)

**Cloud Backup (Optional):**
1. Go to Settings → Backup & Sync
2. Sign in with Google
3. Enable Cloud Sync
4. Your data automatically backs up to your Firebase account

### What's included in a backup?

A backup includes:
- All your photos
- All milestones and their details
- App settings (theme, preferences, etc.)
- Everything needed to restore your data completely

### How do I restore from a backup?

1. Locate your `.ttbackup` file
2. Tap it to open with OpenTransition
3. Confirm you want to import the data
4. Wait for the import to complete

The app will import all photos, milestones, and settings from the backup.

### Can I transfer my data to a new phone?

Yes! Here's how:

**On your old phone:**
1. Create a backup (Export Data)
2. Save the `.ttbackup` file to cloud storage or email it to yourself

**On your new phone:**
1. Install OpenTransition
2. Download the backup file
3. Open the `.ttbackup` file with OpenTransition
4. Import completes!

### What happens if I lose my phone?

If you have a backup (local or cloud), you can restore everything on a new device. If you don't have a backup, unfortunately your data cannot be recovered. **This is why regular backups are so important!**

## Cloud Sync & Firebase

### What is Firebase?

Firebase is Google's cloud platform that powers the optional cloud backup feature. When you enable cloud sync, your data is stored in a Firebase database linked to your Google account.

### Do I need to use cloud sync?

No! Cloud sync is completely optional. The app works perfectly fine without it, storing everything locally on your device.

### Is cloud sync secure?

Yes. Firebase uses industry-standard encryption and security. Your data is:
- Encrypted in transit
- Stored securely in Google's cloud infrastructure
- Only accessible through your Google account
- Protected by Google's security measures

### Does cloud sync cost money?

No. The free tier of Firebase is more than enough for personal use of OpenTransition. You won't incur any charges.

### How do I disable cloud sync?

1. Go to Settings → Backup & Sync
2. Tap "Disable Cloud Sync"
3. Optionally delete cloud data
4. Sign out of Google account

Your local data remains untouched.

## Technical Issues

### The app keeps crashing. What should I do?

Try these steps:
1. Force close the app and restart it
2. Clear the app cache (Settings → Apps → OpenTransition → Clear Cache)
3. Ensure you have the latest version from Play Store
4. If problem persists, report it on GitHub with details about when it crashes

### Photos aren't loading properly

This might be due to:
- Corrupted image files
- Low storage space on device
- Permission issues

Try:
1. Check your device has adequate storage
2. Verify photo permissions are granted
3. Restart the app
4. If problem continues, try reimporting the photos

### I forgot my app lock PIN/pattern

Unfortunately, if you forget your PIN or pattern, there's no way to recover it (this is by design for security). Your options:
1. Uninstall and reinstall the app (you'll lose data unless you have a backup)
2. If you have a backup from before the lock was set, restore from it

**Tip:** Write down your PIN/pattern and store it somewhere safe!

### Cloud sync isn't working

Check these:
- You're signed into a Google account
- You have an internet connection
- Cloud sync is enabled in settings
- Try signing out and back in
- Check Firebase console for any issues

### Import failed with errors

If import fails:
- Verify the `.ttbackup` file isn't corrupted
- Ensure you have enough storage space
- Check all required permissions are granted
- Try importing on a different device

## Features & Customization

### Can I change the theme?

Yes! Go to Settings → Appearance and choose from:
- Pink (default)
- Blue
- Purple
- Green

### Can I add my own milestone types?

Currently, milestones are free-form. You can type any title and description you want. Future versions may add predefined milestone types.

### Does the app remind me to take photos?

Yes! Enable photo reminders in Settings → Notifications. You can choose how often you want to be reminded (weekly, bi-weekly, monthly).

### Can I share photos with my doctor?

Yes. You can:
1. Export individual photos from the gallery
2. Share them via any app (email, messaging, etc.)
3. Show your phone screen directly during appointments

The app doesn't have built-in sharing to protect your privacy, but you can manually share what you choose.

### Can I add notes to photos?

Currently, photos don't have individual note fields. However, you can:
- Add photos to specific dates
- Link photos to milestones (which have description fields)
- Use milestone descriptions as notes for specific time periods

## Updates & Future Features

### How do I update the app?

OpenTransition updates through the Google Play Store:
1. Open Play Store
2. Go to "My apps & games"
3. Find OpenTransition
4. Tap "Update"

Or enable auto-updates for the app in Play Store settings.

### What's planned for future versions?

Some features being considered:
- More customization options
- Additional photo comparison tools
- Timeline visualizations
- More theme options
- Widget support
- More export formats

Check the [GitHub Issues](https://github.com/shelbeely/OpenTransition/issues) for feature requests and roadmap.

### Can I suggest a feature?

Absolutely! We welcome suggestions:
1. Visit GitHub Issues
2. Search to see if it's already suggested
3. If not, create a new feature request
4. Describe what you'd like and why it would be helpful

## Community & Support

### How can I contribute?

There are many ways to help:
- Report bugs you encounter
- Suggest features you'd like
- Help improve documentation
- Contribute code (if you're a developer)
- Share the app with others who might find it useful
- Leave a review on the Play Store

### Where can I get help?

- **Documentation**: [https://shelbeely.github.io/OpenTransition](https://shelbeely.github.io/OpenTransition)
- **GitHub Issues**: [https://github.com/shelbeely/OpenTransition/issues](https://github.com/shelbeely/OpenTransition/issues)
- **User Guides**: Check other pages in the User Guide section

### Is there a community forum or Discord?

Currently, the main community space is GitHub. A Discord or forum may be created in the future based on community interest.

## Safety & Support Resources

### I'm struggling with my transition. Can the app help?

OpenTransition is a tool for tracking your journey, but it's not a substitute for professional support. If you're struggling, please reach out to:

- **Trans Lifeline**: 1-877-565-8860 (US), 1-877-330-6366 (Canada)
- **Trevor Project**: 1-866-488-7386 or text START to 678678
- **PFLAG**: [pflag.org](https://pflag.org) for family support
- Your healthcare provider or therapist

### The app is making me focus too much on appearance

If tracking photos becomes unhealthy or obsessive, it's okay to:
- Take a break from the app
- Reduce photo frequency
- Focus on milestone achievements instead of physical changes
- Talk to a therapist about your feelings

Your mental health is more important than any tracking app.

---

## Still Have Questions?

If your question isn't answered here:
1. Check the other [User Guide](getting-started.md) pages
2. Visit the [full documentation](https://shelbeely.github.io/OpenTransition)
3. Search [GitHub Issues](https://github.com/shelbeely/OpenTransition/issues)
4. Create a new issue on GitHub with your question

We're here to help make your transition tracking as easy and private as possible! 🌈
