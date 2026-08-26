# Versioning & Changelog

## Semantic Versioning

Format: `MAJOR.MINOR.PATCH` (e.g. `1.4.2`)

| Segment | Bump when |
|---|---|
| **MAJOR** | a breaking change — anything marked `!` in a Conventional Commit (public API contract change, incompatible data migration, removed functionality) |
| **MINOR** | new backward-compatible functionality (`feat` commits) |
| **PATCH** | backward-compatible bug fix (`fix` commits) |

Before `1.0.0`: it's fine to stay in `0.x.y` while the app is genuinely pre-stable — a
personal project doesn't need to rush to `1.0.0` just to look mature. Move to `1.0.0` once
it's something you'd consider a real, stable release.

## Tagging a release

```bash
git tag -a v1.4.2 -m "v1.4.2"
git push origin v1.4.2
```

Tag on `main`, at the exact commit you're releasing — not on a feature branch.

## Changelog

Maintain [`CHANGELOG.md`](../CHANGELOG.md) at the repo root, following the
[Keep a Changelog](https://keepachangelog.com) format:

```markdown
# Changelog

## [Unreleased]

## [1.4.2] - 2026-08-20
### Added
- Category filter on the course list (#42)

### Fixed
- Null category no longer crashes the detail screen (#51)
```

Group entries under `Added`, `Changed`, `Fixed`, `Removed` (skip empty groups). Reference
the issue number so anyone (including future you) can jump straight to the full context.

## Automating it

Because commits already follow Conventional Commits (`commit-conventions.md`), most of
the changelog can be generated instead of hand-written:

- `standard-version` or `semantic-release` can read commit history since the last tag,
  determine the next version number automatically (feat → minor, fix → patch, `!` →
  major), and generate the changelog section for you.
- Worth adopting once release cadence becomes frequent enough that hand-writing feels
  like real overhead — not necessary to set up on day one.

## Android versioning specifics

Android has two separate numbers, both set in `app/build.gradle`:
- `versionCode` — integer, must strictly increase for Play Store uploads (irrelevant to
  builds that never go through a store).
- `versionName` — the human-facing SemVer string, e.g. `"1.4.2"`.

Bump both together at release time; keep `versionName` matching the Git tag exactly so a
build can always be traced back to its source commit.
