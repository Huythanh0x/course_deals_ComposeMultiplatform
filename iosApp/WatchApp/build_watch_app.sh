#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
FRAMEWORK_DIR="$REPO_ROOT/domain/coupons/build/bin/watchosSimulatorArm64/debugFramework"
BUILD_DIR="/tmp/CourseDealsWatchBuild/WatchApp.app"
PLUGIN_DIR="$BUILD_DIR/PlugIns/WatchWidgetsExtension.appex"
SDK_PATH="$(xcrun --sdk watchsimulator --show-sdk-path)"
TARGET_TRIPLE="arm64-apple-watchos10.0-simulator"

echo "0. Building shared KMP watchOS framework (CourseDealsWatch.framework)..."
(cd "$REPO_ROOT" && ./gradlew :domain:coupons:linkDebugFrameworkWatchosSimulatorArm64 --quiet)

rm -rf "$BUILD_DIR"
mkdir -p "$PLUGIN_DIR"

echo "1. Compiling WatchApp main binary (App + Core + Features)..."
xcrun --sdk watchsimulator swiftc \
  -target "$TARGET_TRIPLE" \
  -sdk "$SDK_PATH" \
  -F "$FRAMEWORK_DIR" \
  -framework CourseDealsWatch \
  -framework Foundation \
  -framework SwiftUI \
  -Xlinker -objc_abi_version -Xlinker 2 \
  -parse-as-library \
  "$SCRIPT_DIR/App/WatchApp.swift" \
  "$SCRIPT_DIR/Core/Model/WatchDealModel.swift" \
  "$SCRIPT_DIR/Core/Storage/WatchDealCacheStore.swift" \
  "$SCRIPT_DIR/Core/Connectivity/WatchConnectivityClient.swift" \
  "$SCRIPT_DIR/Features/DealFeed/WatchDealListView.swift" \
  "$SCRIPT_DIR/Features/DealFeed/WatchDealCardView.swift" \
  "$SCRIPT_DIR/Features/DealDetail/WatchDealDetailView.swift" \
  -o "$BUILD_DIR/WatchApp"

cp "$SCRIPT_DIR/App/Info.plist" "$BUILD_DIR/Info.plist"

echo "2. Compiling WatchWidgetsExtension (.appex) binary (Core/Model + Core/Storage + Widgets)..."
xcrun --sdk watchsimulator swiftc \
  -target "$TARGET_TRIPLE" \
  -sdk "$SDK_PATH" \
  -F "$FRAMEWORK_DIR" \
  -framework CourseDealsWatch \
  -framework Foundation \
  -framework SwiftUI \
  -Xlinker -objc_abi_version -Xlinker 2 \
  -parse-as-library \
  -application-extension \
  -DWIDGET_EXTENSION \
  -Xlinker -e -Xlinker _NSExtensionMain \
  "$SCRIPT_DIR/Core/Model/WatchDealModel.swift" \
  "$SCRIPT_DIR/Core/Storage/WatchDealCacheStore.swift" \
  "$SCRIPT_DIR/Widgets/Provider/WatchDealTimelineProvider.swift" \
  "$SCRIPT_DIR/Widgets/Views/ComplicationViews.swift" \
  "$SCRIPT_DIR/Widgets/Views/SmartStackWidget.swift" \
  "$SCRIPT_DIR/Widgets/WatchWidgetBundle.swift" \
  -o "$PLUGIN_DIR/WatchWidgetsExtension"

cp "$SCRIPT_DIR/Widgets/Info.plist" "$PLUGIN_DIR/Info.plist"

echo "3. Code-signing WatchWidgetsExtension.appex and WatchApp.app..."
codesign --force --sign - "$PLUGIN_DIR"
codesign --force --sign - "$BUILD_DIR"

echo "✅ Built WatchApp.app + WatchWidgetsExtension.appex at: $BUILD_DIR"
