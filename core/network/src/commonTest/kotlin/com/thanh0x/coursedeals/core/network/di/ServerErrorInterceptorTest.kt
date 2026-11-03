package com.thanh0x.coursedeals.core.network.di

import com.thanh0x.coursedeals.core.logging.CommonTracingInitializer
import com.thanh0x.coursedeals.core.logging.TracingReporter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServerErrorInterceptorTest {

    @BeforeTest
    fun setUp() {
        CommonTracingInitializer.resetForTesting()
        CommonTracingInitializer.initialize(
            distroChannel = "network-test",
            appVersion = "1.0.0 (105)",
        )
    }

    @AfterTest
    fun tearDown() {
        CommonTracingInitializer.resetForTesting()
    }

    @Test
    fun reportIfServerErrorRecordsOnly5xxResponses() {
        reportIfServerError(method = "GET", encodedPath = "/api/v1/courses", statusCode = 200)
        reportIfServerError(method = "POST", encodedPath = "/api/v1/auth/login", statusCode = 401)
        reportIfServerError(method = "GET", encodedPath = "/api/v1/courses/999", statusCode = 404)
        assertTrue(TracingReporter.capturedHttpErrors.isEmpty())

        reportIfServerError(method = "GET", encodedPath = "/api/v1/courses", statusCode = 500)
        reportIfServerError(method = "POST", encodedPath = "/api/v1/courses/report", statusCode = 503)

        assertEquals(2, TracingReporter.capturedHttpErrors.size)
        assertEquals(500, TracingReporter.capturedHttpErrors[0].statusCode)
        assertEquals("/api/v1/courses", TracingReporter.capturedHttpErrors[0].path)
        assertEquals(503, TracingReporter.capturedHttpErrors[1].statusCode)
        assertEquals("POST", TracingReporter.capturedHttpErrors[1].method)
    }
}
