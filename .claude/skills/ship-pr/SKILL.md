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

1. Push the branch: `git push -u origin <branch>` (first push) or `git push` (updates).
2. Confirm which issue this closes — every PR must link one via `Closes #N`
   (`docs/pull-requests.md`).
3. Open the PR filled from `.github/PULL_REQUEST_TEMPLATE.md` — fill in **all** of it,
   including "How this was tested" with what was actually run (not "should work"). If
   this ticket has a workspace at `.claude/tickets/<issue-number>-<slug>/NOTES.md` (from
   `run-tests`/`capture-evidence`), pull the real pass/fail numbers and evidence
   paths/screenshots from its Test evidence section rather than re-deriving them:
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


   ## Checklist
   - [ ] Self-reviewed the diff top to bottom
   - [ ] Commit message(s) follow Conventional Commits
   - [ ] Docs/ADR updated if this changes architecture or setup
   - [ ] CI green (detekt + build + tests)
   EOF
   )"
   ```
4. Report the PR URL back to the user.

## After CI runs

- Check status with `gh pr checks <N> -R Huythanh0x/course_deals_ComposeMultiplatform`.
- If CI fails, diagnose from the actual failure logs (`gh run view <run-id> --log-failed`)
  before proposing a fix — don't guess.
- Default landing strategy is **squash-merge** (`docs/pull-requests.md`) — the squash
  commit message should be rewritten to a single clean Conventional Commits line at
  merge time, not GitHub's default concatenation of every branch commit.
- After merge, the branch is expected to auto-delete (repo setting) and the linked
  issue to auto-close — confirm both rather than assuming.
