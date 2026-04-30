# Unknowns

Open items the source did not fully answer during the pass-1 KB build. These are not bugs — they are documentation gaps.

## SDK targets disagree between docs and source

`mobile/build.gradle` declares `minSdkVersion 26` and `targetSdkVersion 35`, but `.github/copilot-instructions.md` describes `:mobile` as `minSdk 21` and `targetSdk 36`. The KB ([`architecture.md`](../architecture.md)) records the source-derived values and notes the disagreement; the prose docs need a follow-up reconciliation.

Evidence:

- `mobile/build.gradle` `defaultConfig`
- `.github/copilot-instructions.md` "Repository layout" table

Next step: a maintainer decides which values are intended and updates whichever is wrong (the code, the prose, or both). Mirror page: [`questions/index.md`](../questions/index.md).

## Pass-2 scope is documented but not yet executed

Per-feature, per-entity, and per-file pages are intentionally deferred (see [`progress.md`](./progress.md)). They are not "unknown" in the sense of contradicting the source — they are simply not yet documented.

## CI secret name list may be incomplete

[`configuration.md`](../configuration.md) lists the well-known secrets (`KEYSTORE_64`, `STORE_PASS`, `KEY_ALIAS`, `KEY_PASS`, plus the secrets read by `prepare-secrets.sh` / `prepare-google-services.sh`). The pass-1 build did not exhaustively read every `env:` block in every workflow; future passes should diff `.github/ci-scripts/*.sh` against the workflow `env:` declarations to confirm completeness.
