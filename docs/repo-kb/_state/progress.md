# Progress

## Pass 1 — Bootstrap (this session)

- [x] Move `repo-knowledge-base-SKILL.md` from repo root to `.github/skills/repo-knowledge-base/SKILL.md` (per [GitHub Agent Skills docs](https://docs.github.com/en/copilot/concepts/agents/about-agent-skills)).
- [x] Create `docs/repo-kb/` directory tree.
- [x] Write top-level pages: `index.md`, `repo-map.md`, `architecture.md`, `quickstart-for-agents.md`, `build-and-release.md`, `testing.md`, `configuration.md`, `security-and-risk.md`, `maintenance-guide.md`, `glossary.md`, `dependency-map.md`, `data-flow.md`.
- [x] Write index pages for `features/`, `components/`, `apis/`, `data/`, `workflows/`, `decisions/`, `questions/`, `files/`.
- [x] Write `_state/` files: `progress.md` (this), `coverage.md`, `session-log.md`, `unknowns.md`, `command-log.md`.
- [x] Per-workflow summary table for all 9 `.github/workflows/*.yml` files.
- [x] Append KB pointer to `.github/copilot-instructions.md` (existing rules preserved).
- [x] Create `.github/instructions/repo-kb.instructions.md` (path-scoped editing rules).
- [x] Create top-level `AGENTS.md` (Copilot CLI / VS Code agent entry point).
- [x] Record SDK-target discrepancy in `_state/unknowns.md` and `questions/index.md`.

## Pass 2 — Per-feature deep dive (in progress)

- [ ] `features/photo-capture.md` (CameraX + ML Kit)
- [ ] `features/import.md` (TransTracks `.ttbackup` → Realm → Room)
- [ ] `features/app-lock.md` (PIN, pattern, biometric, decoy vault)
- [ ] `features/audio-tracking.md`
- [ ] `features/wear-companion.md`

## Pass 3 — Per-contract / per-entity (in progress)

- [x] `apis/wearable-data-layer.md` — full Mobile↔Wear contract (capabilities, message paths, data-item paths, message keys, helper API, side effects, related test)
- [ ] `data/room-entities.md`
- [ ] `data/realm-import-schema.md`

## Pass 4 — Per-component / per-file (deferred)

- [ ] Application/Activity entry points under `mobile/src/main/java/com/shelbeely/opentransition/`
- [ ] Wear `MainActivity` and `WearableListenerService`
- [ ] Background workers under `mobile/.../background/`
