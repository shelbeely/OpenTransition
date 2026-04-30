# Open Questions

Questions that surfaced during the pass-1 KB build but could not be answered from the source alone. Mirror of [`_state/unknowns.md`](../_state/unknowns.md), formatted as durable Q&A pages.

## Question: SDK targets disagree between docs and source

**Status:** Resolved 2026-04-30 — source is truth; prose docs updated to match.

The phone-app instructions in [`.github/copilot-instructions.md`](../../../.github/copilot-instructions.md) previously described `:mobile` as `minSdk 21 / targetSdk 36`. The actual source in [`mobile/build.gradle`](../../../mobile/build.gradle) sets `minSdkVersion 26`, `targetSdkVersion 35`, `compileSdk 36`. Per the repo-kb rule that source is final authority, the prose was updated; no source change.

**Evidence checked:**

- [`mobile/build.gradle`](../../../mobile/build.gradle) `defaultConfig` block:
  ```groovy
  android {
      compileSdk 36
      defaultConfig {
          applicationId "com.shelbeely.opentransition"
          minSdkVersion 26
          targetSdkVersion 35
          …
      }
  }
  ```
- [`.github/copilot-instructions.md`](../../../.github/copilot-instructions.md) "Repository layout" table now lists `:mobile` as `(minSdk 26, targetSdk 35, compileSdk 36)`, and "What NOT to do" reads: "Do not change `minSdkVersion` below 26 or `targetSdkVersion` above 36 without discussion."

**Resolution:**

- Updated `.github/copilot-instructions.md` "Repository layout" row and the SDK-floor "What NOT to do" line to match the source.
- KB [`architecture.md`](../architecture.md) already recorded the source-derived values; no change needed there.
- [`_state/unknowns.md`](../_state/unknowns.md) marked resolved.
