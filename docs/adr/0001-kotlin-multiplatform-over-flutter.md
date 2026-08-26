# 0001: Kotlin Multiplatform over Flutter for the shared module

## Status
Accepted

## Context
The existing coupon Android app (Kotlin, Clean Architecture) can't be published to
Google Play due to the nature of the coupon-scraping product. A Web-facing version is
needed to actually showcase the product and the underlying engineering. Flutter would
mean a second, unrelated UI toolkit and language investment; Kotlin Multiplatform lets
the existing Android knowledge and a shared business-logic layer extend directly to Web.

## Decision
Use Kotlin Multiplatform with Compose Multiplatform for the Web target, sharing the
domain/data layer between this existing native Android app and a new Web UI.

## Consequences
- Reuses existing Kotlin knowledge instead of learning Dart/Flutter from scratch.
- Compose Multiplatform's Web target is newer/less mature than its Android target —
  expect some tooling friction.
- Native Android UI stays as-is (Fragment/DataBinding, as in this repo today); only new
  UI work targets Web, so there's no forced migration of the existing app's UI layer.
- Not yet implemented — this ADR records the decision ahead of the actual build-out,
  which is tracked as a separate initiative from this repo's day-to-day feature work.
