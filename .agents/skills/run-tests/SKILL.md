---
name: run-tests
description: Run multi-module detekt, JVM/Android unit tests, Compose Desktop UI tests, iOS framework link check, and Android lint for changed modules in `course_deals_ComposeMultiplatform` and record real results in `NOTES.md`.
---

# Run Tests & Static Analysis (`run-tests`)

1. Identify affected modules from `.agents/tickets/<issue-number>-<slug>/NOTES.md` (or `git diff --stat`).
2. Run the verification suite per `AGENTS.md`:
   - **Multi-module Detekt (all 17 modules)**:
     `./gradlew detekt`
   - **Unit Tests (JVM & Android localDebug)**:
     `./gradlew testLocalDebugUnitTest :data:coupons:jvmTest :data:user:jvmTest`
   - **Compose Desktop UI & Unit Tests** (when `:desktopApp` or shared UI changes):
     `./gradlew :desktopApp:test`
   - **iOS Simulator Kotlin/Native Framework Link Gate** (when `commonMain`, `iosMain`, or `iosApp` changes):
     `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`
   - **Flavor-qualified Android Lint**:
     `./gradlew :app:lintLocalDebug`
3. Record exact pass/fail counts and report paths (`**/build/reports/tests/**`, `**/build/reports/detekt/**`, `**/build/reports/lint-results-*.html`) under `## Test evidence` → `### Unit tests` in `NOTES.md` and update `STATE.json`.
4. Fix any Detekt, lint, compilation, or test failure before proceeding to `capture-evidence`.
5. Append one line to `## Progress log` in `NOTES.md`: `<timestamp> ran tests — <pass/fail summary>`.
