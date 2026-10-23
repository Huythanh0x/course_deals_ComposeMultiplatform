---
name: read-requirements
description: Read a GitHub issue's requirements, differentiate Bug reproduction vs Feature RFC research, establish a concrete testable DoD matrix, and record into the ticket workspace.
---

# Read Requirements & Intake Analysis (Android / KMP)

1. Locate `.agents/tickets/<issue-number>-<slug>/NOTES.md`. If it doesn't exist yet,
   create it from the template in `ticket-flow` / `team-flow`.
2. `gh issue view <N> -R Huythanh0x/course_deals_ComposeMultiplatform` — copy the
   **verbatim** body and labels into the workspace's Requirements section.
3. Note labels explicitly (type/priority/area/size) — `size: 13` requires splitting first.

---

## Differentiated Intake Pipeline: Bug vs. Feature

Inspect the issue labels (`type: bug` vs `type: feat` / `type: chore`):

### A. If Bug (`type: bug`) — SWE-Bench Reproduction Protocol
1. **Mandatory Reproduction First**:
   - Before modifying any source code, reproduce the bug on `emulator-5554` or via a failing unit/instrumentation test.
   - Capture the failure state: exception stacktrace in `adb logcat`, or screenshot of corrupted UI state.
   - If the bug cannot be replicated, STOP. Report device/OS version discrepancy to the user rather than guessing a fix.
2. **Root Cause Analysis (RCA)**:
   - Identify the exact component: ViewModel state emission, Room query, Retrofit deserialization, or XML/Compose lifecycle flaw.
   - Post an RCA comment on the GitHub issue detailing:
     - Root cause explanation.
     - Reproduction steps and error logs / UI evidence.
     - Proposed fix approach.
     - Triage verdict: Code PR required, backend API fix required, or invalid/won't fix.

### B. If Feature (`type: feat` / `type: chore`) — Spec-Driven Development (SDD)
1. **Common Sense & Boundary Framing**:
   - Identify edge cases: empty list states, offline cache fallback, token expiration mid-flow, screen rotation / process death, permission denials.
2. **External / UX Industry Research**:
   - Perform web search for standard UX patterns in modern Android/Compose apps (e.g. material 3 design specs, optimistic UI updates, snackbar undo flows).
3. **Architecture Spike & Clean Architecture RFC**:
   - Document data/domain/presentation layer responsibilities.
   - Verify Retrofit DTO parity with backend REST contract.
   - Define immutable UI state (`StateFlow<UiState>`) and one-shot navigation events (`Channel`/`SharedFlow`).

---

## Establish Concrete Definition of Done (DoD) Matrix

Every issue must establish a clear, verifiable DoD matrix before implementation begins:

| DoD Criterion | Verification Method | Committed Artifact / Evidence |
|---|---|---|
| Empty State rendered | ADB UI test / Screenshot | R2 screenshot link: `empty_state.png` |
| Populated / Success State rendered | Emulator live test | R2 screenshot link: `populated_state.png` |
| Network Error / Retry State rendered | Offline toggle test | R2 screenshot link: `error_retry_state.png` |
| Zero detekt violations | Detekt runner | `./gradlew :app:detekt` 0 issues |
| Clean architecture isolation | Architecture check | Presentation does not import data/network entities |

Record this matrix directly in `.agents/tickets/<issue-number>-<slug>/NOTES.md`.
