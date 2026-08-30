---
name: capture-evidence
description: Capture e2e/manual verification evidence and screenshots for a ticket, honestly reflecting what automated verification was actually possible in this environment. Use after run-tests, standalone or as part of ticket-flow, especially for UI-facing changes.
---

# Capture evidence

1. Check whether device automation is actually available: `which adb` and
   `adb devices` for a connected/booted emulator. (As of the last check in this repo,
   `adb` was not on `PATH` in this environment — re-verify, don't assume that's still
   true.)
2. **If available**: install the debug build, launch the relevant screen, capture
   `adb exec-out screencap -p > .claude/tickets/<issue-number>-<slug>/evidence/<name>.png`.
   Save under the ticket workspace directory, not loose in the repo.
3. **If not available**: say so explicitly in the workspace's Test evidence → E2E /
   manual verification section — don't claim verification that didn't happen. Ask the
   user to run the app themselves (`./gradlew installLocalDebug` + manual check) and
   either confirm it works or share a screenshot.
4. For a non-UI change, state plainly that screenshot evidence doesn't apply rather
   than leaving the section ambiguously blank.
5. Append one line to Progress log: `<date> captured evidence — <what, or "asked user">`.

## Output

Report what evidence exists (file paths) or the honest "not available, asked user"
note — `ship-pr` uses this to fill in the PR's "How this was tested" section for real.
