---
name: implement-change
description: Implement the code change for a ticket, following this repo's AGENTS.md conventions, once requirements/code-scan/clarifications are already settled. Use after confirm-edge-cases, standalone or as part of ticket-flow.
---

# Implement change

1. Read the workspace's Requirements, Code scan, Clarifications, and Decisions
   sections first — implement from those, not from re-deriving requirements or
   re-litigating an already-answered edge case.
2. If the branch for this issue doesn't exist yet, create it per `docs/branching.md`
   (or use the `new-ticket` skill if starting completely fresh). If resuming, check
   `git status`/`git log` before assuming a clean start.
3. For anything touching more than 2-3 files or with a real architectural choice, formulate
   an explicit plan (or recommend `/plan`) before writing code, even inside this flow — a
   checkpoint on *approach* is different from this skill's own step-level gate, and still
   worth it for a non-trivial change.
4. Implement, following `AGENTS.md`'s conventions and `.agents/skills/team-flow/references/upstream/`:
   - Convention plugins for any new module (`coursedeals.kmp.library` via `new-module` skill)
   - `./gradlew generateCourseDealsApi` if backend OpenAPI schema changed
   - `MutableStateFlow`/`asStateFlow()` + `_uiState.update { }` for ViewModel state in `commonMain`
   - Semantic `Modifier.testTag("<tag>")` and `.accessibilityIdentifier("<tag>")` for Maestro/Compose UI testability
   - Intent/exported-component care on Android (`intent-security-review` skill) and Keychain/Passkey safety on Apple (`swift-security`)
   - Fakes-over-mocks + Ktor `MockEngine` for tests (see `testing-strategy`)
5. Record any real implementation-level tradeoff in Decisions — not a restatement of
   the diff, but *why* one approach was chosen over another when it mattered.
6. Append to Progress log after each meaningful chunk of work, not only once at the
   end — this is what makes an interrupted implementation resumable.

## Output

Report the files changed and why (a short list, not the full diff) — `run-tests` picks
up from here.
