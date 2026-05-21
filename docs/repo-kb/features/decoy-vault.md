# Feature: Decoy Vault

## Summary

Optional second unlock code that, when entered at the lock screen, opens an
**empty alternate Room database** (`opentransition_decoy.db`) instead of the
real one. Used to give a plausible "this is my whole app" view under duress.

## Entry points

| Surface | Component | Path |
|---|---|---|
| Setup UI | `SettingsFragment` (decoy-vault toggle, decoy code field) | [`ui/settings/SettingsFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt) |
| Code check at unlock | `DatabaseManager.isDecoyPasscode` | [`database/DatabaseManager.kt:57-71`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/DatabaseManager.kt) |
| Vault swap | `DatabaseManager.switchToDecoyVault` / `switchToRealVault` | [`database/DatabaseManager.kt:34-45`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/DatabaseManager.kt) |

## How the swap works

`DatabaseManager` keeps a single `currentVaultIsDecoy` boolean. `getDatabase(context)` returns `AppDatabase.getInstance(context, currentVaultIsDecoy)`, which routes to one of two cached singletons backed by `opentransition.db` vs `opentransition_decoy.db` ([`AppDatabase.kt:47-113`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/AppDatabase.kt)).

If encryption is enabled (today: never — see [`encrypted-database.md`](./encrypted-database.md)), each vault has its **own** passphrase under `KeystoreManager` keys `real_db_passphrase` and `decoy_db_passphrase` ([`KeystoreManager.kt:25-26`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/KeystoreManager.kt)).

## Persistence touched

| Pref | Purpose |
|---|---|
| `decoyVaultEnabled` (`Bool`, default `false`) | Master toggle ([`SettingsManager.kt:423`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt)) |
| `decoyLockCode` (hashed `String`, default empty) | Stored under default prefs ([`SettingsManager.kt:434`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt)) |

Databases:
- `opentransition.db` (real)
- `opentransition_decoy.db` (decoy)

Both live in the standard Room location under `databases/` in the app data dir.

## External APIs

- **Firebase Firestore** mirrors `decoyVaultEnabled` when `saveToFirebase = true` ([`SettingsManager.kt:385`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt)).
- `decoyLockCode` is **excluded** from the Firestore mirror ([`SettingsManager.kt:390`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt)).

## Known issues (audit cross-reference)

- 🟡 [`audit-report/07-issues-and-bugs.md`](../../../audit-report/07-issues-and-bugs.md) — most UI adapters still read directly from **Realm**, so the Room-level vault swap doesn't actually hide existing photo data. The decoy view will still see Realm-backed photos until the Realm→Room migration completes.
- 🟡 `SettingsFragment.kt:776` TODO: decoy-vault export not yet implemented.

## Invariants

- The decoy database must always be a **separate file** so accidental damage to the real DB cannot leak into it (and vice versa).
- A new feature that calls `AppDatabase` directly (instead of `DatabaseManager.getDatabase`) **breaks** the swap silently — any rewrite must enforce going through the manager.

## Tests

None.
