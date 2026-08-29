---
name: new-ticket
description: Create a GitHub issue with this repo's label taxonomy and the matching branch, following docs/project-management.md and docs/branching.md. Use when the user wants to file a new issue/ticket/bug/chore for this repo, or asks to start work on something that doesn't have an issue yet.
---

# New ticket

Creates a GitHub issue in the right shape, then the branch to work on it — mirrors the
flow this repo's own docs prescribe.

## Steps

1. **Pick the type** from the user's request: `feature`, `bug`, `chore`, `docs`,
   `refactor`, `test`, or `design`. If unclear, ask.
2. **Write the issue body** using the shape from `.github/ISSUE_TEMPLATE/bug_report.md`
   or `.github/ISSUE_TEMPLATE/feature_request.md` (whichever fits) — keep the same
   sections even for other types (what/why, environment/area, definition of done).
3. **Pick labels**, exactly one from each category (see `docs/project-management.md`
   for the full taxonomy):
   - `type: <feature|bug|chore|docs|refactor|test|design>`
   - `priority: <P0|P1|P2|P3>` — ask if not obvious from context
   - `area: <android|backend|web|design|testing|ci|infra>` — usually `area: android`
     for this repo
   - `size: <1|2|3|5|8|13>` — if it feels like `13`, say so and suggest splitting
     before creating the issue (per `docs/project-management.md`'s sizing guidance)
4. **Create the issue**:
   ```bash
   gh issue create -R Huythanh0x/course_deals_ComposeMultiplatform \
     --title "<title>" \
     --label "type: <x>" --label "priority: <x>" --label "area: <x>" --label "size: <x>" \
     --body "<body>"
   ```
5. **Create the branch** off the latest `main`, per `docs/branching.md`'s naming
   convention `<type>/<issue-number>-<short-slug>`:
   ```bash
   git checkout main && git pull origin main
   git checkout -b <type>/<issue-number>-<short-slug>
   ```
6. Report the issue URL and branch name back to the user before starting any code
   changes.

## Notes

- Never branch off another feature branch — always from latest `main`
  (`docs/branching.md`).
- Confirm with the user before creating the issue if the title/labels involved any
  real judgment call (priority, size) rather than assuming.
