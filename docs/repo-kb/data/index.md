# Data

Per-entity / per-store evidence-based pages.

| Page | Scope |
|---|---|
| [`room-entities.md`](./room-entities.md) | Every Room `@Entity`, every column, every index/FK, every migration, with `mobile/schemas/` cross-reference |
| [`realm-models.md`](./realm-models.md) | The three Realm classes kept for legacy read paths + `.ttbackup` import; including the read/write call-sites that block Realm removal |
| [`shared-prefs-keys.md`](./shared-prefs-keys.md) | Every preference key actually written, the feature that owns it, and whether it round-trips to Firestore |
| [`file-layout.md`](./file-layout.md) | Directory structure used for photos, audio, and databases under app-private storage |
| [`ttbackup-format.md`](./ttbackup-format.md) | The `.ttbackup` ZIP layout and per-array JSON shape, derived from the import code |

## Stores in use (overview)

| Store | Module | Purpose | Source |
|---|---|---|---|
| Room (optionally SQLCipher-encrypted) | `:mobile` | Primary local DB for journal, milestones, audio metadata, voice goals | [`mobile/build.gradle`](../../../mobile/build.gradle); schemas at `mobile/schemas/` |
| Realm Kotlin (read-mostly, legacy) | `:mobile` | TransTracks `.ttbackup` import + a handful of legacy adapters + one Wear write path | Realm plugin in root [`build.gradle`](../../../build.gradle); see [`realm-models.md`](./realm-models.md) |
| EncryptedSharedPreferences | `:mobile` | DB passphrases (reserved) | `androidx.security:security-crypto`; see [`shared-prefs-keys.md`](./shared-prefs-keys.md) |
| Default `SharedPreferences` | `:mobile` | Lock state, theme, sync toggles, all user settings | wrapped by `SettingsManager` + `PrefUtil`; see [`shared-prefs-keys.md`](./shared-prefs-keys.md) |
| App-private file storage | `:mobile` | Photos, audio recordings | see [`file-layout.md`](./file-layout.md) |
| Firestore | `:mobile` | Optional cloud sync for **settings only** — never for photo/audio bytes | see [`apis/firebase-contracts.md`](../apis/firebase-contracts.md) |

See [`ENCRYPTED_DATABASE.md`](../../../ENCRYPTED_DATABASE.md) for the SQLCipher integration design.
