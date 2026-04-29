# Open Questions

Questions that surfaced during the pass-1 KB build but could not be answered from the source alone. Mirror of [`_state/unknowns.md`](../_state/unknowns.md), formatted as durable Q&A pages.

## Question: SDK targets disagree between docs and source

**Status:** Unknown from current repo scan.

The phone-app instructions in [`.github/copilot-instructions.md`](../../../.github/copilot-instructions.md) describe `:mobile` as `minSdk 21 / targetSdk 36`. The actual source in [`mobile/build.gradle`](../../../mobile/build.gradle) sets `minSdkVersion 26` and `targetSdkVersion 35`. The KB ([`architecture.md`](../architecture.md)) takes the source values as authoritative.

**Evidence checked:**

- [`mobile/build.gradle`](../../../mobile/build.gradle) `defaultConfig` block:
  ```groovy
  defaultConfig {
      applicationId "com.shelbeely.opentransition"
      minSdkVersion 26
      targetSdkVersion 35
      …
  }
  ```
- [`.github/copilot-instructions.md`](../../../.github/copilot-instructions.md) "Repository layout" table lists `:mobile` as `(minSdk 21, targetSdk 36)`, and "What NOT to do" says: "Do not change `minSdkVersion` below 21 or `targetSdkVersion` above 36 without discussion."

**Next step:**
- Decide whether the prose docs should be aligned to the code, or whether the code's `targetSdk 35` is itself a regression vs. the policy of `targetSdkVersion ≤ 36`.
- Update [`architecture.md`](../architecture.md) and [`.github/copilot-instructions.md`](../../../.github/copilot-instructions.md) once decided.
