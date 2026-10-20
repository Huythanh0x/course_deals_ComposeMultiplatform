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

## API versioning

See [ADR 0002](adr/0002-api-db-versioning-and-app-backend-compatibility.md) for the full
decision and rationale — this section is the practical policy day to day.

Backend API is versioned by URL path (`/api/v1/...`). Within one API major version,
**only additive, backward-compatible changes are allowed**: new optional fields/params,
new endpoints, new enum values. Never remove a field, rename a field, change a field's
type, or turn an optional field into a required one inside the same major version.

**Worked example:** `getProfile(tenantId)` shipped at app `1.1.0`. At app `1.2.0` we
need to add `userRole`.

- If `userRole` is **optional**, this needs nothing special. Ship it in `v1`. Gson
  (this repo's converter) ignores response fields it doesn't recognize, and simply
  never receives request fields an old client doesn't send. An app still on `1.1.0`
  keeps working against the updated backend with no code on either side treating
  "missing `userRole`" as an error — it's a permanently legitimate value, not a
  transient migration state.
- If `userRole` must become **required** for some real business rule, but old
  (`< 1.2.0`) app versions structurally can't send it, that's a breaking change to the
  `v1` contract. In order of preference:
  1. Don't make it required in `v1` at all — keep treating "absent" as a valid legacy
     case indefinitely.
  2. If it truly must be enforced: branch on the `X-App-Version` header (see below)
     server-side, applying the new requirement only to clients known to be capable of
     satisfying it, while older clients keep the old behavior. Label this branch as
     temporary in code and delete it once old-client usage is negligible.
  3. Last resort, for a genuinely incompatible redesign of that endpoint: give it a real
     `v2`. Don't reach for this before trying 1 or 2.

**Decision tree:** does the change add something new without altering what
already-shipped requests/responses mean? → additive, ship it in `v1`, no version gate
needed. Does it change or remove existing meaning, or make something newly mandatory
that old clients can't provide? → breaking; keep it optional server-side indefinitely
(preferred), gate it transitionally on `X-App-Version` (temporary, documented), or bump
that endpoint to `v2` (last resort).

**Deprecation window:** because this is a single first-party app whose release cadence
the team controls (not a public API with third-party consumers), a `v1` field or
endpoint stays supported until the app version(s) depending on the old shape are no
longer expected to be in active use — judged from real usage data once the
`X-App-Version` signal below provides it — rather than a fixed calendar SLA. If this API
ever gains third-party consumers, revisit this in a new ADR; that's a materially
different situation.

## Sending app version to the backend

Every backend request carries an `X-App-Version: <BuildConfig.VERSION_NAME>` header,
wired via a Hilt-qualified interceptor in `core:network`'s `CoreNetworkModule`.

This is a client-version **signal**, not an API version **selector** — it never changes
which API version is served. Its purposes:
- **Telemetry**: correlating backend traffic/errors to client versions, so we know when
  an old app version's usage has become negligible enough to retire an old contract
  shape.
- **Transitional conditional branching** (see the `getProfile`/`userRole` example
  above): the one case where the backend may act on this header today, and only as a
  temporary, documented branch — never as rejection.

**No backend-side minimum-version enforcement or rejection exists yet.** Building a
reject-old-clients gate before a real `v2` exists, or before there's any adoption data
from this signal, would be speculative complexity with nothing to act on. This is
explicitly out of scope until that changes.
