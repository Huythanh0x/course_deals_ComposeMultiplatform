# 0002: API/DB versioning and app↔backend compatibility

## Status
Accepted

## Context

Neither this repo nor `course_deals_backend` had a written rule for API versioning, DB
versioning, or app↔backend compatibility, despite both already doing URL-path API
versioning (`/api/v1/...`) and the backend using Flyway for DB migrations. The backend
had no visibility into which app version was calling it: no header, no way to
correlate traffic to client versions, no foundation for a future compatibility gate.

App releases and backend deploys are not atomic — a user can keep an old app version
installed indefinitely, and the backend must keep serving it. This decision exists to
make that constraint explicit instead of discovering it the hard way the first time an
endpoint's contract needs to change.

This is a cross-repo decision. The backend repo has its own ADR 0002 covering its side
(the Flyway expand-contract rule for DB migrations, and the backend's own API
deprecation policy). Companion PR: huythanh0x/course_deals_backend#16 (docs only, no
code change there).

## Decision

**API versioning stays URL-path-based (`/api/v1/...`), unchanged.** This is already in
place, is the most widely adopted and simplest strategy (Stripe, GitHub, Google Cloud
all use it), and switching to header-based API versioning now would add
routing/testing complexity with no benefit while only `v1` exists.

**Within one API major version (`v1`), only additive, backward-compatible changes are
allowed:** new optional fields/params, new endpoints, new enum values. Never remove a
field, rename a field, change a field's type, or turn an optional field into a required
one inside the same `v1`. Concrete example: `getProfile(tenantId)` shipped at app
`1.1.0`; adding an optional `userRole` at app `1.2.0` is free — the Gson converter
already ignores fields it doesn't recognize, so an old `1.1.0` client talking to the
updated backend simply never sees/sends `userRole`, and the backend must treat "absent"
as a permanently legitimate input, not a transient migration state.

**The app sends its own version on every request via `X-App-Version:
<BuildConfig.VERSION_NAME>`.** This is a client-version *signal*, not an API version
*selector* — it doesn't change which API version is served. Its purposes:
1. Telemetry — correlate backend traffic/errors to client versions, to know when an old
   app version's usage has become negligible.
2. A last-resort, temporary conditional branch: if a `v1` contract change would
   otherwise be breaking (e.g. a field must become mandatory for a real business rule,
   but old clients structurally can't provide it), the backend may branch on
   `X-App-Version` to apply the new requirement only to clients capable of satisfying
   it, while older clients keep the old behavior. This is *not* rejection/enforcement —
   nothing is blocked — and it must be documented as temporary: once old-client usage is
   negligible, the branch is deleted and the field becomes unconditionally required (or,
   if the change is disruptive enough to warrant it, that endpoint moves to a real `v2`
   instead of an indefinite branch).

**No backend-side minimum-version enforcement/rejection is added now.** Building a
reject-old-clients gate before a real `v2` exists or before there's any adoption data
from the `X-App-Version` signal would be speculative complexity with nothing to act on
yet.

**Deprecation-window policy is scaled to this project's size, not copied from
enterprise-API guidance.** The common public-API guidance (N-2 version support, 6–12
months notice between deprecation and sunset, `Deprecation`/`Sunset` response headers)
is calibrated for third-party consumers an API owner can't force to upgrade. This is a
single first-party Android app whose release cadence the team fully controls, so the
window is tied to this repo's own release/changelog cadence (`docs/versioning.md`)
instead of a fixed calendar SLA — e.g. a `v1` field or endpoint stays supported until
the app version(s) depending on the old shape are no longer expected to be in active
use, judged from real usage/telemetry once it exists, not a blanket 6–12 months.

**DB versioning (backend) follows the expand-contract pattern**: add
nullable/defaulted column or table → deploy code that writes both old and new shape →
backfill → deploy code that reads only the new shape → drop the old column/table. This
pairs directly with the API's own additive-only rule above — both are the "expand" half
of expand-contract, done before any client is required to send or expect the new data.
Full detail lives in the backend repo's own ADR 0002.

## Consequences

**Easier:**
- Adding fields/params/endpoints to existing `v1` contracts requires no version
  coordination between app and backend releases — this is now the explicit default path
  and doesn't need a design discussion each time it comes up.
- The backend gains, for the first time, a way to know which app versions are actually
  making requests, which is the prerequisite for ever safely retiring old contract
  shapes or planning a real `v2`.
- The conditional-branch escape hatch (`X-App-Version`-gated behavior) gives a concrete,
  bounded answer for the one case that would otherwise force a premature `v2`: a field
  that must become required but that old clients can't yet provide.

**Harder / explicitly traded away:**
- The `/api/v1` literal remains duplicated across the three Retrofit service interfaces
  in `core:network` — a real drift risk if a version bump ever happens, but deliberately
  deferred as a separate follow-up rather than bundled into this decision.
- No backend enforcement exists yet, so nothing currently *prevents* an old, truly
  incompatible client from calling the backend and getting a broken response if a future
  change is made carelessly — this ADR only establishes the discipline (additive-only,
  conditional branches, eventual `v2`) and the signal (`X-App-Version`) to act on later;
  it does not itself guard against someone skipping the discipline.
- The scaled-down deprecation window (tied to this project's own release cadence rather
  than a fixed 6–12 month SLA) only works because this is a single first-party client.
  If this API ever gains third-party consumers, this policy would need to be revisited
  and likely tightened back toward the enterprise norm — that trigger should prompt a
  new ADR rather than a silent policy drift.
