---
name: kmp-readiness
description: Audit a module or file for Android-only APIs that would block a future Kotlin Multiplatform commonMain move, per docs/adr/0001-kotlin-multiplatform-over-flutter.md. Use when the user asks whether code is KMP-ready, what's blocking the KMP migration, or wants a readiness check on domain/data modules before or during that initiative.
---

# KMP readiness check

Audits code against the KMP migration recorded (Accepted, not yet implemented) in
`docs/adr/0001-kotlin-multiplatform-over-flutter.md`. That ADR's plan is to share the
domain/data layer with a new Compose Multiplatform Web UI while native Android UI stays
untouched — so this check only ever concerns `domain:*` / `data:*` (and any future
shared module), never `feature:*` or `app`.

## What to grep for

In the target module/file, look for these Android-only signals (see `CLAUDE.md`'s KMP
section for the current known state — as of now, all four of `domain:coupons`,
`domain:user`, `data:coupons`, `data:user` have hits):

| API | Where it typically shows up | What it needs for KMP |
|---|---|---|
| `android.os.Parcelable` / `@Parcelize` | domain models passed between Android UI components | Usually droppable for the shared layer — Parcelable is an Android UI-navigation concern, not a domain concept; the shared model doesn't need it, only the Android-side UI wrapper would |
| `android.content.Context` | Room/DataStore setup, resource access | Needs an `expect`/`actual` platform abstraction (e.g. a `expect class AppContext` or DI-injected platform accessor) |
| `androidx.room.*` | `data:*` persistence | Room has an official KMP artifact — check the version in use supports it before assuming a rewrite is needed |
| `androidx.datastore.*` | `data:user` preferences | DataStore also has KMP support as of recent versions — same caveat, check the version |
| `android.security.keystore.*` (Android Keystore) | `domain:user`'s `CryptographyManagerUseCase` | **No cross-platform equivalent.** This is a genuine hard blocker — needs a real `expect`/`actual` with a different crypto approach per platform, and is worth its own ADR when tackled, not a quick fix |
| `androidx.paging.*` | `domain:coupons`, `data:coupons` | Paging also has a KMP artifact — check version/support before assuming rewrite |

## Steps

1. Grep the target module for `import android` (excluding `androidx` KMP-supported
   libraries already flagged above) to catch anything not in the table.
2. For each hit, classify it: droppable (no real reason it needs Android), needs
   `expect`/`actual` (platform behavior genuinely differs), or hard blocker (no
   cross-platform equivalent exists — Android Keystore is the only currently-known one).
3. Report findings as a list per file, not a rewrite — this skill diagnoses, it doesn't
   migrate. Actually moving a module to `commonMain`/KMP source sets is real
   architectural work that should go through the `adr` skill first if it hasn't already
   been decided, then its own tracked issue(s).
4. Don't propose speculative `expect`/`actual` scaffolding for code that isn't actually
   being migrated right now — per `CLAUDE.md`, premature abstraction ahead of the real
   KMP work is exactly what to avoid.
