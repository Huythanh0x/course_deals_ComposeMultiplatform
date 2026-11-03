---
name: new-module
description: Scaffold a new Gradle module (`feature:*`, `domain:*`, `data:*`, `core:*`) in `course_deals_ComposeMultiplatform` using the `coursedeals.kmp.library` convention plugin, Koin DI, and Compose Multiplatform source sets (`src/commonMain/kotlin`).
---

# New KMP Module (`new-module`)

Scaffolds a new module in `course_deals_ComposeMultiplatform` using the repository's convention plugins in `build-logic/convention/src/main/kotlin/com/thanh0x/coursedeals/convention/`. Never hand-roll `kotlin {}` targets or Detekt setup in module `build.gradle` files.

## Which Convention Plugin to Use

- **`coursedeals.kmp.library`** (Default for all `core:*`, `domain:*`, `data:*`, `feature:*`, and `shared` modules):
  - Configures Kotlin Multiplatform targets (`jvm` with `JvmTarget.JVM_17`, `iosArm64`, `iosSimulatorArm64`) + Detekt across `src/commonMain/kotlin`, `src/jvmMain/kotlin`, `src/iosMain/kotlin`.
  - For UI modules (`feature:*`, `core:ui`, `shared`), also apply `org.jetbrains.kotlin.plugin.compose` and `org.jetbrains.compose`.
- **`coursedeals.android.library`** / **`coursedeals.kotlin.library`**:
  - Only for platform-specific JVM/Android helper modules when strictly required.

## Steps

1. Create the KMP source set directory structure matching a sibling module (e.g. `feature/home`):
   - `src/commonMain/kotlin/com/thanh0x/coursedeals/<layer>/<name>/`
   - `src/commonTest/kotlin/com/thanh0x/coursedeals/<layer>/<name>/` (or `src/jvmTest/kotlin/...`)
2. Write `build.gradle` matching existing modules:
   ```groovy
   plugins {
       id 'coursedeals.kmp.library'
       // Include the two plugins below only for UI modules (feature:*, core:ui, shared):
       id 'org.jetbrains.kotlin.plugin.compose'
       id 'org.jetbrains.compose'
   }

   kotlin {
       sourceSets {
           commonMain.dependencies {
               implementation project(':core:common')
               // For feature modules:
               // implementation project(':core:ui')
               // implementation project(':domain:coupons')
               // implementation libs.koin.core
               // implementation libs.koin.compose.viewmodel
           }
           jvmTest.dependencies {
               implementation libs.junit
               implementation libs.kotlinx.coroutines.test
           }
       }
   }
   ```
3. Register the module in `settings.gradle`:
   ```groovy
   include ':<layer>:<name>'
   ```
4. Wire Koin bindings in `:shared` (and `:app` / `:desktopApp` if needed), respecting Clean Architecture dependency direction (`feature:*` → `core:ui`/`domain:*`; `data:*` → `core:network` + `domain:*`).
5. Verify compilation and Detekt across targets:
   ```bash
   ./gradlew :<layer>:<name>:compileKotlinJvm :<layer>:<name>:compileKotlinIosSimulatorArm64 :<layer>:<name>:detekt
   ```
