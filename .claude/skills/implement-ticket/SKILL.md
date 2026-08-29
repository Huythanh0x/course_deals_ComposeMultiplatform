---
name: implement-ticket
description: End-to-end implementation of an existing GitHub issue for this repo — read requirements, scan related code, confirm edge cases, implement, run tests, capture evidence, open the PR. Maintains a per-ticket workspace file tracking decisions and progress so work is resumable. Use when the user says "implement issue #N", "work on ticket #N", "pick up #N", or hands you an issue URL/number to build.
---

# Implement ticket

Full lifecycle for turning an existing issue into a merged PR. Composes the other
project skills rather than duplicating them (`new-ticket` for issue creation,
`ship-pr` for the PR step) — this skill is what runs *between* those two, plus the
verification/evidence work neither of them covers.

## Ticket workspace (memory + decision trail + progress log)

Every ticket gets one file: `.claude/tickets/<issue-number>-<slug>/NOTES.md`
(git-ignored — this is working memory, not project documentation; a real architectural
decision that comes out of it still gets promoted to a proper ADR via the `adr` skill).

**Always read this file first if it already exists** — a previous session may have
gotten partway through; resume from where it left off instead of restarting analysis.
If it doesn't exist, create it from this template:

```markdown
# Issue #<N>: <title>

## Requirements
<verbatim issue body + labels, from `gh issue view <N>`>

## Code scan
<files/modules found relevant, one line each, with why>

## Clarifications
- Q: <edge case / ambiguity found while reading requirements or code>
  A: <user's answer, or "assumed X because Y" if it was a reasonable default>

## Decisions
<implementation choices with real tradeoffs and why — not a restatement of the diff>

## Progress log
- <timestamp> <what was just done>

## Test evidence
### Unit tests
<command run, pass/fail summary, report path>
### E2E / manual verification
<what was actually run, or explicitly "not run automatically — see note below">
### Screenshots
<path or "N/A — see note below">

## PR
<link once opened>
```

Append to **Progress log** after every meaningful step (a timestamp via `date` +
one line — this is the resumability mechanism, keep entries terse). Append to
**Clarifications** and **Decisions** as they happen, not retroactively reconstructed
at the end.

## Steps

1. **Read requirements**: `gh issue view <N> -R Huythanh0x/course_deals_ComposeMultiplatform`.
   Copy the verbatim body + labels into the workspace file's Requirements section — don't
   paraphrase away detail you'll need later.

2. **Scan related code**: find the modules/files this issue actually touches before
   writing anything. Use `CLAUDE.md`'s module map to narrow scope fast, an Explore
   agent for anything broader. Record what was found (not the full exploration
   transcript — just the files that matter and why) in Code scan.

3. **Confirm requirements & edge cases**: read the requirements and code scan together
   and identify what's actually ambiguous — missing acceptance criteria, unstated
   behavior for an edge case, a choice between valid approaches. Use `AskUserQuestion`
   for genuine decision points; don't ask about things a reasonable default clearly
   covers (record the default and why instead). Log every Q&A in Clarifications as it
   happens.

4. **Branch**: if the branch for this issue doesn't exist yet, create it per
   `docs/branching.md` (`<type>/<issue-number>-<slug>` off latest `main` — same
   convention `new-ticket` uses). If resuming, `git status`/`git log` to see what's
   already committed before assuming a clean start.

5. **Plan for non-trivial work**: if the implementation touches multiple files or has
   more than one reasonable approach, this is exactly what `EnterPlanMode` is for —
   use it, and record the agreed approach in Decisions once out of plan mode.

6. **Implement**, following `CLAUDE.md`'s conventions (convention plugins for any new
   module — see `new-module`; the `MutableStateFlow`/`asStateFlow()` state pattern;
   Intent/exported-component care if touching manifest components — see
   `intent-security-review`). Log meaningful steps to Progress log as you go, not just
   at the end.

7. **Run tests** and record real results, not assumptions:
   - Unit tests: `./gradlew test<Variant>UnitTest` for the affected module(s) —
     capture the pass/fail summary and report path (`**/build/reports/tests/**`) in
     Test evidence.
   - Detekt/lint: per `CLAUDE.md`'s Build & verify commands.
   - If the change needs test coverage that doesn't exist yet in this repo (true for
     almost everything — see `CLAUDE.md`'s Known gaps), don't silently skip it or
     silently add ad-hoc test infra either: note the gap and point at the
     `testing-strategy` skill rather than making a unilateral testing-stack choice
     mid-ticket.

8. **E2E / manual verification and screenshots** — be honest about what's actually
   possible in the current environment:
   - This repo has no Espresso/UI test coverage beyond Android Studio's boilerplate
     (see Known gaps), so "e2e test" evidence today usually means manually running the
     app, not an automated suite.
   - Screenshot/manual verification needs an Android emulator or connected device with
     `adb` on `PATH`. If one is available, drive it via `adb` (install the debug APK,
     launch the relevant activity, `adb exec-out screencap -p > evidence.png`) and save
     the result under the ticket workspace directory.
   - If no device/emulator/`adb` is available in the current environment (this is the
     common case — check with `which adb` before assuming), **say so explicitly** in
     Test evidence rather than claiming verification that didn't happen, and ask the
     user to run the app and confirm, or share a screenshot themselves.

9. **Ship the PR**: hand off to the `ship-pr` skill. Fill its "How this was tested"
   section from this ticket's actual Test evidence section — real commands and results,
   not "should work". Link the screenshot(s) if the change is UI-facing and one exists.

10. **After merge**: the ticket workspace has done its job — it can be deleted. If
    anything in Decisions represents a real, reusable architectural choice (not just
    "how I implemented this one issue"), promote it to a proper ADR via the `adr` skill
    before deleting the workspace notes.
