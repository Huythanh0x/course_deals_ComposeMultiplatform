---
name: capture-evidence
description: Capture multi-state UI screenshots, run cross-platform GUI framework tests (Maestro) across Android & iOS with step-by-step screenshot capture, visually inspect pixels, and publish feature-rich step-by-step evidence to GitHub PR.
---

# Capture Evidence & Cross-Platform Mobile Verification (Android + iOS)

Mobile verification in `course_deals_ComposeMultiplatform` is strictly cross-platform (Kotlin Multiplatform: Android + iOS), multi-state across 4 quadrants, and mapped directly against the ticket's Definition of Done (DoD) matrix with feature-rich step-by-step side-by-side evidence published to the GitHub PR.

---

## 1. Multi-State UI Verification Across 4 Quadrants

A single arbitrary screenshot is strictly forbidden. Evidence capture requires testing and capturing discrete states across the 4 quadrants:

1. **Quadrant 1 (Happy Path - Empty/Initial State)**: Initial screen render, clean layout, placeholder or zero-item display.
2. **Quadrant 1 (Happy Path - Populated/Success State)**: Screen with live data populated from running local backend (`localhost:8081`).
3. **Quadrant 2 (Failed Path - Domain/Validation State)**: Form validation errors, invalid OTP/credentials feedback, non-blocking alert dialogs.
4. **Quadrant 3 (Error Path - Offline/System Failure)**: Network disconnection retry banner, HTTP 500 error envelope, timeout recovery.
5. **Quadrant 4 (Related Changes / Regressions)**: System bar insets (status bar, navigation bar, dynamic island), Dark/Light theme parity.

---

## 2. Execute Defined GUI Framework Tests with Embedded Screenshot Capture (`maestro`)

The declarative Maestro test flows in `.maestro/flows/` **MUST embed `- takeScreenshot: <step_name>` checkpoints** at each significant transition of the user journey.

Always execute the automated GUI framework test suite across **ALL THREE** platforms (Android, iOS, and Desktop):

```bash
# Android Emulator (emulator-5554)
~/.maestro/bin/maestro --device emulator-5554 test -e APP_ID=com.thanh0x.coursedeals .maestro/flows/<feature-flow>.yaml

# iOS Simulator (booted iPhone Simulator)
~/.maestro/bin/maestro --device booted test -e APP_ID=com.thanh0x.coursedeals.iosApp .maestro/flows/<feature-flow>.yaml

# Compose Desktop (:desktopApp Maestro Flow Runner)
./gradlew :desktopApp:runMaestroFlow -Pflow=.maestro/flows/<feature-flow>.yaml -PscreenshotDir=/tmp/desktop-e2e
```

If platform-specific flows are defined (e.g. `<flow>.yaml` and `<flow>-ios.yaml`), execute each on its respective platform.

---

## 3. Mandatory Visual Pixel Inspection (`read_file`) & Integrity Policy

> [!IMPORTANT]
> **Strict Zero-Assumption & Mandatory Pixel Inspection Policy**:
> 1. **Visual Pixel Inspection via `read_file`**: After capturing any screenshot on either Android or iOS, agents **MUST invoke `read_file`** on the `.png` image path to view and inspect actual pixels. Never claim a UI state is verified, rendered, or error-free without inspecting the image first.
> 2. **Catch Rendering / Connection Errors**: Verify that the screenshot actually displays the expected UI state (e.g. proper controls, populated list, or expected error dialog) rather than an unhandled connection crash, raw stacktrace, or missing view.
> 3. **Document Technical Constraints as Separate PR Comments**: If a specific UI state or activity cannot be driven or captured on device due to an operating system or platform technical constraint (e.g. non-exported activity without deeplink, biometric hardware limitation), **do NOT fabricate or claim visual verification**. Instead, document the exact technical constraint, steps attempted, and root cause in a separate, dedicated comment on the PR.

---

## 4. Multi-Platform Capture & Cloudflare R2 Upload

### A. Capture Screenshots on Android & iOS
- **Android Emulator**:
  ```bash
  adb -s emulator-5554 exec-out screencap -p > .agents/tickets/<issue>-<slug>/evidence/<step>_android.png
  ```
- **iOS Simulator**:
  ```bash
  xcrun simctl io booted screenshot .agents/tickets/<issue>-<slug>/evidence/<step>_ios.png
  ```

### B. Visually Inspect Captured Images
Invoke `read_file` on both `<step>_android.png` and `<step>_ios.png` to inspect layout, insets, text rendering, and colors.

### C. Upload to Cloudflare R2
```bash
worker_base="https://r2-image-worker.huythanh0x.workers.dev"
response="$(curl -sS -X PUT "${worker_base}/upload" \
  -u "$R2_IMAGE_UPLOAD_USER:$R2_IMAGE_UPLOAD_TOKEN" \
  -F "image=@.agents/tickets/<issue>-<slug>/evidence/<step>_<platform>.png")"
```
Extract public image URL (`${worker_base}/<filename>`) and verify HTTP 200 (`curl -sS -o /dev/null -w '%{http_code}' "$url"`). Never use `pub-*.r2.dev` bucket URLs.

---

## 5. Feature-Rich Step-by-Step Flow Evidence in GitHub PR

Every mobile/KMP PR **MUST** present a feature-rich, numbered step-by-step user journey walkthrough comparing Android, iOS, and Desktop side-by-side using `<img src="..." width="250" />`:

```markdown
## Screenshots

| Step | Android Evidence (`emulator-5554`) | iOS Evidence (`iPhone Simulator`) | Desktop Evidence (`:desktopApp`) |
| :--- | :---: | :---: | :---: |
| **1. [Step Name]**<br>• [Key assertion / displayed elements]<br>• [Input or action triggering transition]<br>• [Zero coordinate taps] | <img src="<android_r2_url>" width="250" /> | <img src="<ios_r2_url>" width="250" /> | <img src="<desktop_r2_url>" width="250" /> |
| **2. [Step Name]**<br>• [Key assertion / displayed elements]<br>• [Input or action triggering transition] | <img src="<android_r2_url>" width="250" /> | <img src="<ios_r2_url>" width="250" /> | <img src="<desktop_r2_url>" width="250" /> |
| **3. [Step Name]**<br>• [Key assertion / dialog modal]<br>• [Accept / Decline or error recovery] | <img src="<android_r2_url>" width="250" /> | <img src="<ios_r2_url>" width="250" /> | <img src="<desktop_r2_url>" width="250" /> |
| **4. [Step Name]**<br>• [Target destination loaded]<br>• [Live backend data populated] | <img src="<android_r2_url>" width="250" /> | <img src="<ios_r2_url>" width="250" /> | <img src="<desktop_r2_url>" width="250" /> |
| **5. [Step Name]**<br>• [Profile / Settings updated]<br>• [Dark mode & insets parity verified] | <img src="<android_r2_url>" width="250" /> | <img src="<ios_r2_url>" width="250" /> | <img src="<desktop_r2_url>" width="250" /> |

### Automated Maestro GUI Test Results
- Android (`emulator-5554`): **PASSED** (100% assertions green)
- iOS (`iPhone Simulator`): **PASSED** (100% assertions green)
- Desktop (`:desktopApp:runMaestroFlow`): **PASSED** (100% assertions green)
```
