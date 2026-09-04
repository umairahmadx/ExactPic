#!/usr/bin/env bash
set -euo pipefail

yes | sdkmanager --licenses >/dev/null

if ! avdmanager list avd | grep -q 'Name: exactpic-api35'; then
  echo "no" | avdmanager create avd \
    --name exactpic-api35 \
    --package "system-images;android-35;google_apis;x86_64" \
    --device "pixel_2" \
    --force
fi

java -version
kotlinc -version
sdkmanager --version
adb version
emulator -version