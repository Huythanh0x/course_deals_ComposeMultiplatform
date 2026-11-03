# AGENTS.md

Facts and pointers for working in this repo with Google Antigravity. For process detail,
follow the links into `/docs` rather than expecting it duplicated here.

## Project snapshot

Kotlin Multiplatform (KMP) + Compose Multiplatform app (`:shared`, `:app` Android entry
point, `:desktopApp` Compose Desktop entry point, `iosApp` Xcode iOS entry point + `iosApp/WatchApp` native SwiftUI Apple Watch companion), Clean
Architecture (domain/data/feature layers), package `com.thanh0x.coursedeals`.

- Kotlin 2.4.10, Compose Multiplatform 1.8.0, AGP 9.3.0, KSP 2.3.10, Gradle 9.5 (daemon
  pinned to JDK 21 — `gradle/gradle-daemon-jvm.properties`)
- compileSdk 37 / minSdk 24 / targetSdk 37, Java 17 source/target compatibility
- Targets: `jvm` (Android/Desktop JVM bytecode) + `iosArm64` + `iosSimulatorArm64` (+ native SwiftUI `WatchApp` in Xcode)
- DI: Koin (`koin-core`, `koin-android`, `koin-androidx-compose`, `koin-compose`,
  `koin-compose-viewmodel`)
- Networking: Ktor Client (`ktor-client-okhttp` on JVM/Android, `ktor-client-darwin` on
  iOS) + OpenAPI Generator (`org.openapi.generator` 7.12.0) + `kotlinx.serialization`
- Observability & Logging: Sentry KMP (`io.sentry:sentry-kotlin-multiplatform` 0.29.0), `StartupTimeMonitor`, and `DailyRollingFileLogWriter` (`:core:logging`)
- Persistence: KMP `SettingsStore` (`DataStore` on Android/JVM, `NSUserDefaults` on iOS), Room
  + Paging
- Images: Coil 3 (`coil-compose`, `coil-network-okhttp`, `coil-network-ktor3`)
- UI: Compose Multiplatform in `src/commonMain/kotlin` across `:shared`, `:core:ui`, and
  all `:feature:*` modules; native SwiftUI in `iosApp/WatchApp/` for watchOS.
- Two Android build flavors: `local` and `prod` — this is why lint/assemble task names are
  flavor-qualified for `app` (see Build commands).

## Module map

17 Gradle modules total (`settings.gradle`), plus `iosApp/` Xcode workspace (`iosApp` + `WatchApp`):

| Module | Plugin | Role |
|---|---|---|
| `app` | `com.android.application` | Android application entry point, wires Koin & `CourseDealsApp()` |
| `desktopApp` | `org.jetbrains.kotlin.jvm` + Compose Multiplatform | Compose Desktop (`jvm`) entry point (`Main.kt`), CLI deep-linking (`--screen=...`), and Maestro E2E flow runner (`runMaestroFlow`) |
| `shared` | `coursedeals.kmp.library` + Compose Multiplatform | Shared KMP root (`CourseDealsApp`, `MainViewController` for iOS `ComposeApp.framework`) |
| `core:common` | `coursedeals.kmp.library` | Shared KMP utilities, dispatchers, `PlatformTime`, and base models |
| `core:logging` | `coursedeals.kmp.library` | Sentry KMP tracing (`CommonTracingInitializer`), `StartupTimeMonitor`, and rolling file logs (`DailyRollingFileLogWriter`) |
| `core:ui` | `coursedeals.kmp.library` + Compose Multiplatform | Shared Compose Multiplatform theme, dedicated vector icons, `ViewModel` error boundary, and `PlatformWebView` |
| `core:network` | `coursedeals.kmp.library` | Ktor `HttpClient` setup, OpenAPI-generated client/DTOs (`generateCourseDealsApi`), auth/refresh & server error interceptors |
| `domain:coupons` | `coursedeals.kmp.library` | Coupon domain models, repository interfaces, and use cases (`commonMain`) |
| `domain:user` | `coursedeals.kmp.library` | User/Auth domain models, repository interfaces, and use cases (`commonMain`) |
| `data:coupons` | `coursedeals.kmp.library` | Coupon repository implementation and Ktor remote data sources (`commonMain`) |
| `data:user` | `coursedeals.kmp.library` | User/Auth repository implementation and `SettingsStore` persistence (`commonMain` + `jvmMain`/`iosMain`) |
| `feature:home` / `profile` / `detail` / `auth` / `splash` / `course` / `enroll` | `coursedeals.kmp.library` + Compose Multiplatform | Compose Multiplatform screens and `ViewModel`s in `src/commonMain/kotlin` |

Convention plugins live in
`build-logic/convention/src/main/kotlin/com/thanh0x/coursedeals/convention/`:
- `KmpLibraryConventionPlugin` (`coursedeals.kmp.library`) — Kotlin Multiplatform (`jvm`
  with `JvmTarget.JVM_17`, `iosArm64`, `iosSimulatorArm64`) + Detekt configured across all
  KMP source sets (`src/commonMain/kotlin`, `src/jvmMain/kotlin`, `src/iosMain/kotlin`,
  `src/androidMain/kotlin`, `src/main/java`, `src/main/kotlin`).
- `AndroidLibraryConventionPlugin` (`coursedeals.android.library`) — AGP + KSP + Koin,
  compileSdk/minSdk, Java 17.
- `KotlinLibraryConventionPlugin` (`coursedeals.kotlin.library`) — pure
  `org.jetbrains.kotlin.jvm` with Java 17 and `JvmTarget.JVM_17`.

## State management & concurrency conventions

Every ViewModel (`HomeViewModel`, `CourseViewModel`, `LoginViewModel`,
`CouponDetailViewModel`, `ProfileViewModel`, `EnrollViewModel`, `SplashViewModel`) lives
in `src/commonMain/kotlin` and follows the same pattern consistently:

```kotlin
private val _uiState = MutableStateFlow(FeatureUiState())
val uiState = _uiState.asStateFlow()
```

with `FeatureUiState` a data class (or sealed interface for Loading/Success/Error-style
screens) holding everything the UI needs to render.

General Flow/coroutines discipline to follow:
- Give async work an explicit owner and lifetime — `viewModelScope`/`lifecycleScope`,
  not a manually stored `CoroutineScope` outliving its natural owner.
- Model renderable, current data as `StateFlow` (as above). Model a one-shot event
  (navigate, show a snackbar) separately — a `Channel` exposed via `receiveAsFlow()`, or a
  zero-replay `SharedFlow`, are the usual correct choices.
- Don't add a `launch`/callback wrapper around a suspend function that already gives
  its caller cancellation and result ownership.

## Android component & Intent security

- Never trust extras read from `getIntent()`/`intent.extras` in an exported component as
  if they only ever came from inside this app.
- If any code ever forwards or relays a received `Intent` to launch another component
  (intent redirection), sanitize it first — `androidx.core.content.IntentSanitizer`
  explicitly allowlists components/actions/data/extras before launching.
- Prefer `PendingIntent.FLAG_IMMUTABLE` unless a specific feature genuinely needs
  `FLAG_MUTABLE`.

## Build & verify commands

- Full build (mirrors CI): `./gradlew build`
- Multi-module Detekt (all 17 modules): `./gradlew detekt`
- Desktop app launch (fast local UI & API testing without Android Emulator):
  - Interactive window: `./gradlew :desktopApp:run`
  - Deep-link & automated screenshot capture: `./gradlew :desktopApp:run -Dcoursedeals.args="--screen=home --capture-screenshot=/tmp/desktop_home.png"`
  - Maestro-compatible Desktop E2E YAML flow runner & step-by-step screenshot export:
    `./gradlew :desktopApp:runMaestroFlow -Pflow=.maestro/flows/07-auth-e2e-evidence.yaml -PscreenshotDir=/tmp/desktop-e2e`
- iOS Simulator framework link gate (mirrors macOS CI job):
  `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`
- Android debug APK assemble: `./gradlew :app:assembleLocalDebug`
- Multi-platform release packaging (APK, IPA/xcarchive, Desktop DMG/App with SHA256 checksums):
  `bash scripts/release-build.sh --version 1.0.0 --platform all`
- Lint is flavor-qualified because of the `local`/`prod` split:
  - `app` module: `./gradlew :app:lintLocalDebug` (plain `:app:lintDebug` is ambiguous
    and fails)
- Unit tests: `./gradlew testLocalDebugUnitTest` (or `jvmTest` in KMP modules, e.g. `./gradlew :data:coupons:jvmTest :data:user:jvmTest`)
- OpenAPI Ktor & DTO Codegen: `./gradlew generateCourseDealsApi`
  - Syncs `../course_deals_backend/openapi/openapi.json` into `core/network/openapi/openapi.json` (when the sibling backend repo is present) and runs OpenAPI Generator (`kotlin` + `library = "multiplatform"`, `dateLibrary = "kotlinx-datetime"`) into `core/network/src/commonMain/gen/com/thanh0x/coursedeals/core/network/openapi/`.
  - `core/network/openapi/openapi.json` is committed to version control as the single source of truth, while `core/network/src/commonMain/gen/` is gitignored and generated automatically during build before `:core:network` compiles.
- `git commit` messages are enforced by the `commit-msg` hook (Conventional Commits
  regex) and `pre-commit` runs `./gradlew detekt` across all modules — both installed via
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

The `team-flow` skill orchestrates a **7-role specialized team** across `course_deals_backend`
and `course_deals_ComposeMultiplatform` (**PM/Architect**, `backend-engineer`, `kmp-engineer`,
`apple-developer` [iOS & watchOS], `desktop-engineer`, `security-arch-reviewer`, and
`qa-release-engineer`) with only **2 human-in-the-loop checkpoints**:
1. **Spec, API Contract & Edge Cases approval** (before creating issues/branches and launching parallel engineering subagents)
2. **Ready to Merge** (after coding, cross-repo DTO/security/KMP audit, live E2E & R2 screenshot capture, and PR creation)

The `sprint-flow` skill manages the **1-week solo developer sprint rhythm** (Monday–Saturday build & test, Sunday stop & plan) integrated with GitHub Projects:
- [`docs/SPRINT_GUIDE.md`](docs/SPRINT_GUIDE.md) — 1-week sprint lifecycle, iteration conventions, and mobile DoD
- `sprint-flow plan` — Sunday backlog grooming, point budgeting (15–20 pts), and iteration setup
- `sprint-flow retro` — Sunday velocity and completed points inspection

## Known gaps (state as fact — surface opportunistically, e.g. via a spawned task, don't
silently fix mid-task on unrelated work)

- Unit test coverage across KMP modules is still growing; lead with **fakes behind
  interfaces, not mocking libraries** + `kotlinx-coroutines-test` and Ktor `MockEngine`
  when adding tests in `commonTest` / `jvmTest`.
