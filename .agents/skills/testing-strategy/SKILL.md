---
name: testing-strategy
description: Testing architecture and conventions for `course_deals_ComposeMultiplatform` across KMP (`commonTest` / `jvmTest`), Android (`testLocalDebugUnitTest`), Compose Desktop (`runComposeUiTest`), iOS/watchOS (`Swift Testing` + `:shared:linkDebugFrameworkIosSimulatorArm64`), and Maestro E2E flows.
---

# Course Deals KMP & Multiplatform Testing Strategy (`testing-strategy`)

Aligned to `course_deals_ComposeMultiplatform` (Kotlin Multiplatform + Compose Multiplatform across Android, iOS, watchOS, and Desktop).

## 1. Testing Pyramid Across Platforms

| Layer | Target & Tooling | Command |
|---|---|---|
| **Domain & Data Unit Tests** | `commonTest` / `jvmTest` (`JUnit 4`, `kotlinx-coroutines-test`, Fakes behind `:domain:*` interfaces, Ktor `MockEngine`) | `./gradlew :data:coupons:jvmTest :data:user:jvmTest testLocalDebugUnitTest` |
| **Feature ViewModel Tests** | `commonTest` / `jvmTest` asserting `StateFlow<FeatureUiState>` across all 4 quadrants (Happy, Failed, Error, Related) | `./gradlew testLocalDebugUnitTest` |
| **Compose Desktop UI Framework Tests** | `desktopApp/src/test/kotlin` using `createComposeRule()` / `runComposeUiTest` on semantic `Modifier.testTag` nodes | `./gradlew :desktopApp:test` |
| **iOS Framework Link & Native Tests** | Kotlin/Native iOS Simulator framework link + Xcode `Swift Testing` (`@Test`, `#expect`) for `iosApp` / `watchApp` | `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` |
| **Cross-Platform E2E GUI Flows** | Declarative Maestro YAML flows (`.maestro/flows/*.yaml`) with `- takeScreenshot:` on Android (`emulator-5554`), iOS (`iPhone Simulator`), and Desktop (`:desktopApp:runMaestroFlow`) | See `capture-evidence` |

## 2. Core KMP Testing Rules

1. **Fakes Over Mocks:**
   - Implement hand-written fakes (`FakeCouponRepository`, `FakeUserRepository`, `InMemorySettingsStore`) behind `:domain:*` interfaces in `commonMain`/`commonTest`.
2. **Test Ktor Repositories Through `MockEngine`:**
   - Feed real JSON payloads (200 OK, 400 validation errors, 401 token refresh, 500 server failures, unknown enum values) into `HttpClient(MockEngine { ... })` configured with `:core:network`'s JSON serializer.
3. **No Commas in Backticked Test Names:**
   - Kotlin/Native (`iosSimulatorArm64Test`) rejects `,` in function names (`error: name contains illegal characters: ","`). Write `` fun `login with valid otp updates state to authenticated`() ``.
4. **Keep `getString(Res.string.*)` Out of ViewModels:**
   - Resolve Compose string resources in `@Composable` UI layers so pure JVM unit tests never fail with `MissingResourceException`.
5. **4-Quadrant Scenario Coverage:**
   - Every feature ticket must include unit tests covering **Happy Path**, **Failed Path (Domain Validation)**, **Error Path (Network/500)**, and **Related Changes/Regressions**.
