---
name: testing-strategy
description: Analyze this repo's current testing setup and recommend a concrete testing stack, following CLAUDE.md's fakes-first philosophy. Use when the user asks to set up tests, add test coverage, choose testing libraries, or asks what testing approach this project should use.
---

# Testing strategy

This repo currently has **zero real test coverage** (only Android Studio's unmodified
boilerplate `ExampleUnitTest`/`ExampleInstrumentedTest` in `app`) and no mocking/Flow-
testing libraries in the catalog. Adapted from Google's own official Android
testing-setup guidance and the NowInAndroid reference app's testing philosophy — both
independently converge on the same approach. See `CLAUDE.md`'s Testing section for the
one-paragraph summary; this skill is the workflow for actually acting on it.

## Step 1 — re-confirm the current state (don't assume it's still accurate)

- DI framework: Hilt (confirmed, every `coursedeals.android.library` module)
- UI toolkit: XML views + Data Binding + Fragments (confirmed, no Compose)
- Existing test libs: `junit`, `androidx-junit`, `androidx-espresso-core`,
  `androidx-room-testing` — check `gradle/libs.versions.toml` again, this may have
  changed since CLAUDE.md was last updated
- State pattern: `MutableStateFlow`/`asStateFlow()` + `UiState` in every ViewModel (see
  CLAUDE.md's State management section) — this is what needs testing

## Step 2 — recommend, don't silently install

This is a real decision (per `docs/adr-guide.md`, worth its own ADR) — present the
recommendation and get confirmation before touching `gradle/libs.versions.toml`:

1. **Fakes over mocks as the default.** For a ViewModel's repository dependency,
   prefer a hand-written `FakeCouponRepository : CouponRepository` (or whatever the
   interface is) over mocking it — only reach for a mocking library when a real fake
   isn't feasible (e.g. faking a final Android framework class with no seam).
2. If domain/data interfaces don't already exist for the repositories a test needs to
   fake, that's the actual prerequisite work — surface it rather than reaching for a
   mocking library as a shortcut around missing abstractions.
3. `Turbine` + `kotlinx-coroutines-test` for asserting `StateFlow`/`Flow` emissions from
   ViewModels and Room/DataStore-backed repositories.
4. `MockK` as the fallback mocking library when a fake genuinely isn't practical.
5. `Robolectric` for Fragment/View-based UI tests that would otherwise require an
   emulator — this repo has no Compose, so Compose Testing APIs don't apply.

## Step 3 — land it properly

- Add chosen libraries to `gradle/libs.versions.toml` (version catalog, not inline
  versions in module `build.gradle`).
- Wire test dependencies into the convention plugins
  (`build-logic/convention/.../AndroidLibraryConventionPlugin.kt` /
  `KotlinLibraryConventionPlugin.kt`) if every module should get them, not one module's
  `build.gradle` — consistent with how this repo already centralizes config there.
- Follow the `new-ticket`/`ship-pr` skills for the actual issue/PR — this is exactly
  the kind of change `docs/project-management.md` says deserves its own tracked issue,
  not a drive-by addition during unrelated work.
