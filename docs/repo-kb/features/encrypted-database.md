# Feature: Encrypted Database

> **Pointer page.** The canonical user/maintainer documentation lives in
> [`ENCRYPTED_DATABASE.md`](../../../ENCRYPTED_DATABASE.md). This page only
> records what the code actually does today.

## Current state

| Question | Answer | Evidence |
|---|---|---|
| Is the SQLCipher mechanism wired up? | Yes — `Room.databaseBuilder.openHelperFactory(SupportFactory(passphrase))` when `isEncrypted = true` | [`AppDatabase.kt:129-134`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/AppDatabase.kt) |
| Is it on for real users? | **No.** `SettingsManager.isEncryptedDatabaseEnabled()` is hard-coded to `false` | [`SettingsManager.kt:408`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) |
| Is the UI toggle visible? | **No.** `isEncryptedDatabaseFeatureAvailable()` returns `false` | [`SettingsManager.kt:415`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) |
| Why is it off? | Code comment: "the migration from Realm to Room is incomplete: the SQLCipher-encrypted Room database holds essentially nothing while user-visible data still lives in the unencrypted Realm" | [`SettingsManager.kt:397-407`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) |
| How is the passphrase stored? | EncryptedSharedPreferences under `opentransition_db_keys`, MasterKey `AES256_GCM` | [`KeystoreManager.kt:23-122`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/KeystoreManager.kt) |
| Keys per vault? | Yes — `real_db_passphrase`, `decoy_db_passphrase` (32-byte hex) | [`KeystoreManager.kt:25-61`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/KeystoreManager.kt) |

## Persistence touched

| Surface | Details |
|---|---|
| Pref `encryptedDatabaseEnabled` | Still written to disk for forward-compat; reading it is overridden by the hard-coded `false`. Mirrored to Firestore. |
| `EncryptedSharedPreferences` `opentransition_db_keys` | Holds the (still unused) random 32-byte passphrases. |

## Known issues (audit cross-reference)

- 🔴 [`audit-report/07-issues-and-bugs.md`](../../../audit-report/07-issues-and-bugs.md) ISSUE-004 / ISSUE-013 — "encrypted database" claim was misleading; addressed by making the toggle a no-op until migration completes.
- 🔴 [`audit-report/10-security-review.md`](../../../audit-report/10-security-review.md) §"Encryption at rest" rating: **3 / 10** — the mechanism is sound, the surface is hidden, but readers of the Play Store listing may infer protection that doesn't currently apply.

## Invariants

- Until the Realm→Room migration completes, the toggle must remain off.
- When it is turned on, **both** real and decoy databases must be encrypted (one cannot be plaintext while the other is encrypted, or the user can't tell which is which from the keystore).

## Tests

None.
