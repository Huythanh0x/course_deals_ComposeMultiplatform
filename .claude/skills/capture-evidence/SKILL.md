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
3. **Upload the screenshots actually worth keeping to hosted storage and record the
   URLs.** This repo does not commit screenshot binaries into git history — pick the
   1-3 that actually demonstrate the fix (e.g. a before/after pair, or a single
   after-shot for a new feature), not every intermediate capture taken while
   navigating to the right screen, and upload each to the user's Cloudflare R2 Worker.

   Check the precondition before attempting anything:
   ```bash
   if [ -z "$R2_IMAGE_UPLOAD_USER" ] || [ -z "$R2_IMAGE_UPLOAD_TOKEN" ]; then
     echo "R2_IMAGE_UPLOAD_USER / R2_IMAGE_UPLOAD_TOKEN are not set." >&2
     echo "Set both in your shell profile (e.g. ~/.zshrc), open a new shell, and re-run this step." >&2
     exit 1
   fi
   ```
   Don't ask the user for the secret value in chat — direct them to set it themselves.
   **Never** add `-v`/`-vvv` to the curl call below, never wrap it in `set -x`, and
   never print `$R2_IMAGE_UPLOAD_USER`, `$R2_IMAGE_UPLOAD_TOKEN`, or the resolved `-u`
   value anywhere — not in terminal output, not in `NOTES.md`, not in the PR body. The
   response body itself is safe to print (it's the Worker's reply, not the request's
   credentials).

   For each screenshot:
   ```bash
   worker_base="https://r2-image-worker.huythanh0x.workers.dev"
   response="$(curl -sS -X PUT "${worker_base}/upload" \
     -u "$R2_IMAGE_UPLOAD_USER:$R2_IMAGE_UPLOAD_TOKEN" \
     -F "image=@.claude/tickets/<issue-number>-<slug>/evidence/<name>.png")"

   url=""
   if command -v jq >/dev/null 2>&1; then
     url="$(printf '%s' "$response" | jq -r '.url // .imageUrl // .data.url // empty' 2>/dev/null)"
   fi
   if [ -z "$url" ]; then
     trimmed="$(printf '%s' "$response" | tr -d '[:space:]')"
     case "$trimmed" in
       http*) url="$trimmed" ;;
       "") ;; # empty response — fall through to the failure check below
       *) url="${worker_base}/${trimmed}" ;;  # confirmed: PUT /upload returns a bare stored filename, e.g. "2e87f527...366d3.png"
     esac
   fi
   if [ -z "$url" ]; then
     echo "Could not extract a hosted URL from the R2 worker response. Raw response:" >&2
     printf '%s\n' "$response" >&2
     exit 1
   fi

   status="$(curl -sS -o /dev/null -w '%{http_code}' "$url")"
   if [ "$status" != "200" ]; then
     echo "Uploaded but the resulting URL didn't return 200 (got $status): $url" >&2
     exit 1
   fi
   ```
   Confirmed against the real worker (2026-08-31, during #26's evidence capture): a
   successful `PUT /upload` returns the bare stored filename as plain text (e.g.
   `2e87f5278aef787753a2b58a570760c810a69c0b47091be703ff9ab9264366d3.png`) — not JSON,
   not a full URL. The JSON-field and `http*`-prefix checks are kept as a defensive
   first pass in case the worker's response ever changes shape, but the real path today
   is the bare-filename fallback, which builds the public URL by prefixing
   `$worker_base`. Always verify the constructed URL actually resolves (the `curl -w
   '%{http_code}'` check above) before trusting it — don't embed an unverified URL in a
   PR body.
4. Append each resulting URL into the workspace's Screenshots section as a ready
   markdown image line:
   ```markdown
   ### Screenshots
   ![before](<hosted-url-1>)
   ![after](<hosted-url-2>)
   ```
5. **If device automation isn't available**: say so explicitly in the workspace's Test
   evidence → E2E / manual verification section — don't claim verification that didn't
   happen. Ask the user to run the app themselves (`./gradlew installLocalDebug` +
   manual check) and either confirm it works or share a screenshot.
6. For a non-UI change, state plainly that screenshot evidence doesn't apply rather
   than leaving the section ambiguously blank.
7. Append one line to Progress log: `<date> captured evidence — <what, or "asked user">`.

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

Report what evidence exists (workspace screenshot paths and the hosted URLs recorded in
`NOTES.md`'s Screenshots section) or the honest "not available, asked user" note —
`ship-pr` uses this to fill in the PR's "How this was tested" and "Screenshots" sections
for real.
