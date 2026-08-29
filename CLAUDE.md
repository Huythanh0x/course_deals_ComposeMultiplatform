# CLAUDE.md

Facts and pointers for working in this repo. For process detail, follow the links into
`/docs` rather than expecting it duplicated here.

## Project snapshot

Native Android app, Kotlin, Clean Architecture (domain/data/feature layers), package
`com.thanh0x.coursedeals`. Coupon-scraping product — per `README.md` it can't currently
ship to Google Play, which is why a Web target exists on the roadmap (see KMP section
below).

- Kotlin 2.4.10, AGP 9.3.0, KSP 2.3.10, Gradle 9.5 (daemon pinned to JDK 21 —
  `gradle/gradle-daemon-jvm.properties`)
- compileSdk 37 / minSdk 24 / targetSdk 37, Java 17 source/target compatibility
- DI: Hilt + KSP (applied in every Android library module)
- Networking: Retrofit + Gson (no OkHttp entry in the catalog, no Ktor)
- Persistence: Room + Paging, DataStore Preferences
- Images: Picasso
- UI: XML views + Data Binding + Navigation Component. **No Jetpack Compose anywhere
  yet** — despite the repo name, Compose is the *planned* Web target, not current
  Android code (see ADR 0001 below).
- Two build flavors: `local` and `prod` — this is why lint/assemble task names are
  flavor-qualified for `app` (see Build commands).

## Module map

15 modules total (`settings.gradle`), each using one of two build-logic convention
plugins — never hand-roll `android {}`/`kotlin {}` blocks in a module's `build.gradle`,
add to or extend the convention plugin instead:

| Module | Plugin | Role |
|---|---|---|
| `app` | `com.android.application` directly | Application entry point, wires all modules |
| `core:common` | `coursedeals.kotlin.library` | Pure Kotlin/JVM, no Android deps — the only module actually platform-agnostic today |
| `core:ui` | `coursedeals.android.library` | Shared Fragment/AppCompat UI helpers, Picasso, Biometric |
| `core:network` | `coursedeals.android.library` | Retrofit setup, BuildConfig flavor wiring |
| `domain:coupons` | `coursedeals.android.library` + parcelize | Coupon domain models/use cases (uses `Parcelable`, Paging) |
| `domain:user` | `coursedeals.android.library` + parcelize | User domain models/use cases (uses Android Keystore) |
| `data:coupons` | `coursedeals.android.library` | Room + Paging repository implementation |
| `data:user` | `coursedeals.android.library` | DataStore-backed repository implementation |
| `feature:home` / `profile` / `detail` / `auth` / `splash` / `course` / `enroll` | `coursedeals.android.library` | Fragment + Data Binding UI feature modules |

Convention plugins live in
`build-logic/convention/src/main/kotlin/com/thanh0x/coursedeals/convention/`:
- `AndroidLibraryConventionPlugin` (`coursedeals.android.library`) — AGP + KSP + Hilt,
  compileSdk/minSdk, Java 17, data binding + build config enabled.
- `KotlinLibraryConventionPlugin` (`coursedeals.kotlin.library`) — pure
  `org.jetbrains.kotlin.jvm`, Java 17 source/target *and* Kotlin `jvmTarget` explicitly
  pinned to 17. (This explicit pin matters — see Known gaps: it's exactly the bug fixed
  in issue #6/PR #7. If you ever add a third convention plugin, set `jvmTarget`
  explicitly; don't rely on it defaulting to match whatever JDK runs the daemon.)

## State management & concurrency conventions

Every existing ViewModel (`HomeViewModel`, `CourseViewModel`, `LoginViewModel`,
`CouponDetailViewModel`, `ProfileViewModel`) already follows the same pattern
consistently — match it for any new ViewModel rather than introducing a different one:

```kotlin
private val _uiState = MutableStateFlow(FeatureUiState())
val uiState = _uiState.asStateFlow()
```

with `FeatureUiState` a data class (or sealed interface for Loading/Success/Error-style
screens) holding everything the UI needs to render. This is the same core pattern
Google's own Android architecture guidance and community skill packs converge on
(state-holder exposes a single observable state, UI is a pure function of it) — it's
UI-toolkit-agnostic, so it applies the same to our Fragment/DataBinding screens as it
would to Compose.

General Flow/coroutines discipline to follow (not currently violated anywhere found,
worth keeping true as the codebase grows):
- Give async work an explicit owner and lifetime — `viewModelScope`/`lifecycleScope`,
  not a manually stored `CoroutineScope` outliving its natural owner.
- Model renderable, current data as `StateFlow` (as above). Model a one-shot event
  (navigate, show a snackbar) separately — a conflated `StateFlow` silently drops or
  replays events it shouldn't; a `Channel` exposed via `receiveAsFlow()`, or a
  zero-replay `SharedFlow`, are the usual correct choices depending on whether a late
  collector should still see it.
- Don't add a `launch`/callback wrapper around a suspend function that already gives
  its caller cancellation and result ownership — that's needless indirection.

## Android component & Intent security

`app/src/main/AndroidManifest.xml` currently exports three activities:
`SplashActivity` (has the `MAIN`/`LAUNCHER` intent-filter, expected), `LoginActivity`,
and `CouponDetailActivity` (both `exported="true"` with no intent-filter shown — meaning
any app on the device can still launch them explicitly by component name, deep-link
filters or not). Treat this as the current, real security surface, not a hypothetical:
- Never trust extras read from `getIntent()`/`intent.extras` in an exported component as
  if they only ever came from inside this app.
- If any code ever forwards or relays a received `Intent` to launch another component
  (intent redirection), sanitize it first — `androidx.core.content.IntentSanitizer`
  (needs `androidx.core:core:1.9.0+`, already satisfied by this repo's AndroidX
  versions) explicitly allowlists components/actions/data/extras before launching.
- Prefer `PendingIntent.FLAG_IMMUTABLE` unless a specific feature (inline notification
  reply, etc.) genuinely needs `FLAG_MUTABLE`.

## Build & verify commands

- Full build (mirrors CI): `./gradlew build`
- Detekt: `./gradlew :app:detekt` (see Known gaps — only wired into `app` today)
- Lint is flavor-qualified because of the `local`/`prod` split:
  - `app` module: `./gradlew :app:lintLocalDebug` (plain `:app:lintDebug` is ambiguous
    and fails)
  - Library modules (no flavors): `./gradlew :core:ui:lintDebug`, etc.
- Unit tests: `./gradlew testLocalDebugUnitTest` (or `test<Variant>UnitTest` per module)
- `git commit` messages are enforced by the `commit-msg` hook (Conventional Commits
  regex) and `pre-commit` runs `:app:detekt` — both installed via
  `./gradlew installGitHooks`, run once per clone if hooks aren't already installed.

## Git / GitHub workflow

One-line version: **issue → branch `<type>/<issue-number>-<slug>` off latest `main` →
commits passing the `commit-msg` hook → PR from `.github/PULL_REQUEST_TEMPLATE.md` with
`Closes #N` → CI green → squash-merge → delete branch.** Full detail, don't duplicate
here:
- [`docs/project-management.md`](docs/project-management.md) — label taxonomy (type /
  priority / area / size), Definition of Done
- [`docs/branching.md`](docs/branching.md), [`docs/commit-conventions.md`](docs/commit-conventions.md),
  [`docs/pull-requests.md`](docs/pull-requests.md)
- [`docs/adr-guide.md`](docs/adr-guide.md) — when a decision is ADR-worthy, template,
  status lifecycle

The `new-ticket` → `implement-ticket` → `ship-pr` skills cover issue creation,
end-to-end implementation (with a resumable per-ticket workspace under
`.claude/tickets/`, git-ignored), and PR opening respectively.

## Known gaps (state as fact — surface opportunistically, e.g. via a spawned task, don't
silently fix mid-task on unrelated work)

- Detekt is wired into `app` only (`alias(libs.plugins.detekt)` in `app/build.gradle`);
  other modules aren't checked by `./gradlew detekt` or the pre-commit hook.
- Zero real test coverage anywhere — every module except `app` has no `src/test`
  directory at all, and `app`'s only tests are the unmodified Android Studio
  boilerplate (`ExampleUnitTest`, `ExampleInstrumentedTest`).
- `README.md`'s SDK version mentions (24-33) are stale versus `libs.versions.toml`
  (compileSdk/targetSdk now 37).

## Testing — recommended approach (not yet adopted)

The version catalog has only `junit`, `androidx-junit`, `androidx-espresso-core`, and
`androidx-room-testing` — no mocking library, no Turbine, no `kotlinx-coroutines-test`.

Lead with **fakes behind interfaces, not mocking libraries** as the default — this is
what Google's own official Android testing guidance and the NowInAndroid reference app
both converge on independently: for a dependency on an Android framework class or
something outside this codebase, first give it an interface with a real implementation
and a `Fake` test-double implementation; only reach for a mocking library (`MockK`) when
faking genuinely isn't feasible. `Turbine` + `kotlinx-coroutines-test` are still worth
adding regardless, for testing the Flow-based Room/DataStore repositories in `data:*`.
`Robolectric` is also worth considering for Fragment/View-based UI tests that would
otherwise need an emulator.

Treat actually adopting a testing stack as a real decision worth its own issue (and
likely an ADR, per `docs/adr-guide.md` — it's a genuine tradeoff, not a
trivial/reversible pick) rather than something to bootstrap incidentally while doing
unrelated work. The `testing-strategy` skill runs the analyze-then-recommend workflow
for this repo specifically when that decision is actually made.

## Kotlin Multiplatform migration awareness

[`docs/adr/0001-kotlin-multiplatform-over-flutter.md`](docs/adr/0001-kotlin-multiplatform-over-flutter.md)
(Accepted) records the decision to eventually share the domain/data layer with a new
Compose Multiplatform Web UI, while the existing native Android UI
(Fragment/DataBinding) stays exactly as-is — **not yet implemented**, tracked as a
separate initiative.

Current reality: `domain:coupons`, `domain:user`, `data:coupons`, and `data:user` are
**not** platform-agnostic today. They use the `coursedeals.android.library` convention
plugin (an AGP library, not pure Kotlin) and import Android-only APIs directly:
`android.os.Parcelable` (domain models), `android.security.keystore.*` (Android
Keystore, in `domain:user`'s `CryptographyManagerUseCase`), Room and
`android.content.Context` (`data:coupons`), Jetpack DataStore (`data:user`). Only
`core:common` is actually pure Kotlin/JVM today, and it's essentially an empty
placeholder module.

Guidance: don't force premature `expect`/`actual` abstractions into domain/data code
now — the KMP work isn't scheduled, and speculative abstraction ahead of a real need is
exactly the kind of premature complexity to avoid. But when touching those modules for
unrelated reasons, prefer a platform-agnostic alternative when it's equally simple (e.g.
don't reach for a new Android-only API in `domain:*` if a plain-Kotlin equivalent does
the same job). Android Keystore in particular has no cross-platform equivalent — that's
a genuinely hard blocker that will need a real design decision (and its own ADR) when
the KMP initiative actually starts; don't paper over it.

## External skill packs — analyzed, partially folded in

Reviewed the actual content (not just descriptions) of the popular Android/KMP Claude
Code skill packs. The parts that apply to this repo *today* (Fragment/DataBinding, no
Compose, no KMP yet) are already folded into this file and the `testing-strategy` /
`intent-security-review` skills above, in our own words rather than copied verbatim:
- [`android/skills`](https://github.com/android/skills) (official Google) —
  `testing-setup`'s analyze-then-recommend methodology and `android-intent-security`'s
  exported-component guidance are the two genuinely current-relevant skills in this
  pack; the rest (AGP migration, XML→Compose migration, Navigation 3) aren't applicable
  yet.
- [`chrisbanes/skills`](https://github.com/chrisbanes/skills) —
  `kotlin-concurrency-and-flow`'s ownership/state-vs-event principle is
  UI-toolkit-agnostic and genuinely applies now (see State management section above);
  the rest of that pack is Compose-specific and doesn't apply yet.
- [`dpconde/claude-android-skill`](https://github.com/dpconde/claude-android-skill) —
  its ViewModel+UiState state-holder pattern and offline-first/unidirectional-data-flow
  principles matched what this codebase already does; its concrete templates are
  Compose-screen-specific and don't transfer.

Not yet relevant, install only when the timing described applies:
- `github.com/android/skills`'s Compose/Navigation-3/AGP-migration skills, and
  `chrisbanes/skills`'s Compose-specific skills (state hoisting, performance, UI
  testing) — once Compose work actually starts.
- A KMP-specific pack (e.g. `rcosteira79/android-skills` or
  `felipechaux/kmp-compose-multiplatform-skill`) — once the ADR 0001 initiative is
  actually kicked off.
