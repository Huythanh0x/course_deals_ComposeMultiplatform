---
name: create-sub-issues
description: Create one or more GitHub sub-issues under an existing parent issue (new or already-existing issues), link them via the Sub-issues API, and clean up GitHub's board auto-cascade. Use when the user wants to break a tracked issue into child tasks, or attach existing standalone issues as children of a parent.
---

# Create sub-issues

Pulled out of `new-ticket` into its own skill since it's invoked on its own at least as
often as from a fresh `new-ticket` flow — e.g. splitting an existing umbrella issue
after the fact, or attaching issues that already existed before the parent did (as
happened when #24 was created and #17/#18 were attached to it retroactively).

Sub-issues are **intentionally excluded from the "Course Deals" board** — only a
parent issue represents the work there (see `docs/project-board.md`). Every path below
ends with the same board-cascade check for that reason.

## Case A: creating brand-new sub-issues

```bash
gh issue create -R Huythanh0x/course_deals_ComposeMultiplatform \
  --title "<title>" \
  --label "type: <x>" --label "priority: <x>" --label "area: <x>" --label "size: <x>" \
  --parent <parent-issue-number> \
  --body "<body>"
```

Deliberately **omit `--project`** — adding a sub-issue to the board separately from
its parent defeats the point of grouping the work under one card.

## Case B: attaching an already-existing issue as a sub-issue

`gh issue create --parent` only works for issues you're creating fresh. For an issue
that already exists (e.g. #17 and #18 existed before parent #24 did), link it via the
Sub-issues REST API directly:

```bash
# Sub-issue id must be the issue's numeric *database* id, not its #number —
# resolve it first:
SUB_ID=$(gh api repos/Huythanh0x/course_deals_ComposeMultiplatform/issues/<child-number> -q '.id')

gh api repos/Huythanh0x/course_deals_ComposeMultiplatform/issues/<parent-number>/sub_issues \
  -X POST -f sub_issue_id="$SUB_ID"
```

Repeat per child issue. This does not touch labels or the board on its own — it only
establishes the parent/child relationship.

## Board cascade cleanup (run after either case)

GitHub auto-adds a sub-issue to the board the instant its parent is already a board
item, regardless of how the sub-issue was created or whether `--project` was passed —
this isn't something `--project`/auto-add settings can prevent, it's tied to the
parent-child relationship itself. Check for it and remove it if it happened:

```bash
NUM="$(gh project list --owner Huythanh0x --format json -q '.projects[] | select(.title=="Course Deals") | .number')"
ITEM_ID="$(gh api graphql -f query='
  query($num: Int!) { user(login: "Huythanh0x") { projectV2(number: $num) { items(first: 100) {
    nodes { id content { ... on Issue { number repository { name } } } } } } } }' \
  -F num="$NUM" \
  -q ".data.user.projectV2.items.nodes[] | select(.content.repository.name == \"course_deals_ComposeMultiplatform\" and .content.number == <child-issue-number>) | .id")"
[ -n "$ITEM_ID" ] && gh project item-delete "$NUM" --owner Huythanh0x --id "$ITEM_ID"
```

If nothing prints for `ITEM_ID`, the sub-issue never landed on the board — nothing to
clean up.

## Branch creation

Sub-issues get branches the same way any other issue does — see `new-ticket`'s branch
step (`<type>/<issue-number>-<slug>` off latest `main`). Not repeated here since it's
identical regardless of parent/child status.

## Notes

- `gh issue create --parent` and the Sub-issues REST API both require the `project`
  scope on the authenticated `gh` token (`gh auth refresh -s project` if missing).
- Ask the user before deciding an issue should be a sub-issue if it's ambiguous whether
  it's genuinely a child task of an existing parent versus its own top-level unit of
  work — this is a real judgment call, not a mechanical one.
