# Data

Detailed pages:

- [`room-entities.md`](./room-entities.md) — `AppDatabase` (v3), the four Room entities, every DAO query, both migrations, and the SQLCipher integration point.
- [`realm-schema.md`](./realm-schema.md) — Read-only Realm models retained for legacy `.ttbackup` / `.realm` ingestion.

## Stores in use

| Store | Module | Purpose | Source |
|---|---|---|---|
| Room (optionally SQLCipher-encrypted) | `:mobile` | Primary local DB for journal, milestones, audio metadata | [`mobile/build.gradle`](../../../mobile/build.gradle); schemas at `mobile/schemas/` |
| Realm Kotlin (read-only) | `:mobile` | TransTracks `.ttbackup` import only | Realm plugin in root [`build.gradle`](../../../build.gradle) |
| EncryptedSharedPreferences | `:mobile` | App-lock state, decoy-vault state, small secrets | `androidx.security:security-crypto` |
| App-private file storage | `:mobile` | Photos, audio recordings | Android scoped storage |
| Firestore | `:mobile` | Optional cloud sync for account-tied data | Firebase |

See [`ENCRYPTED_DATABASE.md`](../../../ENCRYPTED_DATABASE.md) for the SQLCipher integration design.

## Next pass

Per-store deep-dives beyond the entity tables (e.g. EncryptedSharedPreferences key inventory, Firestore document shapes) — using the API/Contract template in [`.github/skills/repo-knowledge-base/SKILL.md`](../../../.github/skills/repo-knowledge-base/SKILL.md) §"Eighth Pass".
