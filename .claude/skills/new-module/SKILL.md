---
name: new-module
description: Scaffold a new Gradle module (feature/domain/data/core) using this repo's build-logic convention plugins, matching the existing module patterns. Use when the user asks to create/add a new Gradle module, feature module, domain module, or data module in this repo.
---

# New module

Scaffolds a new module using one of the two existing convention plugins — never
hand-roll `android {}`/`kotlin {}` blocks in a new module's `build.gradle`. See
`CLAUDE.md`'s module map for which plugin every existing module uses, and
`build-logic/convention/src/main/kotlin/com/thanh0x/coursedeals/convention/` for the
plugin source.

## Which plugin

- **`coursedeals.android.library`** — for anything with Android dependencies (Context,
  Fragment, Room, DataStore, Parcelable, etc.) or that ships resources/manifest. This is
  what every existing module except `core:common` uses, including `domain:*` and
  `data:*` today (see CLAUDE.md's KMP section — this is a known, accepted state, not a
  mistake to "fix" while adding a new module).
- **`coursedeals.kotlin.library`** — only for a module that's genuinely pure
  Kotlin/JVM with zero Android imports, matching `core:common`.

If truly unsure, default to `coursedeals.android.library` — it's what every real
(non-placeholder) module in the repo uses.

## Steps

1. Create the directory structure matching a sibling module of the same kind, e.g. for
   a new feature module mirror `feature:home`'s layout
   (`src/main/java/...`, `src/main/res/...`, `AndroidManifest.xml` if it needs one).
2. Write `build.gradle` (Groovy DSL, matching every existing module — this repo doesn't
   use `.gradle.kts` in module build files):
   ```groovy
   plugins {
       id 'coursedeals.android.library'   // or coursedeals.kotlin.library
       id 'com.google.devtools.ksp'        // only if using Hilt/KSP-generated code
       id 'dagger.hilt.android.plugin'     // only for Android library modules
   }

   android {
       namespace 'com.thanh0x.coursedeals.<layer>.<name>'
   }

   dependencies {
       implementation project(':core:common')
       // add sibling module deps following the existing dependency direction:
       // feature -> core:ui/core:common -> domain:* ; data:* -> core:network + domain:*
   }
   ```
3. Add the module to `settings.gradle`:
   ```groovy
   include ':<layer>:<name>'
   ```
4. Confirm the dependency direction matches the existing layering (feature modules
   depend on `core:ui`/`domain:*`, never the reverse; `data:*` depends on
   `core:network` + `domain:*`; nothing depends on `app`).
5. Verify it builds: `./gradlew :<layer>:<name>:build` (or `:lintDebug` /
   `:lintLocalDebug` if it's `app`).

## Don't

- Don't set `compileSdk`, `minSdk`, Java/Kotlin version compatibility, or KSP/Hilt
  plugin versions by hand in the new module — those come from the convention plugin.
  If the new module needs something the convention plugin doesn't provide, that's a
  signal to extend the convention plugin, not to duplicate config locally.
