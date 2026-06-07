#!/usr/bin/env bash

# ------------------------------------------------------------
# run_appium_tests.sh
# ------------------------------------------------------------
# Purpose: Start the Android emulator (if needed), launch Appium with
#          WebView support (auto‑download Chromedriver), run the Appium
#          test suite, and clean up.
# ------------------------------------------------------------

set -e

# ---- Configurable variables ---------------------------------
EMULATOR_NAME="Pixel_4_API_34"
APP_PATH="./appium-tests"
GRADLE_CMD="./gradlew"
APPIUM_PORT=4723
APPIUM_LOG="appium_server.log"
# ------------------------------------------------------------

# Helper: check if a process is listening on a TCP port
function wait_for_port() {
  local host=$1
  local port=$2
  local retries=30
  local wait=2
  echo "Waiting for $host:$port to become reachable..."
  for ((i=1; i<=retries; i++)); do
    if nc -z $host $port >/dev/null 2>&1; then
      echo "Port $port is open."
      return 0
    fi
    sleep $wait
  done
  echo "Timeout waiting for $host:$port"
  return 1
}

# ---- Start Android emulator if not already running ------------
if ! adb devices | grep -q "device$"; then
  echo "Launching Android emulator '$EMULATOR_NAME'..."
  emulator -avd $EMULATOR_NAME -no-snapshot-save -no-window &
  EMU_PID=$!
  # Give the emulator time to boot
  echo "Waiting for emulator to be ready (this may take a minute)..."
  adb wait-for-device
else
  echo "Emulator/device already connected."
fi

# ---- Start Appium server with insecure flag for chromedriver auto‑download ----
echo "Starting Appium server on port $APPIUM_PORT..."
appium --allow-insecure=chromedriver_autodownload --port $APPIUM_PORT > $APPIUM_LOG 2>&1 &
APPIUM_PID=$!

# Wait until Appium is listening
wait_for_port 127.0.0.1 $APPIUM_PORT

# ---- Run the Gradle test suite --------------------------------
cd "$APP_PATH"
$GRADLE_CMD :appium-tests:test --no-daemon

test_status=$?

# ---- Cleanup -------------------------------------------------
# Stop Appium
kill $APPIUM_PID || true
# Stop emulator if we started one
if [ "${EMU_PID+set}" = set ]; then
  echo "Shutting down emulator..."
  adb -s emulator-$EMULATOR_NAME emu kill || true
  kill $EMU_PID || true
fi

exit $test_status
