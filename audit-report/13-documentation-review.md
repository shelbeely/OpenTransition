# 13 — Documentation Review

## Inventory

| Document | LOC | Audience | Quality |
|----------|----:|----------|---------|
| `README.md` | 193 | end users + new contributors | 7 — concise, accurate on the big picture, links to the docs site |
| `ARCHITECTURE.md` | 305 | new engineers | 5 — describes intended architecture; some claims diverge from code (see ISSUE-024) |
| `MONOREPO.md` | 138 | new engineers | 7 — accurate description of `:mobile`/`:wear`/`:shared` roles |
| `WEAR_APP_FEATURES.md` | 265 | end users + product | 4 — describes capabilities that aren't fully wired (capability discovery, sync) |
| `ENCRYPTED_DATABASE.md` | 262 | end users + privacy auditors | 3 — describes a feature that doesn't actually protect the active dataset (ISSUE-004) |
| `LOGO_REPLACEMENT_GUIDE.md` | 211 | maintainers | 8 — clear, step-by-step |
| `docs/` (mkdocs source) | not counted | end users (shelbeely.github.io) | 6 — mostly screenshots + setup pages |
| `.github/copilot-instructions.md` | (custom-instruction file) | the AI agent | 9 — well-structured |
| Per-module READMEs | ❌ none | | n/a |
| Architecture Decision Records (`docs/adr/`) | ❌ none | | n/a |
| `CONTRIBUTING.md` | ❌ | | n/a |
| `CODE_OF_CONDUCT.md` | ❌ | | n/a |
| `SECURITY.md` | ❌ | | n/a |
| `LICENSE` | ✅ GPL v3 (`LICENSE`) | | ✅ |

## 1. Root README quality

**Strengths:**
- Clear positioning: forked from TransTracks, GPL-licensed, privacy-first.
- Build instructions in `## Building from Source` are accurate against the actual `mobile/build.gradle`.
- Contains the canonical screenshot.

**Issues:**
- 🟡 Doesn't mention the wear app at all in the build section. A contributor who only follows the README
  will never know to run `:wear:assembleDebug`.
- 🟡 "Translations/localization" is listed under "Areas where help is needed" without a link to a
  Crowdin/Weblate/etc. — there's nowhere for a translator to start.

## 2. `ARCHITECTURE.md` accuracy check

A spot-check against the code:

| Claim in `ARCHITECTURE.md` | Code reality |
|----------------------------|--------------|
| "Wearable Data Layer — Capabilities Registered: `opentransition_mobile_app`" (`ARCHITECTURE.md:115-121`) | 🔴 Not registered anywhere — see ISSUE-001 / ISSUE-024. |
| "Mobile triggers Wear sync via `WearableHelper.syncMilestones`" | 🔴 Helper exists, never called from production code. |
| "Encrypted database via SQLCipher" | 🟠 Mechanism exists; UI bypasses it (ISSUE-004). |
| "Realm 2.3.0 for primary persistence" | ✅ Accurate. |
| "Single-Activity navigation with Jetpack Navigation" | ✅ Accurate. |
| "Compose for new UI" | 🟡 Half-true: Compose surfaces exist, but Activity is XML; navigation is XML. |
| "RxJava 3 for reactive flows" | ✅ Accurate. |

🟠 **Net**: the document describes the *intended* architecture more than the *implemented* one. New
contributors will be misled.

## 3. `WEAR_APP_FEATURES.md` accuracy check

| Claim | Code reality |
|-------|--------------|
| "Take Photo — instantly trigger photo capture on phone" | 🟡 Code path exists, blocked by ISSUE-001. |
| "View Milestone Stats" | 🔴 Always stale because the producer is a stub (ISSUE-003). |
| "Camera Remote Control — zoom, flash, switch" | 🔴 Wear sends commands, mobile has no listener for those paths. |
| "Voice Recording" | 🟠 Records OK; ships over a channel size-limited to ~100 KB (ISSUE-010). |
| "Sync Now button" | 🔴 Triggers a `Log.d` line on the mobile side. |

🔴 **All five top-level Wear features are broken or partial in the current code.**

## 4. `ENCRYPTED_DATABASE.md` accuracy check

The document describes:
- A toggle in Settings ✅ exists.
- Hardware-backed passphrase via Android Keystore ✅ correct (KeystoreManager).
- SQLCipher encryption at rest ✅ Room is wired with SQLCipher.

What it does **not** mention, but should:
- 🔴 The encrypted Room database stores essentially no live data. Photos, milestones, audio analyses,
  and audio files all live in unencrypted Realm + plaintext files.
- 🔴 Enabling the toggle does not retroactively migrate existing Realm data.
- 🔴 Therefore a user reading this document will overestimate the protection they have.

## 5. Onboarding friction

Imagined flow for a new contributor:
1. ✅ Clone repo, read README.
2. ✅ `cp secrets.properties.example secrets.properties` — clear from `.gitignore`.
3. ⚠️ Need `mobile/google-services.json` — README says "obtain from Firebase Console" but doesn't say
   how to make a debug-only Firebase project. Most contributors will skip this and find the build
   fails.
4. ✅ `./gradlew :mobile:assembleDebug` — works out of the box once the JSON is in place (verified in
   this audit's runtime run).
5. 🔴 No instructions for testing the Wear app:
   - No mention that you need a Wear OS AVD.
   - No mention of pairing/dev-mode.
   - No mention that capabilities are missing (so the contributor will see "Phone Disconnected" and
     think their setup is wrong, when actually the app is bugged).

🟡 **Recommendation**: add a `docs/CONTRIBUTING.md` with a 5-step onboarding (mobile + wear) and a
"known issues you'll hit" section.

## 6. Pairing / setup docs for mobile↔wear testing

❌ **None exist.** The closest is `WEAR_APP_FEATURES.md`, which describes the user-facing pairing flow
but not the developer setup. Topics that should be covered:

- Creating a Wear OS AVD with Play Services.
- Pairing the AVDs (or pairing a wear AVD to a physical phone).
- Verifying capability registration via `adb shell dumpsys activity service WearableService`.
- Testing the Data Layer with `adb shell am broadcast` or the Wearable simulator.

## 7. KDoc coverage on shared module APIs

`shared/src/main/java/com/shelbeely/opentransition/shared/`:

| File | Public API surface | KDoc coverage |
|------|--------------------|---------------|
| `WearableConstants.kt` | 21 `const val`s | 🟡 grouped by `// section` comments; no per-constant docs. |
| `models/MilestoneData.kt` | 1 data class, 5 fields | ❌ no field-level docs. `type` is a `String` with no allowed-values docs. |
| `util/WearableHelper.kt` | 4 functions | ✅ full KDoc on each. |

🟡 **Gap**: `MilestoneData.type` is the most ambiguous field in the cross-device contract; it should
be either an enum or have its allowed values documented.

## 8. Outdated / contradictory docs

| Conflict | Files |
|----------|-------|
| Path of `google-services.json` | README says `mobile/`; `.gitignore` lists both `mobile/` and `app/` (legacy from TransTracks). |
| Capability registration | `ARCHITECTURE.md` claims registered; code disagrees. |
| Wear feature list | `WEAR_APP_FEATURES.md` lists features that aren't wired. |
| Encryption coverage | `ENCRYPTED_DATABASE.md` overstates protection. |
| TransTracks vs OpenTransition | README/MONOREPO use the new name; `ci.yml:86` still uses `com.drspaceboo.transtracks`; `dependabot.yml` reviewers are `TransTracks`. |

## 9. Missing critical docs

| Missing | Severity |
|---------|---------|
| `CONTRIBUTING.md` | 🟠 |
| `SECURITY.md` (responsible disclosure for a privacy app!) | 🟠 |
| `CODE_OF_CONDUCT.md` | 🟡 |
| Release runbook (manual upload, staged rollout, rollback) | 🟡 |
| ADR for the Realm→Room migration decision | 🟡 |
| ADR for the wear OS architecture choice (Views vs Compose) | 🟡 |
| Privacy policy | 🔴 — a transition-tracking app *must* have one. Likely exists on the website but not in repo. |

## 10. Documentation rating

| Sub-criterion | Score |
|---------------|------:|
| Root README accuracy | 7 |
| Architecture accuracy | 4 |
| Wear feature docs accuracy | 3 |
| Security/privacy claims accuracy | 3 |
| Onboarding completeness | 5 |
| Cross-device dev docs | 1 |
| API KDoc | 6 |
| Critical-doc presence | 3 |
| **Overall documentation** | **4 / 10** |

Documentation is voluminous and well-formatted but **systematically overstates feature completeness
and security**. Bringing claims in line with reality (or finishing the features described) is the
single biggest documentation improvement.
