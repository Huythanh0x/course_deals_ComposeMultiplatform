package com.thanh0x.coursedeals.core.logging

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TracingAndStartupMonitorTest {

    @BeforeTest
    fun setUp() {
        LoggerFactory.clearWriters()
        LoggerFactory.minLogLevel = LogLevel.VERBOSE
        LogFileManager.init(null)
        LogFileManager.clearLogs()
        CommonTracingInitializer.resetForTesting()
        StartupTimeMonitor.resetForTesting()
    }

    @AfterTest
    fun tearDown() {
        CommonTracingInitializer.resetForTesting()
        StartupTimeMonitor.resetForTesting()
    }

    @Test
    fun commonTracingInitializerPopulatesPlatformMetadataAndSanitizesUserId() {
        CommonTracingInitializer.initialize(
            distroChannel = "local",
            appVersion = "1.2.3 (105)",
        )

        assertTrue(CommonTracingInitializer.isInitialized)
        assertTrue(CommonTracingInitializer.isGuest)
        val tags = CommonTracingInitializer.currentTags()
        assertTrue(tags["os"].orEmpty().isNotBlank())
        assertTrue(tags["arch"].orEmpty().isNotBlank())
        assertEquals("1.2.3 (105)", tags["app_version"])
        assertEquals("local", tags["distro_channel"])
        assertEquals("guest", tags["userId"])

        // Numeric user ID is prefixed without exposing raw email/PII
        CommonTracingInitializer.setUserId("42")
        assertFalse(CommonTracingInitializer.isGuest)
        assertEquals("user_42", CommonTracingInitializer.userId)

        // Email address is hashed so raw PII is never stored in tracing tags
        CommonTracingInitializer.setUserId("alice@coursedeals.com")
        val hashedId = CommonTracingInitializer.userId
        assertTrue(hashedId.startsWith("user_"))
        assertFalse(hashedId.contains("alice"))
        assertFalse(hashedId.contains("@"))

        // Null or blank resets back to guest
        CommonTracingInitializer.setUserId(null)
        assertTrue(CommonTracingInitializer.isGuest)
        assertEquals("guest", CommonTracingInitializer.userId)

        val formattedLogs = LogFileManager.getFormattedLogs()
        assertTrue(
            formattedLogs.contains("CommonTracingInitializer initialized"),
            "Expected initialization log in LogFileManager, got: $formattedLogs",
        )
    }

    @Test
    fun tracingReporterRecordsBreadcrumbsExceptionsAndHttp5xxServerErrors() {
        CommonTracingInitializer.initialize(
            distroChannel = "desktop",
            appVersion = "1.0.0 (105)",
        )
        CommonTracingInitializer.setCurrentRoute("home")

        // 1. WARN/ERROR logs from AppLogger automatically record breadcrumbs
        AppLogger.tag("HomeRepo").warn { "Slow response from cache" }
        assertTrue(
            TracingReporter.breadcrumbs.any {
                it.category == "log.homerepo" && it.message.contains("Slow response from cache")
            }
        )

        // 2. Non-fatal exception capture attaches platform & custom tags
        val boom = IllegalStateException("Coupon parse failure")
        TracingReporter.captureException(
            throwable = boom,
            tags = mapOf("feature" to "home"),
            isFatal = false,
        )
        val captured = TracingReporter.capturedExceptions.lastOrNull()
        assertNotNull(captured)
        assertEquals("IllegalStateException", captured.throwableClass)
        assertEquals("Coupon parse failure", captured.message)
        assertFalse(captured.isFatal)
        assertEquals("home", captured.tags["feature"])
        assertEquals("home", captured.tags["current_route"])
        assertEquals("desktop", captured.tags["distro_channel"])

        // 3. HTTP 5xx server error capture strips query strings and records metadata
        TracingReporter.captureHttpServerError(
            method = "GET",
            path = "/api/v1/courses?token=secret123&page=1",
            statusCode = 503,
        )
        val httpError = TracingReporter.capturedHttpErrors.lastOrNull()
        assertNotNull(httpError)
        assertEquals("GET", httpError.method)
        assertEquals("/api/v1/courses", httpError.path)
        assertFalse(httpError.path.contains("secret123"))
        assertEquals(503, httpError.statusCode)
        assertEquals("503", httpError.tags["http.status_code"])
    }

    @Test
    fun startupTimeMonitorTracksAllFourStepsAndIsIdempotentOnFirstFrame() {
        CommonTracingInitializer.initialize(
            distroChannel = "desktop",
            appVersion = "1.0.0 (105)",
        )

        StartupTimeMonitor.onAppStart()
        StartupTimeMonitor.trackStep(StartupTimeMonitor.StepName.Logging) {
            // Simulated logging init
        }
        StartupTimeMonitor.trackStep(StartupTimeMonitor.StepName.KoinModules) {
            // Simulated Koin init
        }
        StartupTimeMonitor.trackStep(StartupTimeMonitor.StepName.AuthSession) {
            // Simulated auth session check
        }
        StartupTimeMonitor.startStep(StartupTimeMonitor.StepName.FirstFrame)

        val firstReport = StartupTimeMonitor.onFirstFrameRendered()
        val secondReport = StartupTimeMonitor.onFirstFrameRendered()

        assertTrue(StartupTimeMonitor.isFirstFrameReported())
        assertSame(firstReport, secondReport)
        assertEquals(firstReport, TracingReporter.lastStartupMetrics)

        val steps = firstReport.stepsMs
        assertTrue(steps.containsKey("logging"))
        assertTrue(steps.containsKey("koin_modules"))
        assertTrue(steps.containsKey("auth_session"))
        assertTrue(steps.containsKey("first_frame"))
        assertTrue(firstReport.totalMs >= 0L)

        val formattedLogs = LogFileManager.getFormattedLogs()
        assertTrue(
            formattedLogs.contains("ColdStart complete in"),
            "Expected ColdStart summary in LogFileManager, got: $formattedLogs",
        )
        assertTrue(formattedLogs.contains("logging="))
        assertTrue(formattedLogs.contains("koin_modules="))
        assertTrue(formattedLogs.contains("auth_session="))
        assertTrue(formattedLogs.contains("first_frame="))
    }

    @Test
    fun sentryMultiplatformSdkIntegrationSyncsScopeBreadcrumbsAndFatalExceptionsSafely() {
        CommonTracingInitializer.initialize(
            distroChannel = "ios",
            appVersion = "1.0.0 (105)",
            dsn = "https://public@o0.ingest.sentry.io/1",
            enableExternalSdk = false,
        )
        assertFalse(CommonTracingInitializer.isSentrySdkActive)

        CommonTracingInitializer.setUserId("42")
        CommonTracingInitializer.setCurrentRoute("detail/7")
        CommonTracingInitializer.setNetworkType("wifi")

        val fatalError = IllegalStateException("Simulated unhandled crash")
        TracingReporter.captureException(
            throwable = fatalError,
            tags = mapOf("mechanism" to "ios_unhandled_exception_hook"),
            isFatal = true,
        )

        val captured = TracingReporter.capturedExceptions.lastOrNull()
        assertNotNull(captured)
        assertTrue(captured.isFatal)
        assertEquals("user_42", captured.context.userId)
        assertEquals("detail/7", captured.context.currentRoute)
        assertEquals("wifi", captured.context.networkType)
        assertEquals("ios_unhandled_exception_hook", captured.tags["mechanism"])
        assertTrue(
            TracingReporter.breadcrumbs.any {
                it.category == "crash" && it.message.contains("Simulated unhandled crash")
            }
        )
    }
}
