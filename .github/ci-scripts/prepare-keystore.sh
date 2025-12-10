#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${ANDROID_KEYSTORE_B64:-}" ]]; then
  echo "ANDROID_KEYSTORE_B64 is not set. Skipping release keystore creation."
  exit 0
fi

mkdir -p keys

echo "Decoding release-keystore.jks from ANDROID_KEYSTORE_B64 secret..."
echo "${ANDROID_KEYSTORE_B64}" | base64 --decode > keys/release-keystore.jks

echo "Release keystore written to keys/release-keystore.jks"
