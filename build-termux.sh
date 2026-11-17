#!/usr/bin/env bash
# ==============================================================================
# YESMIN AI - Termux Android Build Automation Script
# Builds debug APK directly on your Android phone using Termux.
# ==============================================================================

set -e

echo ""
echo "=========================================================="
echo "   YESMIN AI – Termux Build Automation System"
echo "   Building: com.yesmin.ai (Debug APK)"
echo "=========================================================="
echo ""

ARCH=$(uname -m)
echo "[1/7] Detected Device Architecture: $ARCH"

echo "[2/7] Checking Java Development Kit..."
if ! command -v java &> /dev/null; then
    echo "[-] Java not found! Installing openjdk-17..."
    pkg update -y && pkg install -y openjdk-17
fi
JAVA_VER=$(java -version 2>&1 | head -n 1)
echo "[+] Java OK: $JAVA_VER"

echo "[3/7] Checking Android SDK path..."
if [ -z "$ANDROID_HOME" ]; then
    if [ -d "$HOME/android-sdk" ]; then
        export ANDROID_HOME="$HOME/android-sdk"
    elif [ -d "/data/data/com.termux/files/home/android-sdk" ]; then
        export ANDROID_HOME="/data/data/com.termux/files/home/android-sdk"
    else
        export ANDROID_HOME="$HOME/android-sdk"
        mkdir -p "$ANDROID_HOME"
    fi
fi
echo "[+] Android SDK path: $ANDROID_HOME"

echo "sdk.dir=$ANDROID_HOME" > local.properties

echo "[4/7] Checking Gradle..."
if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    GRADLE_CMD="./gradlew"
elif command -v gradle &> /dev/null; then
    GRADLE_CMD="gradle"
else
    pkg install -y gradle
    GRADLE_CMD="gradle"
fi

echo "[5/7] Starting Gradle compile: assembleDebug..."
$GRADLE_CMD assembleDebug --stacktrace --no-daemon

echo "[6/7] Verifying compiled APK..."
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

if [ -f "$APK_PATH" ]; then
    echo ""
    echo "=========================================================="
    echo " [SUCCESS] YESMIN AI APK COMPILED SUCCESSFULLY!"
    echo " APK Path: $APK_PATH"
    echo "=========================================================="
    echo ""
    echo "[7/7] Installation: termux-open $APK_PATH"
else
    echo "[-] Error: Expected APK file not found at $APK_PATH"
    exit 1
fi
