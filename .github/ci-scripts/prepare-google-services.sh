#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${GOOGLE_SERVICES_JSON:-}" ]]; then
  echo "GOOGLE_SERVICES_JSON is not set. Cannot create app/google-services.json."
  exit 1
fi

mkdir -p app

echo "Decoding google-services.json from GOOGLE_SERVICES_JSON secret..."
echo "${GOOGLE_SERVICES_JSON}" | base64 --decode > app/google-services.json

echo "google-services.json written to app/google-services.json"
