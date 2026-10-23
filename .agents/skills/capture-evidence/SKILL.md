---
name: capture-evidence
description: Capture multi-state UI screenshots, live emulator verification, and deterministic evidence mapped 1:1 against the ticket's concrete Definition of Done (DoD).
---

# Capture Evidence & Deterministic Mobile Verification

Verification on mobile must be multi-state, deterministic, and mapped directly against the ticket's Definition of Done (DoD) matrix.

---

## 1. Multi-State UI Verification (Avoid Blind Screenshots)

A single arbitrary screenshot is insufficient. Mobile verification requires capturing evidence across multiple discrete states defined in the DoD:

1. **State 1: Initial / Empty State** (e.g. no items found, first-time user prompt).
2. **State 2: Populated / Success State** (with live data populated from the running local backend or mock server).
3. **State 3: Error / Offline / Boundary State** (e.g. network failure retry banner, validation error state).

---

## 2. Emulator Capture & Cloudflare R2 Upload

1. Ensure `emulator-5554` is booted and application is installed via `./gradlew installLocalDebug`.
2. Navigate to target screens using ADB or interactive runner:
   ```bash
   adb exec-out screencap -p > .agents/tickets/<issue>-<slug>/evidence/<state-name>.png
   ```
3. Upload each captured state to Cloudflare R2:
   ```bash
   worker_base="https://r2-image-worker.huythanh0x.workers.dev"
   response="$(curl -sS -X PUT "${worker_base}/upload" \
     -u "$R2_IMAGE_UPLOAD_USER:$R2_IMAGE_UPLOAD_TOKEN" \
     -F "image=@.agents/tickets/<issue>-<slug>/evidence/<state-name>.png")"
   ```
4. Extract the direct image URL from the JSON response.

---

## 3. DoD Mapping Table in PR

Map every captured screenshot and test output directly to the ticket's DoD matrix:

```markdown
### Verification Evidence vs. Definition of Done
| DoD Criterion | Status | Evidence / Artifact |
|---|---|---|
| Empty State Rendered | PASSED | ![Empty State](https://pub-...) |
| Populated Success State | PASSED | ![Success State](https://pub-...) |
| Network Error / Retry State | PASSED | ![Error State](https://pub-...) |
| Detekt Check | PASSED | `./gradlew :app:detekt` (0 issues) |
| Unit Tests | PASSED | `./gradlew testDebugUnitTest` (all passed) |
```
