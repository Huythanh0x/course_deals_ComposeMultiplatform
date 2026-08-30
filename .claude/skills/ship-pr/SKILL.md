---
name: ship-pr
description: Push a committed branch and open a PR from this repo's template, linking Closes #N, following docs/pull-requests.md. Use when work on a branch is done and ready to open (or update) a pull request for this repo.
---

# Ship PR

Pushes a finished branch and opens a PR in the shape `docs/pull-requests.md` and
`.github/PULL_REQUEST_TEMPLATE.md` expect.

## Before opening

Run the self-review checklist from `docs/pull-requests.md` for real, not as a
rubber stamp:
- Read the full diff top to bottom as if it were someone else's PR
- Does every changed file actually belong in this PR?
- Any leftover debug code, commented-out blocks, stray `Log.d`/`println`?
- Do tests actually cover the new behavior (if tests were added)?
- Confirm the branch's commit messages follow Conventional Commits (the `commit-msg`
  hook should already enforce this, but check)

## Steps

1. **Pull screenshot URLs from the workspace, if any exist.** `capture-evidence`
   already uploaded the kept screenshots to hosted storage (the user's Cloudflare R2
   Worker) and recorded the resulting URLs as markdown image lines in
   `.claude/tickets/<issue-number>-<slug>/NOTES.md`'s Test evidence → Screenshots
   section. Read that section verbatim — don't re-upload, don't re-derive URLs, and
   don't commit any screenshot files to the branch (screenshot binaries are
   intentionally kept out of this repo's git history). If that section is empty or
   absent, leave the PR's Screenshots section as `N/A` — don't invent one.
2. Push the branch: `git push -u origin <branch>` (first push) or `git push` (updates).
3. Confirm which issue this closes — every PR must link one via `Closes #N`
   (`docs/pull-requests.md`).
4. Open the PR filled from `.github/PULL_REQUEST_TEMPLATE.md` — fill in **all** of it,
   including "How this was tested" with what was actually run (not "should work"). If
   this ticket has a workspace at `.claude/tickets/<issue-number>-<slug>/NOTES.md` (from
   `run-tests`/`capture-evidence`), pull the real pass/fail numbers from its Test
   evidence section rather than re-deriving them. For the Screenshots section, embed
   the hosted URLs read in step 1 (not a prose description of what they show) whenever
   that step found any:
   ```bash
   gh pr create -R Huythanh0x/course_deals_ComposeMultiplatform \
     --title "<type>(<scope>): <description>" \
     --body "$(cat <<'EOF'
   ## Summary
   ...

   ## Changes
   - ...

   ## Related issue
   Closes #<N>

   ## How this was tested
   - [ ] ...

   ## Screenshots (if UI-facing)
   ![before](<hosted-url-1>)
   ![after](<hosted-url-2>)

   ## Checklist
   - [ ] Self-reviewed the diff top to bottom
   - [ ] Commit message(s) follow Conventional Commits
   - [ ] Docs/ADR updated if this changes architecture or setup
   - [ ] CI green (detekt + build + tests)
   EOF
   )"
   ```
5. **Mirror the linked issue's labels onto the PR** — this repo's label taxonomy
   (`type`/`priority`/`area`/`size`, see `docs/project-management.md`) is otherwise only
   ever applied to issues, so a PR with no labels can't be filtered/skimmed on its own in
   the PR list. Pull the labels straight from the issue rather than re-deciding them:
   ```bash
   labels=$(gh issue view <N> -R Huythanh0x/course_deals_ComposeMultiplatform --json labels -q '.labels[].name')
   args=()
   while IFS= read -r l; do args+=(--add-label "$l"); done <<< "$labels"
   gh pr edit <PR-number> -R Huythanh0x/course_deals_ComposeMultiplatform "${args[@]}"
   ```
6. Report the PR URL back to the user.

## After CI runs

- Check status with `gh pr checks <N> -R Huythanh0x/course_deals_ComposeMultiplatform`.
- If CI fails, diagnose from the actual failure logs (`gh run view <run-id> --log-failed`)
  before proposing a fix — don't guess.
- Once CI is green and the user wants it landed, use the standalone `merge-pr` skill —
  merging is deliberately not auto-chained here (see that skill for why).
