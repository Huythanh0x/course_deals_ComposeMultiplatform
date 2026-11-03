# Changelog

All notable changes to this project are documented here. Format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), versioning follows
[Semantic Versioning](https://semver.org/) — see [`docs/versioning.md`](docs/versioning.md).

## [Unreleased]
### Added
- Multiplatform Compose & Kotlin Multiplatform architecture across Android (`:app`), iOS (`iosApp`), watchOS (`iosApp/WatchApp`), and Compose Desktop (`:desktopApp`) — ADR 0001, ADR 0002, ADR 0003.
- WatchOS SwiftUI companion app with `/api/v1/coupons/hot-deals` integration, student enrollment counts, hot badges, `WatchConnectivity` (`WCSession`), and Apple Handoff (#223, #229).
- Multi-platform Sentry error tracking & observability (`io.sentry:sentry-kotlin-multiplatform`), `StartupTimeMonitor`, coroutine `ViewModel` error boundary, and rolling file diagnostic logs in `:core:logging` (#157).
- Automated multi-platform Release CD pipeline (`.github/workflows/release-cd.yml` & `scripts/release-build.sh`) with R8/ProGuard size optimizations and SHA256 checksums (#211, #213, #222).
- Maestro-compatible Compose Desktop E2E UI test framework and YAML flow runner (`:desktopApp:runMaestroFlow`) (#219).
- OpenAPI Generator (`generateCourseDealsApi`) contract-first Ktor client and `kotlinx.serialization` DTO generation (#194).
- Remote app configuration (`GET /api/v1/app/config`), Force Update gate, and feature flag evaluation on Splash (#156).
- Passwordless Email OTP authentication, Terms & Privacy consent gating, and GDPR/Store-compliant account deletion (#155).
- Community deal reporting modal (`POST /api/courses/{courseId}/reports`) and FCM device token registration (`POST /api/v1/users/device-token`) (#158).

### Fixed
- Preserve Home filter and search state on Detail back navigation (#220).
- Resolve cross-platform Profile preference deserialization (`PreferenceDto`), status/navigation bar insets on Detail, and iOS Dark Mode cold-start persistence (`QA_VERIFICATION_REPORT.md`).
