---
name: swarm-flow
description: >-
  Orchestrate multiple features/tickets concurrently (2 to 5 tickets in parallel) within a single
  conversation using isolated Git Worktrees, parallel engineering subagents, and a centralized
  QA/Emulator semaphore to prevent port & device collisions. Use when the user asks to run multiple
  tickets in parallel or run swarm-flow.
---

# Swarm Flow (`swarm-flow`)

Hierarchical multi-ticket orchestrator designed to run **2 to 5 independent tickets simultaneously** within a single Antigravity conversation, multiplying delivery throughput while guaranteeing zero git conflicts and zero hardware/port collisions.

Target GitHub Repositories & Project:
- **Backend repo**: `/Users/thanh0x/StudioProjects/course_deals_backend` (`Huythanh0x/course_deals_backend`)
- **Android / KMP repo**: `/Users/thanh0x/StudioProjects/course_deals_ComposeMultiplatform` (`Huythanh0x/course_deals_ComposeMultiplatform`)
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
        Gate --> Sub1[Feature Team #155: backend-engineer + android-engineer]
        Gate --> Sub2[Feature Team #156: backend-engineer + android-engineer]
        Sub1 --> LocalCommit1[Passed detekt/unit tests -> Local Commit]
        Sub2 --> LocalCommit2[Passed detekt/unit tests -> Local Commit]
    end

    subgraph Phase4 [Phase 4: Central QA & Emulator Semaphore]
        LocalCommit1 --> Semaphore{Central Resource Lock}
        LocalCommit2 --> Semaphore
        Semaphore -->|Step A: Ticket 155| E2E_1[Port 8081 + emulator-5554 Verification & R2 Upload]
        E2E_1 -->|Step B: Ticket 156| E2E_2[Port 8081 + emulator-5554 Verification & R2 Upload]
    end

    subgraph Phase5 [Phase 5: Consolidated Pull Request Delivery]
        E2E_2 --> Ship[Open PR #1 & PR #2 with Mirrored Labels]
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
   * **Unit Tests & Detekt**: Run **in parallel** inside each isolated worktree.
   * **Live Port 8081 & Emulator-5554**: Managed strictly **sequentially** by the Master Orchestrator. When Ticket 1 finishes its live E2E check, Ticket 2 gains the lock.

---

## 2. Phase-by-Phase Execution Protocol

### Phase 1: Swarm Initialization & Worktree Setup
For each ticket specified in the batch (e.g., `#155` and `#156`):
1. Read ticket requirements from GitHub issue via `gh issue view`.
2. Create dedicated worktrees in the target repo(s):
   ```bash
   git -C /Users/thanh0x/StudioProjects/CourseDeals/course_deals_ComposeMultiplatform worktree add .agents/worktrees/<issue>-<slug> -b feat/<issue>-<slug> origin/main
   ```
3. Initialize `.agents/tickets/<issue>-<slug>/NOTES.md` inside each worktree.

### Phase 2: Checkpoint 1 (Unified Batch Spec Approval)
Present a consolidated **Swarm Execution Brief** to the user:
- **Tickets in Flight**: List of ticket IDs and titles.
- **Assigned Worktrees**: File system paths.
- **Architectural Summary**: Bullet summary per ticket.
- Call `ask_question`:
  - **(Recommended) Approve swarm plan — launch parallel engineering**
  - **Adjust specific ticket requirements**

### Phase 3: Parallel Subagent Execution
Once approved:
1. Define specialized engineer subagents for each ticket pointing to their respective worktree paths.
2. Invoke the subagents **in the same turn** using `invoke_subagent`.
3. Subagents execute independently:
   - Implement source code & unit tests.
   - Run `./gradlew :app:detekt` and unit tests inside their worktree.
   - Commit locally to the ticket branch.
   - Report commit SHA and test counts back to the Master Orchestrator.

### Phase 4: Central QA Semaphore (Sequential Live Verification)
Once engineer commits are reported:
1. Master Orchestrator locks the test environment.
2. For Ticket 1:
   - Run backend check / bootRun if backend was modified.
   - Run `./gradlew installLocalDebug` on `emulator-5554`.
   - Capture UI state screenshots & upload to Cloudflare R2.
3. Release lock and repeat for Ticket 2.

### Phase 5: Consolidated Delivery & Checkpoint 2
1. Push branches and create GitHub Pull Requests with linked issues (`Closes #<issue>`).
2. Prune completed worktrees:
   ```bash
   git -C <repo-path> worktree remove .agents/worktrees/<issue>-<slug>
   ```
3. Present the **Swarm Delivery Report** with links to all opened PRs.
4. Call `ask_question` for the merge decision.
