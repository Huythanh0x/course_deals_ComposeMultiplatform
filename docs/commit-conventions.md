# Commit Conventions — Conventional Commits

## Format

```
<type>(<scope>): <short description>

<optional longer body>

<optional footer>
```

## Types

| Type | Use for |
|---|---|
| `feat` | a new feature |
| `fix` | a bug fix |
| `docs` | documentation only (README, ADRs, comments-as-docs) |
| `style` | formatting only, no logic change (whitespace, semicolons) |
| `refactor` | code change that's neither a fix nor a feature — same behavior, different structure |
| `perf` | a change that specifically improves performance |
| `test` | adding or correcting tests, no production code change |
| `build` | build system or dependency changes (Gradle, version catalog) |
| `ci` | CI configuration/scripts (GitHub Actions workflows) |
| `chore` | everything else maintenance-shaped that doesn't fit above |
| `revert` | reverting a previous commit |

## Scope

Free text, kept short — the module or area affected. For this repo that's usually one of
the Gradle module names or a cross-cutting concern: `app`, `auth`, `home`, `course`,
`detail`, `enroll`, `profile`, `splash`, `network`, `ui`, `common`, `ci`. Omit the scope
entirely if the change is broad enough that no single scope fits (`chore: update all
dependencies`).

## Examples

```
feat(course): add category filter to course list
fix(detail): handle null category without crashing
docs(adr): record decision to use Compose Multiplatform for web
chore(deps): bump Hilt to 2.52
refactor(course): extract paging source from repository
test(auth): add coverage for expired token refresh
ci(actions): cache gradle dependencies between runs
```

## Breaking changes

Mark a breaking change with `!` right after the type/scope, and explain it in the footer:

```
feat(network)!: change ApiResult error shape to sealed class hierarchy

BREAKING CHANGE: callers pattern-matching on the old ApiError enum must migrate to the
new sealed ApiResult.Error subtypes.
```

This matters specifically because it's what drives an automatic MAJOR version bump if
you're using changelog/version automation (see `versioning.md`) — don't skip the `!` just
because you're the only consumer of the code today.

## Formatting rules

- Subject line under ~72 characters.
- Subject line: imperative mood, lowercase after the colon, no trailing period —
  `fix(auth): correct token expiry check`, not `Fixed the token expiry check.`
- If you need to explain *why*, not just *what*, put it in the body (blank line after the
  subject, then free-form prose). The subject answers "what changed"; the body answers
  "why did this need to happen."
- One logical change per commit. A commit that touches five unrelated things should be
  five commits — this doesn't matter much on the branch itself (it'll get squashed), but
  it matters if you ever need to `git revert` or `git bisect` something specific.

## Why bother, if it's all getting squashed anyway

The *branch's* individual commits get squashed away at merge (see `pull-requests.md`),
but the final squashed commit on `main` should itself follow this format — that's the
commit that actually matters for `git log` readability and for auto-generating the
changelog. Following the convention on the branch too is just good practice for your own
sanity while working, and makes writing that final squashed message trivial (often it's
just your best individual commit message, cleaned up).
