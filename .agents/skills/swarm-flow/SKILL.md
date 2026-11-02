---
name: swarm-flow
description: >-
  Orchestrate multiple features/tickets concurrently (2 to 5 tickets in parallel) within a single
  conversation using isolated Git Worktrees, parallel engineering subagents (backend-engineer,
  kmp-engineer, apple-developer, desktop-engineer), and a centralized QA/Emulator semaphore to
  prevent port & device collisions. Use when the user asks to run multiple tickets in parallel
  or run swarm-flow.
---

# Swarm Flow (`swarm-flow`)

Hierarchical multi-ticket orchestrator designed to run **2 to 5 independent tickets simultaneously** within a single Antigravity conversation, multiplying delivery throughput while guaranteeing zero git conflicts and zero hardware/port collisions.

Target GitHub Repositories & Project (relative to workspace root):
- **Backend repo**: `./course_deals_backend` (`Huythanh0x/course_deals_backend`)
- **KMP / Multiplatform repo**: `./course_deals_ComposeMultiplatform` (`Huythanh0x/course_deals_ComposeMultiplatform`)
- **GitHub Project**: `"Course Deals"` (`PVT_kwHOBEROFc4BhjJ2`, Project #4)

---

## The Core Triad of Swarm Orchestration

```mermaid
flowchart TD
    User([User Prompt: swarm-flow on #155 & #156]) --> Master[Master Orchestrator Agent]

    subgraph Phase1 [Phase 1: Multi-Ticket Spec & Worktree Setup]
        Master --> W1[Git Worktree: .agents/worktrees/155-account-deletion]
        Master --> W2[Git Worktree: .agents/worktrees/156-remote-config]
    end

    subgraph Phase2 [Phase 2: Checkpoint 1 - Unified Plan Approval]
        W1 --> Gate[Checkpoint 1: Approve Specs for All Tickets]
        W2 --> Gate
    end

    subgraph Phase3 [Phase 3: Parallel Engineering Execution]
        Gate --> Sub1["Feature Team #155: backend-engineer + kmp-engineer + apple-developer + desktop-engineer"]
        Gate --> Sub2["Feature Team #156: backend-engineer + kmp-engineer + apple-developer + desktop-engineer"]
        Sub1 --> LocalCommit1[Passed detekt/xcodebuild/unit tests -> Local Commit]
        Sub2 --> LocalCommit2[Passed detekt/xcodebuild/unit tests -> Local Commit]
    end

    subgraph Phase4 [Phase 4: Central QA & Device Semaphore]
        LocalCommit1 --> Semaphore{Central Resource Lock}
        LocalCommit2 --> Semaphore
        Semaphore -->|Step A: Ticket 155| E2E_1[Port 8081 + Android, iOS, watchOS & Desktop E2E + Side-by-Side R2 Upload]
        E2E_1 -->|Step B: Ticket 156| E2E_2[Port 8081 + Android, iOS, watchOS & Desktop E2E + Side-by-Side R2 Upload]
    end

    subgraph Phase5 [Phase 5: Consolidated Pull Request Delivery]
        E2E_2 --> Ship[Open PR #1 & PR #2 with Mirrored Labels + release-resource]
        Ship --> Checkpoint2([Checkpoint 2: Unified Merge Gate])
    end
```

---

## 1. Safety Protocols & Invariants

1. **Worktree Isolation (Strict Invariant)**:
   * **Never** allow two concurrent feature tasks to execute in the primary repo working directory.
   * Every ticket runs in its own isolated worktree under `.agents/worktrees/<issue-number>-<slug>`:
     ```bash
     git -C <repo-path> worktree add .agents/worktrees/<issue>-<slug> -b <type>/<issue>-<slug> origin/main
     ```
2. **Module Orthogonality**:
   * Verify that the parallel tickets do not touch the exact same lines of common files (e.g. `AppNavigation.kt` or `libs.versions.toml`). If overlapping changes exist, specify clear non-conflicting sections.
3. **Hardware / Port Semaphore (Sequential Execution for Live Testing)**:
   * **Unit Tests, Detekt & Framework Link**: Run **in parallel** inside each isolated worktree.
   * **Live Port 8081, Android `emulator-5554`, iOS/watchOS Simulator & Desktop E2E**: Managed strictly **sequentially** by the Master Orchestrator. When Ticket 1 finishes its live E2E check, Ticket 2 gains the lock.

---

## 2. Phase-by-Phase Execution Protocol

### Phase 1: Swarm Initialization & Worktree Setup
For each ticket specified in the batch (e.g., `#155` and `#156`):
1. Read ticket requirements from GitHub issue via `gh issue view`.
2. Create dedicated worktrees in the target repo(s):
   ```bash
   git -C ./course_deals_ComposeMultiplatform worktree add .agents/worktrees/<issue>-<slug> -b feat/<issue>-<slug> origin/main
   ```
3. Initialize `.agents/tickets/<issue>-<slug>/NOTES.md` and `STATE.json` inside each worktree.

### Phase 2: Checkpoint 1 (Unified Batch Spec Approval)
Present a consolidated **Swarm Execution Brief** to the user:
- **Tickets in Flight**: List of ticket IDs and titles.
- **Assigned Worktrees & Agent Roles**: File system paths and which engineers (`backend-engineer`, `kmp-engineer`, `apple-developer`, `desktop-engineer`) are assigned per ticket.
- **Architectural Summary**: Bullet summary per ticket.
- Call `ask_question`:
  - **(Recommended) Approve swarm plan — launch parallel engineering**
  - **Adjust specific ticket requirements**

### Phase 3: Parallel Subagent Execution
Once approved:
1. Define specialized engineer subagents (`backend-engineer`, `kmp-engineer`, `apple-developer`, `desktop-engineer` per `.agents/skills/team-flow/references/*.md`) pointing to their respective worktree paths.
2. Invoke the subagents **in the same turn** using `invoke_subagent`.
3. Subagents execute independently:
   - Implement source code & unit tests.
   - Run `./gradlew detekt`, unit tests, `:shared:linkDebugFrameworkIosSimulatorArm64`, and `xcodebuild` (if Apple files modified) inside their worktree.
   - Commit locally to the ticket branch.
   - Report commit SHA and test counts back to the Master Orchestrator.

### Phase 4: Central QA Semaphore (Sequential Cross-Platform Live Verification)
Once engineer commits are reported:
1. Master Orchestrator locks the test environment.
2. For Ticket 1:
   - Run backend check / bootRun if backend was modified.
   - Build & verify across **Android (`emulator-5554`), iOS (`iPhone Simulator`), Desktop (`:desktopApp`)**, and **watchOS (`Apple Watch Simulator`)** when applicable.
   - Run automated Maestro GUI test flows across platforms.
   - Capture step-by-step screenshots, visually inspect via `read_file`, upload to Cloudflare R2, and compile the side-by-side comparison matrix.
3. Release lock and repeat for Ticket 2.

### Phase 5: Consolidated Delivery, Resource Cleanup & Checkpoint 2
1. Push branches and create GitHub Pull Requests with linked issues (`Closes #<issue>`).
2. Prune completed worktrees:
   ```bash
   git -C <repo-path> worktree remove .agents/worktrees/<issue>-<slug>
   ```
3. Execute `release-resource` to stop Gradle/Kotlin daemons and reclaim host RAM/CPU.
4. Present the **Swarm Delivery Report** with links to all opened PRs.
5. Call `ask_question` for the merge decision.
