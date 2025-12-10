#!/usr/bin/env bash
set -euo pipefail

echo "Preparing secrets.properties..."

# If no SECRETS_PROPERTIES_B64 secret exists, use the example file
if [[ -z "${SECRETS_PROPERTIES_B64:-}" ]]; then
  echo "SECRETS_PROPERTIES_B64 is not set. Using secrets.properties.example as fallback."
  cp secrets.properties.example secrets.properties
else
  echo "Decoding secrets.properties from SECRETS_PROPERTIES_B64 secret..."
  echo "${SECRETS_PROPERTIES_B64}" | base64 --decode > secrets.properties
fi

echo "secrets.properties is ready"
