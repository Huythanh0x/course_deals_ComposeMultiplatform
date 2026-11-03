# 0003: Native SwiftUI and Shared KMP Domain Architecture for watchOS Companion App

## Status
Accepted

## Context
Course Deals surfaces time-critical 100% discount coupons across online learning platforms (Udemy, Coursera). Because high-demand promotional coupons have strict redemption caps and tight expiration windows (often 1–4 hours), users need immediate, high-glanceability notifications and quick action triggers.

An Apple Watch companion app solves this need by putting flash deals right on the user's wrist. Key interactions include:
1. Viewing top 5 expiring deals with real-time countdown tickers and platform badges.
2. Fluid Digital Crown scrolling for rapid one-handed navigation.
3. Tap-to-copy coupon codes with wrist haptic feedback.
4. Seamlessly handing off a deal from Apple Watch to iPhone or Mac via Apple Handoff (`NSUserActivity`).

From an engineering perspective:
- Compose Multiplatform targets Android, iOS, Desktop (JVM), and Web (Wasm/JS), but does **not** support watchOS. Apple's watchOS environment enforces strict WatchKit and SwiftUI runtime constraints.
- Duplicating domain models, business logic, expiration parsers, and API contracts in Swift creates maintenance overhead, divergence risk, and double implementation efforts.

## Decision
We adopt a **Native SwiftUI + Shared Kotlin Multiplatform (KMP)** hybrid architecture:

1. **Native SwiftUI UI Layer (`iosApp/WatchApp/`)**:
   - Write watchOS UI strictly in native SwiftUI to exploit platform-native APIs: `digitalCrownRotation`, `ProgressView`, `WKInterfaceDevice` haptic cues, and Apple Handoff (`NSUserActivity` type `com.coursedeals.deal.open`).
   - Keep views concise, glanceable, and battery-friendly (`WatchDealListView`, `WatchDealDetailView`).

2. **Shared KMP Domain & Data Layer (`:core:common`, `:domain:coupons`, `:data:coupons`, `:core:network`)**:
   - Configure `watchosArm64()` and `watchosSimulatorArm64()` native targets in `KmpLibraryConventionPlugin`.
   - Generalize shared Darwin/Apple code from `iosMain` into `appleMain` so shared data structures (`TimeLeft`, `Coupon`, Ktor Darwin client) compile seamlessly across both iOS and watchOS targets.
   - Compose-dependent feature modules (`core:ui`, `feature:*`, `:shared`) remain dedicated to iOS Phone, Android, and Desktop, avoiding watchOS symbol incompatibilities.

## Consequences
- **Ecosystem Continuity**: Supports standard Apple Handoff (`com.coursedeals.deal.open`), allowing users to tap "Open on iPhone" on their watch and immediately resume enrollment on their phone or desktop browser.
- **Glanceability & Performance**: SwiftUI on watchOS runs at full 60fps with native Digital Crown physics and instant haptic feedback.
- **Single Source of Truth**: Coupon entity schemas, countdown duration calculations, and API contracts remain unified in the shared Kotlin Multiplatform domain.
- **Trade-off (Two UI Stacks)**: Watch UI requires Swift/SwiftUI development, while phone/desktop uses Compose Multiplatform. This is an intentional and unavoidable trade-off due to Apple watchOS platform restrictions.
- **Tooling & Build Times**: Kotlin/Native toolchain compiles `watchosArm64` and `watchosSimulatorArm64` targets, requiring watchOS SDKs during full multiplatform builds.
