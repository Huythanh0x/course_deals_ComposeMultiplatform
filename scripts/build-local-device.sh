#!/usr/bin/env bash
set -euo pipefail

# ==============================================================================
# CourseDeals Local-Network Physical Device Build & Install Script
# Profile: "physicalDeviceLocal" -> Injects Mac Wi-Fi LAN IP (:8081) into:
#   - KMP :core:common BuildConfig.LOCAL_API_BASE_URL
#   - iOS ComposeApp.framework (iosArm64)
#   - watchOS CourseDealsWatch.framework (watchosDeviceArm64)
#   - Android :app localDebug APK
# ==============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

TARGET="all" # ios, android, all
PORT="8081"
LAN_IP=""
IOS_DEVICE_ID=""
INSTALL_APP="true"

print_usage() {
  cat <<EOF
Usage: $(basename "$0") [OPTIONS]

Options:
  -t, --target <target>    Target platform: ios (iPhone + Watch), android, or all [Default: ios]
  -i, --ip <ip>            Override host LAN IPv4 address [Default: auto-detected via en0/route]
  -p, --port <port>        Local backend API port [Default: 8081]
  -d, --device <udid>      Physical iOS device UDID [Default: auto-detect first connected physical iPhone]
  --no-install             Build only without installing/launching on connected device
  -h, --help               Show this help message

Examples:
  ./scripts/build-local-device.sh
  ./scripts/build-local-device.sh --target ios --port 8081
  ./scripts/build-local-device.sh --target android
  ./scripts/build-local-device.sh --target all --ip 192.168.2.185
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    -t|--target)
      TARGET="$2"
      shift 2
      ;;
    -i|--ip)
      LAN_IP="$2"
      shift 2
      ;;
    -p|--port)
      PORT="$2"
      shift 2
      ;;
    -d|--device)
      IOS_DEVICE_ID="$2"
      shift 2
      ;;
    --no-install)
      INSTALL_APP="false"
      shift
      ;;
    -h|--help)
      print_usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      print_usage
      exit 1
      ;;
  esac
done

detect_lan_ip() {
  local ip=""
  for iface in en0 en1 eth0 wlan0; do
    ip="$(ipconfig getifaddr "${iface}" 2>/dev/null || true)"
    if [[ -n "${ip}" ]]; then
      echo "${ip}"
      return 0
    fi
  done
  ip="$(ifconfig 2>/dev/null | awk '/inet / && $2 != "127.0.0.1" {print $2; exit}' || true)"
  echo "${ip:-127.0.0.1}"
}

if [[ -z "${LAN_IP}" ]]; then
  LAN_IP="$(detect_lan_ip)"
fi

LOCAL_API_URL="http://${LAN_IP}:${PORT}"

echo "=================================================================="
echo " CourseDeals — Physical Device Local Network Profile"
echo " Host LAN API URL : ${LOCAL_API_URL}"
echo " Target Platform  : ${TARGET}"
echo "=================================================================="

if curl -fsS --max-time 2 "${LOCAL_API_URL}/api/v1/coupons/hot-deals?pageIndex=0&numberPerPage=1" >/dev/null 2>&1; then
  echo "✔ Local backend reachable at ${LOCAL_API_URL}"
else
  echo "⚠️  Warning: Local backend at ${LOCAL_API_URL} did not respond within 2s."
  echo "   Ensure mobile-local backend is running on 0.0.0.0:${PORT}."
fi

cd "${REPO_ROOT}"

build_and_install_ios() {
  echo ""
  echo "==> [iOS + watchOS] Compiling KMP frameworks with LOCAL_API_BASE_URL=${LOCAL_API_URL}..."
  ./gradlew \
    :shared:linkDebugFrameworkIosArm64 \
    :domain:coupons:linkDebugFrameworkWatchosDeviceArm64 \
    -PphysicalDeviceLocal=true \
    -PlocalApiUrl="${LOCAL_API_URL}" \
    -x test

  if [[ -z "${IOS_DEVICE_ID}" ]]; then
    IOS_DEVICE_ID="$(xcrun devicectl list devices 2>/dev/null | awk '/physical/ && /iPhone/ {for(i=1;i<=NF;i++) if ($i ~ /^[0-9A-Fa-f-]{20,}$/) {print $i; exit}}' || true)"
  fi

  local destination="generic/platform=iOS"
  if [[ -n "${IOS_DEVICE_ID}" ]]; then
    destination="id=${IOS_DEVICE_ID}"
    echo "✔ Detected physical iPhone UDID: ${IOS_DEVICE_ID}"
  else
    echo "ℹ️  No physical iPhone detected via devicectl; building for generic/platform=iOS."
  fi

  local derived_data="${REPO_ROOT}/build/ios-device-derived-data"
  echo "==> [iOS + watchOS] Building iosApp.app + embedded WatchApp.app (arm64)..."
  xcodebuild \
    -project "${REPO_ROOT}/iosApp/iosApp.xcodeproj" \
    -scheme iosApp \
    -configuration Debug \
    -destination "${destination}" \
    -derivedDataPath "${derived_data}" \
    -allowProvisioningUpdates \
    SKIP_KOTLIN_BUILD=1 \
    build

  local app_bundle="${derived_data}/Build/Products/Debug-iphoneos/iosApp.app"
  echo "✔ Built iOS + Watch bundle: ${app_bundle}"

  if [[ "${INSTALL_APP}" == "true" && -n "${IOS_DEVICE_ID}" ]]; then
    echo "==> Installing iosApp.app (with embedded WatchApp.app) onto ${IOS_DEVICE_ID}..."
    xcrun devicectl device install app --device "${IOS_DEVICE_ID}" "${app_bundle}"
    xcrun devicectl device process launch --device "${IOS_DEVICE_ID}" com.thanh0x.coursedeals.iosApp
    echo "✔ Installed and launched on iPhone (${IOS_DEVICE_ID})!"
  fi
}

build_and_install_android() {
  echo ""
  echo "==> [Android] Building :app:assembleLocalDebug with LOCAL_API_BASE_URL=${LOCAL_API_URL}..."
  ./gradlew :app:assembleLocalDebug \
    -PphysicalDeviceLocal=true \
    -PlocalApiUrl="${LOCAL_API_URL}" \
    -x test

  local apk_path="${REPO_ROOT}/app/build/outputs/apk/local/debug/app-local-debug.apk"
  echo "✔ Built Android Local APK: ${apk_path}"

  if [[ "${INSTALL_APP}" == "true" ]] && command -v adb >/dev/null 2>&1; then
    local android_serial
    android_serial="$(adb devices | awk 'NR>1 && $2=="device" && $1 !~ /^emulator-/ {print $1; exit}' || true)"
    if [[ -n "${android_serial}" ]]; then
      echo "==> Installing APK onto physical Android device (${android_serial})..."
      if ! adb -s "${android_serial}" install -r "${apk_path}"; then
        echo "⚠️  Signature mismatch detected on ${android_serial}; uninstalling existing com.thanh0x.coursedeals and reinstalling..."
        adb -s "${android_serial}" uninstall com.thanh0x.coursedeals || true
        adb -s "${android_serial}" install -r "${apk_path}"
      fi
      adb -s "${android_serial}" shell monkey -p com.thanh0x.coursedeals -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1 || true
      echo "✔ Installed and launched on Android device (${android_serial})!"
    else
      echo "ℹ️  No physical Android device detected via adb. APK ready at: ${apk_path}"
    fi
  fi
}

case "${TARGET}" in
  ios)
    build_and_install_ios
    ;;
  android)
    build_and_install_android
    ;;
  all)
    build_and_install_ios
    build_and_install_android
    ;;
  *)
    echo "Unknown target: ${TARGET} (expected: ios, android, or all)" >&2
    exit 1
    ;;
esac

echo ""
echo "=================================================================="
echo " ✔ Physical Device Local-Network Build Complete (${LOCAL_API_URL})"
echo "=================================================================="
