#!/usr/bin/env bash
# start-emulator.sh — Start the medium_phone AVD headlessly and wait for ADB.
#
# Usage:
#   bash .github/ci-scripts/start-emulator.sh [--cold]
#
# Options:
#   --cold   Force a cold boot (ignore saved snapshot).
#            Use when the snapshot is stale or you need a clean state.
#
# After this script exits successfully the emulator is reachable via plain
# `adb shell` with no extra flags.  The device serial is emulator-5554.
#
# Requirements (all satisfied by copilot-setup-steps.yml):
#   • Xvfb on PATH
#   • $ANDROID_SDK_ROOT set
#   • AVD "medium_phone" created with the google_apis (non-PlayStore) image
#   • libpulse0 installed
set -euo pipefail

COLD=0
for arg in "$@"; do
  [ "$arg" = "--cold" ] && COLD=1
done

# ── Virtual framebuffer ──────────────────────────────────────────────────────
if ! pgrep -x Xvfb > /dev/null 2>&1; then
  echo "[emulator] Starting Xvfb on :99"
  Xvfb :99 -screen 0 1280x800x24 &
  sleep 1
fi
export DISPLAY=:99

# ── Build emulator flags ─────────────────────────────────────────────────────
EMU_FLAGS=(
  -avd medium_phone
  -no-window
  -gpu swiftshader_indirect
  -noaudio
  -no-boot-anim
  -accel on
  -qemu -m 2048
)

if [ "$COLD" = "1" ]; then
  EMU_FLAGS+=(-no-snapshot-load)
  echo "[emulator] Cold boot requested — ignoring saved snapshot"
else
  echo "[emulator] Loading from snapshot 'agent_ready' (fastest start)"
fi

# ── Launch emulator ──────────────────────────────────────────────────────────
"$ANDROID_SDK_ROOT/emulator/emulator" "${EMU_FLAGS[@]}" \
  > /tmp/emulator.log 2>&1 &
EMU_PID=$!
echo "[emulator] Emulator PID: $EMU_PID"
disown "$EMU_PID"

# ── Wait for ADB device ──────────────────────────────────────────────────────
echo "[emulator] Waiting for ADB device..."
adb wait-for-device

# ── Poll for full boot ────────────────────────────────────────────────────────
echo "[emulator] Waiting for Android to finish booting..."
for i in $(seq 1 60); do
  BOOTED=$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')
  if [ "$BOOTED" = "1" ]; then
    echo "[emulator] Android fully booted (check $i/60)."
    break
  fi
  echo "[emulator]   Boot check $i/60: sys.boot_completed=${BOOTED:-<empty>}"
  sleep 5
done

if [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; then
  echo "[emulator] ERROR: emulator did not boot in 5 minutes."
  tail -40 /tmp/emulator.log
  exit 1
fi

# ── Confirm ADB access ────────────────────────────────────────────────────────
echo "[emulator] ADB status: $(adb get-state)"
echo "[emulator] Emulator ready.  Use 'adb devices' to confirm."
