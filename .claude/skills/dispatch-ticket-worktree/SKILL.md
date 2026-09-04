---
name: dispatch-ticket-worktree
description: Set up an isolated git worktree for one ticket and launch a background agent to implement it end-to-end (through a local commit), for running multiple independent tickets in parallel. Use when 2+ issues need implementing at the same time and don't touch overlapping files.
---

# Dispatch ticket to a worktree agent

Generalizes the pattern used to implement #38 and #39 in parallel. Each ticket gets its
own git worktree (shares this repo's `.git` history/objects, so commits are instantly
visible everywhere — much cheaper than a full duplicate clone) and its own background
agent that works unattended through implementation and local build verification, then
stops before anything that needs the shared emulator or touches GitHub.

**Only dispatch tickets that are genuinely independent** — different modules/files, no
shared base class or resource both would edit. If two candidate tickets touch the same
file, either sequence them instead of parallelizing, or accept that one PR will need to
resolve a conflict against the other later (state that plainly, as happened between #27
and #38).

## Preconditions

The issue and its branch must already exist (`new-ticket`, or `create-sub-issues` for a
child issue) before dispatching — this skill implements against an existing
`<type>/<issue-number>-<slug>` branch, it doesn't create the ticket itself.

## Steps

1. **Create the worktree**, one per ticket, off that ticket's own branch:
   ```bash
   ID=$(openssl rand -hex 8)
   git worktree add ".claude/worktrees/agent-$ID" <type>/<issue-number>-<slug>
   ```
2. **Compose a fully self-contained agent prompt** — the agent has no memory of this
   conversation and, per the Agent tool's own guidance, cannot see prior turns, so the
   prompt must carry everything it needs:
   - The exact worktree path, with an explicit instruction to run every command with
     that directory as cwd and never touch the main checkout or a sibling worktree.
   - A pointer to read `CLAUDE.md` in full first (module map, build/lint task naming,
     conventions, known gaps).
   - The issue's title, labels, and **verbatim** body (don't paraphrase — the agent
     can't re-fetch context it's missing).
   - The ticket workspace template (`.claude/tickets/<issue-number>-<slug>/NOTES.md`,
     same structure `ticket-flow` uses) inlined directly in the prompt — a background
     agent may not have the project skills registry available the way this session
     does, so don't assume it can invoke `read-requirements`/`scan-related-code`/etc.
     by name; give it the procedure inline instead.
   - **Two hard boundaries, stated explicitly and unambiguously:**
     1. *No interactive `AskUserQuestion`* — it's running unattended in the background.
        For any edge case, it must pick the most reasonable default consistent with
        existing code, record it in the workspace's Decisions section with its
        reasoning, and flag it in the final report for the user to override later.
     2. *No `adb`/emulator, no `git push`, no `gh pr create`* — there is exactly one
        shared emulator and (usually) a sibling agent building in parallel; on-device
        verification from two worktrees at once would corrupt each other's testing.
        Stop after a clean local `git commit` (Conventional Commits, `Closes #N`) and
        report back — capture-evidence and ship-pr happen afterward, driven by this
        session, one ticket at a time against the shared emulator.
   - What real build/lint/test commands to run (module-scoped, flavor-qualified where
     relevant per `CLAUDE.md`), and the instruction to actually fix and re-run on a
     real failure rather than reporting red as acceptable.
   - What the final report should contain: root cause / approach taken, files changed
     with one-line reasons, real build/test output, Decisions-section assumptions
     worth the user's override, and the commit hash with a clean `git status`.
3. **Launch one `Agent` call per ticket**, `subagent_type: general-purpose` (needs full
   tool access), in the **same message** when dispatching more than one so they run
   concurrently — the Agent tool description warns that firing them across separate
   messages loses the parallelism. Leave `isolation` unset — the worktree was already
   created manually in step 1; the Agent tool's own `isolation: "worktree"` would create
   a second, redundant one on a throwaway branch.
4. **After each agent reports back** (you'll get a task notification per agent, not a
   synchronous reply): read its report, then drive `capture-evidence` and `ship-pr`
   yourself, sequentially across tickets, against the one shared emulator. It's fine —
   and often more efficient — to start a finished ticket's on-device verification while
   a sibling ticket's agent is still implementing, as long as the still-running agent
   was actually instructed not to touch the emulator.
5. Leave the worktrees in place after shipping unless the user asks to clean them up —
   they cost only disk, and `merge-pr`'s cleanup step removes the ones that get merged.

## What this skill deliberately does not do

- It doesn't attempt real on-device parallelism. Two agents driving the same emulator
  at once via `adb install`/`adb shell input` would race and corrupt each other's
  results. Genuine parallel *device* testing would need a second AVD — worth
  considering only if this pattern becomes a frequent bottleneck, not set up
  speculatively here.
- It doesn't auto-merge or open PRs on the agents' behalf — that stays a
  user-in-the-loop step (`ship-pr`, then `merge-pr`) for the same reason those are
  already standalone, explicit steps elsewhere in this skill set.
