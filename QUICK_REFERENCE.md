# Package Name Update - Quick Reference

## What Changed?

**Old Package:** `com.drspaceboo.transtracks`  
**New Package:** `com.shelbeely.transtracks`

This change enables independent distribution on Google Play Store while maintaining GPL v3 compliance.

## Files Updated

- ✅ **app/build.gradle** - applicationId and namespace
- ✅ **app/src/main/AndroidManifest.xml** - FileProvider authority
- ✅ **85+ Kotlin/Java source files** - package declarations and imports
- ✅ **Navigation XML files** - fragment class references
- ✅ **Layout XML files** - custom view class references
- ✅ **Test files** - package declarations and assertions
- ✅ **ProGuard rules** - package keep rules

## Quick Start Guide

### 1. Development Setup
```bash
# Copy configuration files
cp secrets.properties.example secrets.properties

# Add your google-services.json to app/ directory
# Get this from Firebase Console after creating a project
```

### 2. Build the App
```bash
./gradlew clean assembleDebug
```

### 3. Generate Release Key
```bash
keytool -genkey -v -keystore release-keystore.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias transtracks
```

### 4. Play Store Checklist

**Before Publishing:**
- [ ] Update app/google-services.json with your Firebase config
- [ ] Generate and configure release signing key
- [ ] Update secrets.properties with your ad IDs (if using ads)
- [ ] Test all features thoroughly
- [ ] Add source code link to Play Store description
- [ ] Include GPL v3 license notice in description
- [ ] Consider adding About screen with license and source link

**Recommended Play Store Description Addition:**
```
This app is free and open source software licensed under GPL v3.
Source code: https://github.com/shelbeely/TransTracks-Android
```

## GPL v3 Compliance

### Must Do:
✅ Provide source code access to users  
✅ Keep GPL v3 license and copyright notices  
✅ Allow users to modify and redistribute  
✅ Document changes made to the code  

### Cannot Do:
❌ Add restrictions beyond GPL v3  
❌ Remove copyright notices  
❌ Relicense under different terms  
❌ Prevent users from accessing source code  

## Important Notes

1. **Users Cannot Upgrade** - Package name change means users of the original TransTracks app cannot directly update to this version. They must export/import their data.

2. **Firebase Configuration** - You need your own Firebase project with package name `com.shelbeely.transtracks`.

3. **AdMob (if using)** - Create new ad units for the new package name.

4. **Backup Compatibility** - Backup files (.ttbackup) should remain compatible between versions.

5. **Signing Key** - Generate a NEW signing key - don't use the original TransTracks key.

## Documentation

- **PACKAGE_NAME_UPDATE.md** - Comprehensive guide with all details
- **GPL_COMPLIANCE.md** - Legal compliance verification
- **README.md** - Development setup instructions

## Support

- Original TransTracks: https://github.com/TransTracks/TransTracks-Android
- This Fork: https://github.com/shelbeely/TransTracks-Android
- GPL v3 License: https://www.gnu.org/licenses/gpl-3.0.en.html

## Version Info

- **Original Copyright:** 2018-2023 TransTracks
- **Fork Package:** com.shelbeely.transtracks
- **License:** GNU General Public License v3
- **Last Updated:** 2025-12-07

---

For detailed information, see **PACKAGE_NAME_UPDATE.md** and **GPL_COMPLIANCE.md**.
