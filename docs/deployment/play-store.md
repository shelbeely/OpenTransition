# Google Play Store

Use this checklist when preparing the Play Console submission.

## 1) Privacy Policy

- Public URL: `https://shelbeely.github.io/OpenTransition/privacy-policy/`
- Include the link in the “Privacy Policy” field on the store listing.

## 2) App Access

- **No login required** for core features (photos, milestones, gallery).
- **Optional Google sign-in** is only used for cloud backup/sync.
- If Play review needs credentials, provide a temporary Google test account or note that all functionality is available without login.

## 3) Ads

- **Yes – ads are shown (AdMob)** in release builds.
- No personalized tracking beyond AdMob’s defaults; no data is sold.

## 4) Content Rating

- Questionnaire answers: no violence, no sexual content, no profanity, no UGC sharing, no gambling, no drug/alcohol promotion.
- Expected result: **Teen/Everyone-level** rating.

## 5) Target Audience

- Intended for **adults (18+)**; not directed to children and not a kids app.

## 6) Data Safety Declaration (summary)

- **Collected (when enabled):**
  - Personal info: Google account ID/email (only when you turn on cloud backup).
  - User-generated content: photos, milestones, notes (stored locally; optionally synced to Firebase).
  - App activity/usage data: via Firebase Analytics (toggle in Settings).
  - App diagnostics: crash logs via Firebase Crashlytics (toggle in Settings).
  - Device identifiers/coarse data for ads (AdMob).
- **Sharing:** Data is only shared with Google Firebase/AdMob as processors; no data selling or other third-party sharing.
- **Security:** Data in transit is encrypted (HTTPS). App lock and disguised icon available.
- **User choice:** Analytics and crash reporting can be disabled; cloud backup is optional; users can delete data in-app or by uninstalling. Cloud data removal on request.

## 7) Government Apps

- **No** – the app is not operated by or on behalf of a government entity.

## 8) Financial Features

- **No** payments, banking, lending, crypto, or investment features.

## 9) Health Disclosure

- The app is for **informational/self-tracking only**.  
- It does **not** provide medical advice, diagnosis, or treatment.  
- Include this statement in the store listing if prompted.

## 10) Category & Contact

- Suggested categories: **Health & Fitness** or **Lifestyle**. Use **Medical** only if the Play Console offers it and the questionnaire flags the app as health-related.
- Contact email: **privacy@shelbeely.com**
- Website/Policy: `https://shelbeely.github.io/OpenTransition/privacy-policy/`
