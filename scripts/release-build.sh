#!/usr/bin/env bash
set -euo pipefail

# ==============================================================================
# CourseDeals ComposeMultiplatform Release Build Script
# Targets: APK, IPA, DMG (scaffold), EXE (scaffold), Desktop Jar (scaffold)
# ==============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
OUTPUT_DIR="${REPO_ROOT}/build/release-artifacts"

VERSION=""
TARGET="all" # all, apk, ipa, dmg, exe, jar
FLAVOR="prod"
BUILD_TYPE="release"

print_usage() {
  cat <<EOF
Usage: $(basename "$0") [OPTIONS]

Options:
  -v, --version <ver>      Release version tag (e.g. v1.0.0 or 1.0.0) [Required]
  -t, --target <target>    Target artifact: all, apk, ipa, dmg, exe, jar [Default: all]
  -f, --flavor <flavor>    Android product flavor: prod or local [Default: prod]
  -o, --output <dir>       Artifact output directory [Default: build/release-artifacts]
  -h, --help               Show this help message

Examples:
  $(basename "$0") --version v1.0.0
  $(basename "$0") --version v1.0.0 --target apk
  $(basename "$0") --version v1.0.0 --target ipa
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    -v|--version)
      VERSION="$2"
      shift 2
      ;;
    -t|--target)
      TARGET="$2"
      shift 2
      ;;
    -f|--flavor)
      FLAVOR="$2"
      shift 2
      ;;
    -o|--output)
      OUTPUT_DIR="$2"
      shift 2
      ;;
    -h|--help)
      print_usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      print_usage
      exit 1
      ;;
  esac
done

if [[ -z "${VERSION}" ]]; then
  echo "Error: --version is required (e.g. --version v1.0.0)" >&2
  exit 1
fi

# Normalize version (strip leading 'v' for file names, keep for tags)
CLEAN_VERSION="${VERSION#v}"
mkdir -p "${OUTPUT_DIR}"

echo "=========================================================="
echo " Building CourseDeals Client Release"
echo " Version: ${VERSION} (clean: ${CLEAN_VERSION})"
echo " Target:  ${TARGET}"
echo " Output:  ${OUTPUT_DIR}"
echo "=========================================================="

cd "${REPO_ROOT}"

# ------------------------------------------------------------------------------
# 1. Android APK Build
# ------------------------------------------------------------------------------
build_apk() {
  echo ""
  echo "==> [1/5] Building Android APK (flavor: ${FLAVOR}, type: ${BUILD_TYPE})..."
  local task_flavor
  task_flavor="$(tr '[:lower:]' '[:upper:]' <<< "${FLAVOR:0:1}")${FLAVOR:1}"
  local task_type
  task_type="$(tr '[:lower:]' '[:upper:]' <<< "${BUILD_TYPE:0:1}")${BUILD_TYPE:1}"
  local task_name=":app:assemble${task_flavor}${task_type}"

  ./gradlew "${task_name}" -x test --stacktrace

  local source_apk="${REPO_ROOT}/app/build/outputs/apk/${FLAVOR}/${BUILD_TYPE}/app-${FLAVOR}-${BUILD_TYPE}.apk"
  if [[ ! -f "${source_apk}" ]]; then
    # Fallback to search any generated release apk
    source_apk=$(find "${REPO_ROOT}/app/build/outputs/apk" -name "*${BUILD_TYPE}*.apk" | head -n 1)
  fi

  if [[ -f "${source_apk}" ]]; then
    local target_apk="${OUTPUT_DIR}/CourseDeals-${CLEAN_VERSION}-${FLAVOR}.apk"
    cp -v "${source_apk}" "${target_apk}"
    echo "✔ Android APK copied to: ${target_apk}"

    # Verify and ensure valid signature with apksigner
    local apksigner_bin=""
    if command -v apksigner >/dev/null 2>&1; then
      apksigner_bin="apksigner"
    else
      local sdk_path="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/Volumes/MacExt/thanh0x/Library/Android/sdk}}"
      apksigner_bin=$(find "${sdk_path}/build-tools" -name apksigner 2>/dev/null | sort -V | tail -n 1 || true)
    fi

    if [[ -n "${apksigner_bin}" && -x "${apksigner_bin}" ]]; then
      echo "Checking APK signature with apksigner..."
      if ! "${apksigner_bin}" verify "${target_apk}" >/dev/null 2>&1; then
        echo "⚠️ APK has no valid signature. Signing with debug keystore..."
        local debug_keystore="${HOME}/.android/debug.keystore"
        if [[ -f "${debug_keystore}" ]]; then
          "${apksigner_bin}" sign --ks "${debug_keystore}" --ks-pass pass:android --key-pass pass:android "${target_apk}"
          echo "✔ APK signed successfully."
        else
          echo "⚠️ debug.keystore not found at ${debug_keystore}."
        fi
      else
        echo "✔ APK signature verified (valid signature schemes present)."
      fi
    fi
  else
    echo "❌ Error: APK not found at expected path: ${source_apk}" >&2
    return 1
  fi
}

# ------------------------------------------------------------------------------
# 2. iOS IPA Build
# ------------------------------------------------------------------------------
build_ipa() {
  echo ""
  echo "==> [2/5] Building iOS Release IPA..."
  if ! command -v xcodebuild >/dev/null 2>&1; then
    echo "⚠️ xcodebuild not found. Skipping iOS build (requires macOS with Xcode)."
    return 0
  fi

  # First link release framework for arm64
  echo "Linking iOS Release framework in Gradle..."
  ./gradlew :shared:linkReleaseFrameworkIosArm64 -x test

  local xcproj="${REPO_ROOT}/iosApp/iosApp.xcodeproj"
  local archive_path="${REPO_ROOT}/build/ios-archive/iosApp-${CLEAN_VERSION}.xcarchive"
  mkdir -p "$(dirname "${archive_path}")"

  echo "Archiving Xcode project..."
  xcodebuild archive \
    -project "${xcproj}" \
    -scheme iosApp \
    -configuration Release \
    -destination "generic/platform=iOS" \
    -archivePath "${archive_path}" \
    CODE_SIGNING_ALLOWED=NO \
    CODE_SIGNING_REQUIRED=NO \
    CODE_SIGN_IDENTITY="" \
    | xcbeautify 2>/dev/null || true

  local app_path="${archive_path}/Products/Applications/iosApp.app"
  if [[ -d "${app_path}" ]]; then
    local ipa_target="${OUTPUT_DIR}/CourseDeals-${CLEAN_VERSION}-ios.ipa"
    local payload_dir="${REPO_ROOT}/build/ios-archive/Payload"
    rm -rf "${payload_dir}"
    mkdir -p "${payload_dir}"
    cp -R "${app_path}" "${payload_dir}/"
    (
      cd "${REPO_ROOT}/build/ios-archive"
      zip -q -r "${ipa_target}" Payload
    )
    rm -rf "${payload_dir}"
    echo "✔ iOS IPA created: ${ipa_target}"
  else
    echo "⚠️ .xcarchive built at ${archive_path}, but app bundle not found at ${app_path}. Zipping entire archive."
    local zip_target="${OUTPUT_DIR}/CourseDeals-${CLEAN_VERSION}-ios.xcarchive.zip"
    (cd "$(dirname "${archive_path}")" && zip -q -r "${zip_target}" "$(basename "${archive_path}")")
    echo "✔ iOS Archive created: ${zip_target}"
  fi
}

# ------------------------------------------------------------------------------
# 3. macOS DMG & Standalone App Build
# ------------------------------------------------------------------------------
build_dmg() {
  echo ""
  echo "==> [3/5] Building macOS Native App & DMG..."
  ./gradlew :desktopApp:packageReleaseDmg || ./gradlew :desktopApp:packageDmg

  local dmg_file
  dmg_file=$(find "${REPO_ROOT}/desktopApp/build/compose/binaries" -name "*.dmg" | grep "main-release" | head -n 1 || true)
  if [[ -z "${dmg_file}" ]]; then
    dmg_file=$(find "${REPO_ROOT}/desktopApp/build/compose/binaries" -name "*.dmg" | head -n 1 || true)
  fi
  if [[ -n "${dmg_file}" && -f "${dmg_file}" ]]; then
    local target_dmg="${OUTPUT_DIR}/CourseDeals-${CLEAN_VERSION}-macos.dmg"
    cp -v "${dmg_file}" "${target_dmg}"
    echo "✔ macOS DMG installer created: ${target_dmg}"
  fi

  local app_dir
  app_dir=$(find "${REPO_ROOT}/desktopApp/build/compose/binaries" -type d -name "CourseDeals.app" | grep "main-release" | head -n 1 || true)
  if [[ -z "${app_dir}" ]]; then
    app_dir=$(find "${REPO_ROOT}/desktopApp/build/compose/binaries" -type d -name "CourseDeals.app" | head -n 1 || true)
  fi
  if [[ -n "${app_dir}" && -d "${app_dir}" ]]; then
    local target_zip="${OUTPUT_DIR}/CourseDeals-${CLEAN_VERSION}-macos-app.zip"
    echo "Creating compressed macOS .app bundle: ${target_zip}..."
    (cd "$(dirname "${app_dir}")" && zip -r -q "${target_zip}" "$(basename "${app_dir}")")
    echo "✔ macOS standalone .app bundle created: ${target_zip}"
  fi
}

# ------------------------------------------------------------------------------
# 4. Windows EXE Build (Scaffold Hook)
# ------------------------------------------------------------------------------
build_exe() {
  echo ""
  echo "==> [4/5] Checking Windows EXE Target..."
  if ./gradlew tasks --all 2>/dev/null | grep -q "packageMsi\|packageExe"; then
    echo "Running ./gradlew packageMsi / packageExe..."
    ./gradlew packageMsi || ./gradlew packageExe
    local exe_file
    exe_file=$(find "${REPO_ROOT}" \( -name "*.msi" -o -name "*.exe" \) | head -n 1)
    if [[ -n "${exe_file}" && -f "${exe_file}" ]]; then
      local target_exe="${OUTPUT_DIR}/CourseDeals-${CLEAN_VERSION}-windows${exe_file##*.}"
      cp -v "${exe_file}" "${target_exe}"
      echo "✔ Windows installer created: ${target_exe}"
      return 0
    fi
  fi

  echo "ℹ️  Windows EXE scaffold hook ready: Compose Desktop native packaging requires Windows host or Cross-compiler."
}

# ------------------------------------------------------------------------------
# 5. Desktop Runnable JAR (Scaffold Hook)
# ------------------------------------------------------------------------------
build_desktop_jar() {
  echo ""
  echo "==> [5/5] Checking Desktop JAR Target..."
  if ./gradlew tasks --all 2>/dev/null | grep -q "packageUberJarForCurrentOS"; then
    echo "Running ./gradlew packageUberJarForCurrentOS..."
    ./gradlew packageUberJarForCurrentOS
    local jar_file
    jar_file=$(find "${REPO_ROOT}" -name "*-all.jar" -o -name "*desktop*.jar" | head -n 1)
    if [[ -n "${jar_file}" && -f "${jar_file}" ]]; then
      local target_jar="${OUTPUT_DIR}/CourseDeals-${CLEAN_VERSION}-desktop.jar"
      cp -v "${jar_file}" "${target_jar}"
      echo "✔ Desktop JAR created: ${target_jar}"
      return 0
    fi
  fi

  echo "ℹ️  Desktop JAR scaffold hook ready: packageUberJarForCurrentOS will be invoked once desktop module is registered."
}

# ------------------------------------------------------------------------------
# Execute Requested Targets
# ------------------------------------------------------------------------------
IFS=',' read -ra TARGET_LIST <<< "${TARGET}"
for t in "${TARGET_LIST[@]}"; do
  case "${t}" in
    all)
      build_apk
      build_ipa
      build_dmg
      build_exe
      build_desktop_jar
      break
      ;;
    apk)
      build_apk
      ;;
    ipa)
      build_ipa
      ;;
    dmg)
      build_dmg
      ;;
    exe)
      build_exe
      ;;
    jar)
      build_desktop_jar
      ;;
    *)
      echo "ℹ️  Skipping non-client target: ${t}"
      ;;
  esac
done

# ------------------------------------------------------------------------------
# Checksums
# ------------------------------------------------------------------------------
echo ""
echo "==> Generating SHA256 checksums..."
(
  cd "${OUTPUT_DIR}"
  if ls ./* 2>/dev/null | grep -v "SHA256SUMS.txt" >/dev/null; then
    shasum -a 256 ./* 2>/dev/null | grep -v "SHA256SUMS.txt" > SHA256SUMS.txt || true
    echo "✔ SHA256SUMS.txt generated:"
    cat SHA256SUMS.txt
  fi
)

echo ""
echo "=========================================================="
echo " Finished Client Release Artifacts:"
ls -lh "${OUTPUT_DIR}"
echo "=========================================================="
