# Course Deals - 1-Week Sprint Lifecycle Guide (Multiplatform Client)

This guide documents the weekly engineering sprint cadence for Course Deals Compose Multiplatform & Kotlin Multiplatform development (**Android**, **iOS**, **watchOS**, and **Desktop**), optimized for a solo full-stack developer paired with Antigravity multi-agent orchestration.

---

## 1. Cadence & Rhythm

| Window | Focus | Key Actions |
|---|---|---|
| **Sunday (Stop & Plan)** | **Retro & Planning** | • Run `sprint-flow retro` to tally completed story points & UI/tech debt.<br>• Run `sprint-flow plan` to select backlog items and set Iteration scope on GitHub Project.<br>• Review cross-repo OpenAPI / Ktor DTO contracts (`./gradlew generateCourseDealsApi`). |
| **Mon - Fri (Build & Test)** | **Execution (`team-flow`)** | • Pick highest-priority issue from GitHub Sprint iteration.<br>• Run `team-flow` to implement Clean Architecture layers (`kmp-engineer`, `apple-developer`, `desktop-engineer`).<br>• Automated static analysis (`detekt`, `lintLocalDebug`) & unit/UI testing. |
| **Saturday (Release & Hardening)** | **Verification & Staging** | • Full live cross-platform E2E smoke tests (`emulator-5554`, `iPhone Simulator`, `:desktopApp`, and `Apple Watch Simulator`).<br>• Verify end-to-end integration against running `mobile-local` backend (port `8081`).<br>• Upload side-by-step verification screenshots to Cloudflare R2.<br>• Cut multi-platform release artifacts (`scripts/release-build.sh`). |

---

## 2. GitHub Projects Integration

- **Board Name**: `"Course Deals"`
- **Iteration Field**: 1-week cycles (Monday 00:00 to Sunday 23:59).
- **Weekly Capacity**: **15 – 20 Story Points** (solo dev + multi-agent parallel execution).
- **Labeling Conventions**:
  - `type: feat` | `type: fix` | `type: chore` | `type: docs`
  - `priority: P0` (Blocker) | `priority: P1` (This Sprint) | `priority: P2` (Next Sprint) | `priority: P3` (Backlog)
  - `area: android` | `area: kmp` | `area: desktop`
  - `size: 1` (~1-2h), `size: 2` (~half day), `size: 3` (~1 day), `size: 5` (~2-3 days), `size: 8` (split required).

---

## 3. Multiplatform Definition of Done (DoD)

A client ticket is considered **Done** when:
1. **Contract Locked**: OpenAPI spec synced and Ktor API services/DTOs generated (`./gradlew generateCourseDealsApi`).
2. **Clean Architecture & Platform Safety**:
   - Presentation layer (`ViewModel` + `StateFlow` in `commonMain`) decoupled from network/DB layers.
   - Domain layer handles business logic cleanly.
   - Android Intent filters pass security audit (`intent-security-review`) and iOS/watchOS bridges follow Keychain/concurrency rules (`swift-security`).
3. **Automated Quality Checks**:
   ```bash
   ./gradlew detekt :app:lintLocalDebug testLocalDebugUnitTest :shared:linkDebugFrameworkIosSimulatorArm64
   ```
   All tests pass with zero detekt violations.
4. **Live Cross-Platform Verification & Evidence**:
   - Verified via Maestro E2E flows across Android (`emulator-5554`), iOS (`iPhone Simulator`), and Desktop (`:desktopApp:runMaestroFlow`) against local backend (`localhost:8081`).
   - Every captured screenshot visually inspected via `read_file` and uploaded to Cloudflare R2 in a side-by-side comparison table.
5. **Release PR & Resource Cleanup**:
   - Pull Request opened with mirrored issue labels and `Closes #<N>`.
   - Host RAM/CPU reclaimed via `release-resource` (`./gradlew --stop`).
   - CI build green and squash-merged to `main`.
