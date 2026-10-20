# Pull Requests & Merge Strategy

## Why open a PR at all, solo

Pushing straight to `main` skips two things you actually want even alone: a CI run gated
on the change *before* it lands, and a written record of *why*, sitting next to the diff,
that you can find later. A PR is cheap to open and gives you both for free.

## PR conventions

- **Title** matches the commit convention: `feat(course): add category filter to course list`
- **Always link the issue** — include `Closes #42` in the description so merging
  auto-closes the issue and the board updates itself.
- **Use `.github/PULL_REQUEST_TEMPLATE.md`** — it pre-fills automatically when you open a
  PR on GitHub. Fill in all of it, don't leave the testing/screenshots sections blank out
  of habit.
- **Draft PRs are fine** for work in progress that you want visible on the board before
  it's ready for merge — open it as draft, mark "Ready for review" when it's actually done.

## Self-review checklist

This is the solo-dev substitute for a second pair of eyes — do it deliberately as its own
step, not as a glance while writing the description:

- [ ] Read the full diff top to bottom as if it were someone else's PR
- [ ] Does every changed file actually belong in this PR, or did something unrelated
  sneak in?
- [ ] Any leftover debug code, commented-out blocks, or stray `Log.d`/`println` calls?
- [ ] Do the tests actually cover the new behavior, or just re-confirm what already worked?
- [ ] Does the PR description's "how tested" section describe something you *actually
  did* (device/emulator, which flavor) — not just "should work"?

## CI gate

CI (detekt + build + unit tests, minimum) must pass before merge — configure this as a
required status check in the repo's branch protection rules for `main`, so it's enforced
structurally, not just a personal habit you might skip when tired. See `ci-cd.md` for the
pipeline itself.

## Merge strategy

Three options GitHub offers, and when each one actually makes sense:

| Strategy | What it does | Use when |
|---|---|---|
| **Squash and merge** | Collapses all commits on the branch into one commit on `main` | **Default. Use this for essentially every PR.** |
| Merge commit | Keeps every branch commit, plus a merge commit tying them together | Rare — only if individual commit history on a long-lived branch genuinely matters (uncommon solo) |
| Rebase and merge | Replays branch commits onto `main` individually, no merge commit | Avoid as the actual PR-landing strategy — squash instead |

### Updating a branch against `main` mid-work

This is a separate question from the landing strategy above — it's about keeping an
**open, unmerged** branch current while `main` moves ahead under it (e.g. a PR sitting
in review while other work merges). Default to `git merge origin/main`, not
`git rebase origin/main`:

- A merge needs a normal `git push` — no `--force`/`--force-with-lease` ever required.
- A rebase rewrites the branch's commits, which means the remote copy has to be
  force-pushed to match — safe enough with `--force-with-lease` on a solo,
  unmerged branch, but it's friction and risk this repo's squash-merge default makes
  entirely unnecessary: the merge commit a `git merge` leaves behind disappears anyway
  once the PR is squash-merged, so there's no actual history cost to avoiding rebase
  here.
- Reach for a local rebase only when you deliberately want to clean up or reorder the
  branch's *own* commits before opening the PR — a different goal than "just catch up
  to `main`."

### Why squash by default

- Your branch's real-time commits ("wip", "fix typo", "actually fix it this time") never
  pollute `main` — they collapse into one commit representing the finished PR.
- `main`'s history ends up as one entry per shipped unit of work — exactly what you want
  skimming `git log`, writing a changelog, or explaining what happened in a given week.
- **Edit the squash commit message at merge time** — GitHub defaults to concatenating
  every branch commit's message, which is *not* what you want. Rewrite it to a single
  clean Conventional Commits line (often your best individual commit message from the
  branch, cleaned up) before confirming the merge.

### After merging

- Delete the branch (turn on GitHub's auto-delete-on-merge repo setting so this isn't a
  manual step you can forget).
- Confirm the linked issue actually closed and the board column updated — usually
  automatic via `Closes #42`, but worth a glance, especially across the multi-repo board.
