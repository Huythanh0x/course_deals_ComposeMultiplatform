# Ticket #160: Animated coupon countdown ticker and urgency badge

## Implementation Details
- **CountdownFormatter & ExpiryUrgency**:
  - Created `CountdownFormatter` and `ExpiryUrgency` (NORMAL, WARNING, CRITICAL) in `:core:ui` (`core/ui/src/commonMain/kotlin/com/thanh0x/coursedeals/core/ui/components/CountdownFormatter.kt`).
  - Formatting boundaries:
    - `> 24h`: Formatted as `"2 days left"` or `"1 day left"` with calm/primary container styling (`ExpiryUrgency.NORMAL`).
    - `< 24h`: Formatted as `"08h 42m remaining"` with warning/errorContainer styling and gentle pulse animation (`ExpiryUrgency.WARNING`).
    - `< 1h`: Formatted as `"Expiring soon: 28m"` (or seconds when `< 1m`) with high-urgency flame badge and rapid pulse animation (`ExpiryUrgency.CRITICAL`).
    - Expired: Displays `"expired"` in surfaceVariant.
    - Unknown/null: Handled gracefully (omits display).
- **CountdownTicker & UrgencyBadge Composable**:
  - Implemented `CountdownTicker` and `UrgencyBadge` in `core/ui/src/commonMain/kotlin/com/thanh0x/coursedeals/core/ui/components/CountdownTicker.kt`.
  - Self-contained `LaunchedEffect` timer updates interval adaptively (every 1s under 1m, 10s under 1h, 30s under 24h, 60s otherwise), isolating state changes to avoid unnecessary parent recompositions.
  - Urgency badge uses Compose infinite animation for pulsing alpha and animated color transition.
  - Multiplatform string resources in `Res.string` with padding format support (`%02d`).
- **UI Integration**:
  - Integrated `CountdownTicker` into `CouponCourseCard` on `:feature:home`.
  - Integrated `CountdownTicker` into `DetailHeroHeader` on `:feature:detail`.
- **Unit Testing**:
  - Added comprehensive boundary tests in `core/ui/src/commonTest/kotlin/com/thanh0x/coursedeals/core/ui/components/CountdownFormatterTest.kt` verifying null, negative, expired, >24h, <24h, <1h, and exact edge boundaries.

## Verification & Commands Run
- `./gradlew :core:ui:jvmTest`: Passed (All CountdownFormatterTest unit tests passed).
- `./gradlew detekt`: Passed.
- `./gradlew :feature:home:jvmTest :feature:detail:jvmTest`: Passed.
- `./gradlew testLocalDebugUnitTest`: Passed (All 95 unit test tasks clean).
