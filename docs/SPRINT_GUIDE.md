# Course Deals - 1-Week Sprint Lifecycle Guide (Android / KMP)

This guide documents the weekly engineering sprint cadence for Course Deals Compose Multiplatform / Android development, optimized for a solo full-stack developer paired with Antigravity multi-agent orchestration.

---

## 1. Cadence & Rhythm

| Window | Focus | Key Actions |
|---|---|---|
| **Sunday (Stop & Plan)** | **Retro & Planning** | • Run `sprint-flow retro` to tally completed story points & UI/tech debt.<br>• Run `sprint-flow plan` to select backlog items and set Iteration scope on GitHub Project.<br>• Review cross-repo REST / DTO contracts against Retrofit client models. |
| **Mon - Fri (Build & Test)** | **Execution (`team-flow`)** | • Pick highest-priority issue from GitHub Sprint iteration.<br>• Run `team-flow` to implement Clean Architecture layers (data/domain/presentation).<br>• Automated static analysis & unit testing. |
| **Saturday (Release & Hardening)** | **Verification & Staging** | • Full live emulator E2E smoke tests on `emulator-5554`.<br>• Verify end-to-end integration against running local backend.<br>• Upload verification screenshots to Cloudflare R2.<br>• Cut alpha/beta internal test release build. |

---

## 2. GitHub Projects Integration

- **Board Name**: `"Course Deals"`
- **Iteration Field**: 1-week cycles (Monday 00:00 to Sunday 23:59).
- **Weekly Capacity**: **15 – 20 Story Points** (solo dev + multi-agent parallel execution).
- **Labeling Conventions**:
  - `type: feat` | `type: fix` | `type: chore` | `type: docs`
  - `priority: P0` (Blocker) | `priority: P1` (This Sprint) | `priority: P2` (Next Sprint) | `priority: P3` (Backlog)
  - `area: android` | `area: kmp`
  - `size: 1` (~1-2h), `size: 2` (~half day), `size: 3` (~1 day), `size: 5` (~2-3 days), `size: 8` (split required).

---

## 3. Mobile / KMP Definition of Done (DoD)

An Android/KMP ticket is considered **Done** when:
1. **Contract Locked**: Retrofit API services and DTO models match the locked backend schema.
2. **Clean Architecture**:
   - Presentation layer (`ViewModel` + `StateFlow`) decoupled from network/DB layers.
   - Domain layer handles business logic cleanly.
   - Intent filters and exported activities pass security audit (`intent-security-review`).
3. **Automated Quality Checks**:
   ```bash
   ./gradlew :app:detekt testDebugUnitTest
   ```
   All tests pass with zero detekt violations.
4. **Live Verification & Evidence**:
   - App installed and launched on local `emulator-5554` against local/staging backend.
   - Core user flow visually verified.
   - Screencaps / screenshots uploaded to Cloudflare R2 and linked in the PR description.
5. **Release PR**:
   - Pull Request opened with labeled tags (`area: android`, linked issue `#N`).
   - CI build green.
   - Squash-merged to `main`.
