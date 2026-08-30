---
name: capture-evidence
description: Capture e2e/manual verification evidence and screenshots for a ticket, honestly reflecting what automated verification was actually possible in this environment. Use after run-tests, standalone or as part of ticket-flow, especially for UI-facing changes.
---

# Capture evidence

1. Check whether device automation is actually available: `which adb` and
   `adb devices` for a connected/booted emulator.
2. **If available**: install the debug build, launch the relevant screen, capture
   `adb exec-out screencap -p > .claude/tickets/<issue-number>-<slug>/evidence/<name>.png`
   for real — actually run this, don't just describe what a screenshot would show. Save
   every raw capture under the ticket workspace directory first (gitignored working
   memory, cheap to take several).
3. **Promote the screenshots that are actually worth keeping** into a repo-tracked
   location `ship-pr` can commit and embed in the PR body:
   ```bash
   mkdir -p docs/evidence/<issue-number>
   cp .claude/tickets/<issue-number>-<slug>/evidence/<name>.png docs/evidence/<issue-number>/
   ```
   Pick the 1-3 screenshots that actually demonstrate the fix (e.g. a before/after pair,
   or a single after-shot for a new feature) — don't promote every intermediate capture
   taken while navigating to the right screen.
4. **If not available**: say so explicitly in the workspace's Test evidence → E2E /
   manual verification section — don't claim verification that didn't happen. Ask the
   user to run the app themselves (`./gradlew installLocalDebug` + manual check) and
   either confirm it works or share a screenshot.
5. For a non-UI change, state plainly that screenshot evidence doesn't apply rather
   than leaving the section ambiguously blank.
6. Append one line to Progress log: `<date> captured evidence — <what, or "asked user">`.

## Known device quirks (this AVD, re-verify if behavior seems to have changed)

- `adb shell monkey -p <pkg> -c android.intent.category.LAUNCHER 1` immediately after
  `adb shell am force-stop <pkg>` frequently no-ops on the first call — the command
  reports success but the app doesn't actually come to the foreground. Always screenshot
  after, and if it's still on the home/previous screen, just run the exact same `monkey`
  command again (no extra delay needed) rather than assuming the app crashed.
- Forcing orientation via `adb shell settings put system accelerometer_rotation 0` +
  `adb shell settings put system user_rotation <0|1|2|3>` sometimes doesn't visibly
  apply on the first attempt even though the setting reads back correctly immediately
  after being set (`dumpsys window | grep mCurrentRotation` still shows the old value).
  Re-run both `settings put` commands once more before concluding rotation is stuck.
- Screenshot coordinates from the `Read` tool are in the *displayed* (possibly
  downscaled) image size, not the device's real pixel size — the image result states
  the scale factor (e.g. "displayed at 900x2000, multiply by 1.20"); always convert
  before calling `adb shell input tap`.
- Reset whatever you forced (`accelerometer_rotation 1` for auto-rotate,
  `cmd uimode night no` or whatever the prior state was) once verification is done —
  don't leave the emulator in a forced state for the next ticket's testing.

## Output

Report what evidence exists (file paths, both the workspace originals and the promoted
`docs/evidence/<issue-number>/` copies) or the honest "not available, asked user" note —
`ship-pr` uses this to fill in the PR's "How this was tested" and "Screenshots" sections
for real.
