# Package Name Update Guide

## Changes Made

The package name has been updated from `com.drspaceboo.transtracks` to `com.shelbeely.transtracks` to allow independent release on the Google Play Store.

### Files Modified

1. **app/build.gradle**
   - Updated `applicationId` to `com.shelbeely.transtracks`
   - Updated `namespace` to `com.shelbeely.transtracks`

2. **app/src/main/AndroidManifest.xml**
   - Updated FileProvider authority to `com.shelbeely.transtracks.data.TransTracksFileProvider`

3. **All Source Files (85+ files)**
   - Updated package declarations from `com.drspaceboo.transtracks.*` to `com.shelbeely.transtracks.*`
   - Updated all import statements to reference new package name
   - Updated ComponentName references in MainActivity.kt

4. **Navigation and Layout XML Files**
   - Updated all fragment and view class references in navigation graph
   - Updated custom view references in layout files

5. **Test Files**
   - Updated package declarations in test files
   - Updated expected package name assertion in ExampleInstrumentedTest.kt

## GPL v3 Compliance Requirements

Since this application is licensed under GNU General Public License v3, here are the requirements you must meet when releasing on the Play Store:

### 1. **Provide Access to Source Code**
   - You must make the complete source code available to users
   - This can be done by:
     - Providing a link to the GitHub repository in the app description
     - Including a menu option in the app that links to the source code
     - Offering to provide source code upon request

### 2. **Include License Information**
   - The GPL v3 license text is already included in the LICENSE file
   - License headers are present in all TransTracks source files
   - You should display license information in the app's About or Settings screen

### 3. **Attribute Original Authors**
   - Keep the copyright notices that credit TransTracks (2018-2023)
   - If you make modifications, add your own copyright notice but don't remove existing ones
   - Example: "Copyright (C) 2024 Your Name" in addition to existing copyright

### 4. **State Changes**
   - GPL v3 requires that modified versions be marked as changed
   - Consider adding a version note or changelog that mentions this is a modified version
   - Update the app name or add a subtitle if you're making significant changes

### 5. **No Additional Restrictions**
   - You cannot add any terms that restrict the freedoms granted by GPL v3
   - Play Store DRM and license verification are acceptable as they don't restrict user freedoms
   - Users must still be able to modify and redistribute the code

### 6. **Installation Information**
   - For GPL v3 compliance, you should provide information needed for users to install modified versions
   - Include build instructions in README.md
   - Provide keystore generation instructions if signing is required

## Next Steps for Play Store Release

1. **Create a New Signing Key**
   ```bash
   keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias transtracks
   ```

2. **Update Firebase Configuration**
   - Create a new Firebase project for your app
   - Download the `google-services.json` file
   - Update the package name in Firebase console to `com.shelbeely.transtracks`
   - Place the new `google-services.json` in the `app/` directory

3. **Update Admob Configuration (if using ads)**
   - Create new Ad units for the new package name
   - Update the ad IDs in `secrets.properties`

4. **Update App Metadata**
   - Change the app name if desired (in `app/src/main/res/values/strings.xml`)
   - Update launcher icons if you want to distinguish your version
   - Update the applicationId version numbers as appropriate

5. **Build and Test**
   - Test all app functionality thoroughly
   - Verify file provider works correctly with the new package name
   - Test backup and restore functionality
   - Verify Firebase integration works

6. **Play Store Listing**
   - In the app description, include a link to the source code repository
   - Mention that the app is free software under GPL v3
   - Consider adding: "Source code available at: https://github.com/shelbeely/TransTracks-Android"

7. **App Settings Screen**
   - Consider adding a "About" or "Open Source" section that:
     - Links to the GitHub repository
     - Shows the GPL v3 license
     - Credits the original TransTracks developers

## Important Notes

- The package name change means users cannot directly update from the original TransTracks app
- Users will need to export their data from the original app and import into your version
- You should clearly communicate this in your app description
- Consider providing migration instructions

## GPL v3 Key Points

✅ **You CAN:**
- Release the app on Play Store (even commercially)
- Charge for the app
- Make modifications
- Use different branding

❌ **You MUST:**
- Provide source code to users
- Keep GPL v3 license
- Credit original authors
- Allow users to modify and redistribute
- Document your changes

## Additional Resources

- [GPL v3 Full Text](https://www.gnu.org/licenses/gpl-3.0.en.html)
- [GPL FAQ](https://www.gnu.org/licenses/gpl-faq.html)
- [Android App Bundle Documentation](https://developer.android.com/guide/app-bundle)
- [Play Store Publishing Guide](https://developer.android.com/distribute)
