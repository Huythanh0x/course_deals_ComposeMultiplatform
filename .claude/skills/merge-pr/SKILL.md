---
name: merge-pr
description: Merge an open, CI-green PR for this repo — squash-merge with a rewritten Conventional Commits message, then confirm the branch auto-deleted and the linked issue auto-closed. Use when a PR is ready to land, not automatically as part of ship-pr.
---

# Merge PR

`ship-pr` opens a PR and stops there deliberately — merging to `main` is a
shared-state, hard-to-reverse action, so it's always its own explicit step, never
auto-chained after opening a PR or after CI merely starts running. Only invoke this
when the user actually wants a specific PR merged now.

## Steps

1. **Check CI is actually green**, don't assume:
   ```bash
   gh pr checks <N> -R Huythanh0x/course_deals_ComposeMultiplatform
   ```
   If anything is failing or still running, stop here. For a failure, diagnose from the
   real logs before proposing a fix:
   ```bash
   gh run view <run-id> --log-failed -R Huythanh0x/course_deals_ComposeMultiplatform
   ```
   Don't merge on red or pending CI.

2. **Read the PR** (`gh pr view <N> -R Huythanh0x/course_deals_ComposeMultiplatform`) and
   its commits to compose a clean squash commit message. GitHub's default squash message
   concatenates every branch commit — that's not what should land on `main` (see
   `docs/pull-requests.md`'s "Why squash by default"). Write one Conventional Commits
   line (often the PR's own title, or the best individual commit message on the branch,
   cleaned up), plus a short body if the PR's summary adds real context.

3. **Merge**:
   ```bash
   gh pr merge <N> -R Huythanh0x/course_deals_ComposeMultiplatform \
     --squash \
     --subject "<type>(<scope>): <description>" \
     --body "<optional body — omit for a single-line commit>"
   ```

4. **Confirm, don't assume**:
   ```bash
   gh pr view <N> -R Huythanh0x/course_deals_ComposeMultiplatform --json state,mergedAt,mergeCommit
   gh issue view <linked-issue-number> -R Huythanh0x/course_deals_ComposeMultiplatform --json state,stateReason
   ```
   The branch should auto-delete (repo setting) and the linked issue should auto-close
   via the PR's `Closes #N` — verify both rather than trusting it happened silently,
   especially since a squash-merge sometimes needs the `Closes #N` line to have survived
   into the final squash commit message to trigger auto-close correctly.

5. **Local cleanup**, if the merged branch has a local worktree or branch:
   ```bash
   git worktree remove <path>       # if it was worked in a worktree
   git branch -d <branch-name>      # if a plain local branch (not from a worktree)
   git fetch --prune                # drop the now-deleted remote-tracking ref
   ```

6. Report the merge commit SHA and the two confirmations (branch deleted, issue closed)
   back to the user.

## Notes

- This is genuinely a step that needs the user's go-ahead per-PR (see the repo's
  broader safety conventions around pushing/merging shared state) — don't chain
  multiple merges in a row without confirming each one first if there's any real
  uncertainty about ordering (e.g. two PRs that touch overlapping files, where merge
  order determines who resolves the conflict).
- If two open PRs conflict on the same file (e.g. a shared base-class method both
  touch), decide and state the merge order rather than merging whichever was asked
  about first without checking — `gh pr diff <N>` on both is cheap insurance.
