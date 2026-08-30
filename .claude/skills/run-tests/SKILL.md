---
name: run-tests
description: Run unit tests, detekt, and lint for a ticket's changed modules and record real results into its workspace. Use after implement-change, standalone or as part of ticket-flow.
---

# Run tests

1. Identify affected modules from the workspace's Code scan section (or from
   `git diff --stat` if resuming without a fresh scan).
2. Run, per `CLAUDE.md`'s Build & verify commands (task names are flavor-qualified —
   `:app:lintLocalDebug`, not `:app:lintDebug`):
   - `./gradlew test<Variant>UnitTest` for each affected module
   - `./gradlew detekt` (or `:app:detekt` — see Known gaps, it's only wired into `app`
     today, don't assume other modules get checked)
   - Relevant lint task(s)
3. Record real pass/fail counts and report paths (`**/build/reports/tests/**`,
   `**/build/reports/lint-results-*.html`) into Test evidence → Unit tests. Don't round
   up or assume — quote the actual numbers from the command output.
4. If a real failure surfaces: fix it and re-run, don't report a failure as if it were
   acceptable to carry forward. If a fix isn't straightforward, stop here and report the
   failure honestly rather than proceeding to the next step with red tests.
5. If the change needed test coverage that doesn't exist in this repo yet (true for
   almost anything outside `app`'s boilerplate — see Known gaps), don't silently skip
   it or silently bootstrap a testing stack mid-ticket either — note the gap plainly and
   point at the `testing-strategy` skill for that decision.
6. Append one line to Progress log: `<date> ran tests — <pass/fail summary>`.

## Output

Report the pass/fail summary — `capture-evidence` runs next, and shouldn't proceed past
a red test suite.
