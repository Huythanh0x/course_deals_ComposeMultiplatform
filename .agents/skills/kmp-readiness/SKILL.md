---
name: kmp-readiness
description: Audit `course_deals_ComposeMultiplatform` modules (`:shared`, `:core:*`, `:domain:*`, `:data:*`, `:feature:*`, `:desktopApp`, `iosApp`, `watchApp`) for KMP target purity, platform boundary discipline, and multiplatform build health.
---

# KMP & Multiplatform Architecture Audit (`kmp-readiness`)

All 14 shared modules in `course_deals_ComposeMultiplatform` (`:shared`, `:core:common`, `:core:ui`, `:core:network`, `:domain:coupons`, `:domain:user`, `:data:coupons`, `:data:user`, and `:feature:home`, `profile`, `detail`, `auth`, `splash`, `course`, `enroll`) are Kotlin Multiplatform modules using `coursedeals.kmp.library` (`commonMain`, `jvmMain`, `iosMain`).

Use this skill to audit changes for multiplatform purity and target safety.

## 1. Platform Boundary Rules

| Source Set / Module | Allowed Dependencies | Forbidden Leaks |
|---|---|---|
| `src/commonMain/kotlin` (`:domain:*`, `:data:*`, `:core:*`, `:feature:*`, `:shared`) | Pure Kotlin, `kotlinx.coroutines`, `kotlinx.serialization`, `kotlinx-datetime`, Ktor `commonMain`, Koin `commonMain`, Compose Multiplatform | **NEVER** `import android.*`, `import java.awt.*`, `import javax.swing.*`, `import javafx.*`, or `import platform.UIKit.*` |
| `src/jvmMain/kotlin` | JVM / Android / Desktop shared bytecode abstractions | Platform UI toolkits that break Android or Desktop |
| `src/iosMain/kotlin` | Kotlin/Native `platform.Foundation.*`, `platform.UIKit.*`, Darwin Ktor engine | JVM/Android classes |
| `:app` | Android entry point (`MainActivity`, `AndroidManifest.xml`, Android Koin bootstrap) | Desktop `compose.desktop.*` or JavaFX |
| `:desktopApp` | JVM Desktop entry point (`Window`, `SwingPanel`, `JFXPanel`, `kotlinx-coroutines-swing`) | `android.*` imports |
| `iosApp/` & `watchApp/` | Native SwiftUI host, `ComposeApp.framework` bridge, Keychain, WidgetKit, `WatchConnectivity` (`WCSession`) | Hardcoded secrets in `Info.plist` or `UserDefaults` |

## 2. Audit Steps

1. **Grep `commonMain` for Platform Leaks:**
   ```bash
   grep -rnE "^import (android\.|java\.awt\.|javax\.swing\.|javafx\.)" core/ domain/ data/ feature/ shared/ --include="*.kt" | grep "/commonMain/"
   ```
2. **Verify Abstraction Mechanism:**
   - Stateless fact/helper → `expect`/`actual`.
   - Stateful service/repository → `interface` in `commonMain` (never `expect class` with divergent constructors).
   - Swift-only Apple framework (`WidgetKit`, `ActivityKit`, `WCSession`) → Kotlin interface in `:shared` implemented in Swift inside `iosApp/`.
3. **Verify Multiplatform Compilation Gates:**
   - `./gradlew detekt`
   - `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`
   - `./gradlew :desktopApp:compileKotlin`
