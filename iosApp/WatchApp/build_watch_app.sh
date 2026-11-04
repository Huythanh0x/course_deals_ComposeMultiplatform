#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
BUILD_DIR="/tmp/CourseDealsWatchBuild/WatchApp.app"
PLUGIN_DIR="$BUILD_DIR/PlugIns/WatchWidgetsExtension.appex"
SDK_PATH="$(xcrun --sdk watchsimulator --show-sdk-path)"
TARGET_TRIPLE="arm64-apple-watchos10.0-simulator"

echo "0. Regenerating Watch Swift Codable DTOs (:shared:generateWatchSwiftModels)..."
(cd "$REPO_ROOT" && ./gradlew :shared:generateWatchSwiftModels --quiet)

rm -rf "$BUILD_DIR"
mkdir -p "$PLUGIN_DIR"

echo "1. Compiling WatchApp main binary..."
xcrun --sdk watchsimulator swiftc \
  -target "$TARGET_TRIPLE" \
  -sdk "$SDK_PATH" \
  -parse-as-library \
  "$SCRIPT_DIR/WatchApp.swift" \
  "$SCRIPT_DIR/Generated/WatchDealDto.generated.swift" \
  "$SCRIPT_DIR/WatchDealModel.swift" \
  "$SCRIPT_DIR/WatchDealCacheStore.swift" \
  "$SCRIPT_DIR/WatchConnectivityClient.swift" \
  "$SCRIPT_DIR/WatchDealListView.swift" \
  "$SCRIPT_DIR/WatchDealDetailView.swift" \
  "$SCRIPT_DIR/WatchWidgetBundle.swift" \
  "$SCRIPT_DIR/SmartStackWidget.swift" \
  -o "$BUILD_DIR/WatchApp"

cp "$SCRIPT_DIR/Info.plist" "$BUILD_DIR/Info.plist"

echo "2. Compiling WatchWidgetsExtension (.appex) binary..."
xcrun --sdk watchsimulator swiftc \
  -target "$TARGET_TRIPLE" \
  -sdk "$SDK_PATH" \
  -parse-as-library \
  -application-extension \
  -DWIDGET_EXTENSION \
  -Xlinker -e -Xlinker _NSExtensionMain \
  "$SCRIPT_DIR/Generated/WatchDealDto.generated.swift" \
  "$SCRIPT_DIR/WatchDealModel.swift" \
  "$SCRIPT_DIR/WatchDealCacheStore.swift" \
  "$SCRIPT_DIR/WatchWidgetBundle.swift" \
  "$SCRIPT_DIR/SmartStackWidget.swift" \
  -o "$PLUGIN_DIR/WatchWidgetsExtension"

cp "$SCRIPT_DIR/WatchWidgetsInfo.plist" "$PLUGIN_DIR/Info.plist"

echo "3. Code-signing WatchWidgetsExtension.appex and WatchApp.app..."
codesign --force --sign - "$PLUGIN_DIR"
codesign --force --sign - "$BUILD_DIR"

echo "✅ Built WatchApp.app + WatchWidgetsExtension.appex at: $BUILD_DIR"
