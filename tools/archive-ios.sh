#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
archive_path="${1:-$PWD/build/release/CircuitConnection-unsigned.xcarchive}"
# Scheme is the preferred archive path. Do not silently substitute a build for an archive.
xcodebuild -project iosApp/CircuitConnection.xcodeproj -scheme CircuitConnection \
  -configuration Release -destination 'generic/platform=iOS' \
  -archivePath "$archive_path" CODE_SIGNING_ALLOWED=NO archive
