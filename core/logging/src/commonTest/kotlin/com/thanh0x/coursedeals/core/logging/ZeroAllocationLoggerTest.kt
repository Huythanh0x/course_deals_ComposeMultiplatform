package com.thanh0x.coursedeals.core.logging

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ZeroAllocationLoggerTest {

    private class TestLogWriter : LogWriter {
        val entries = mutableListOf<String>()

        override fun write(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
            entries.add("[${level.shortCode}] [$tag] $message")
        }
    }

    private lateinit var writer: TestLogWriter

    @BeforeTest
    fun setUp() {
        writer = TestLogWriter()
        LoggerFactory.clearWriters()
        LoggerFactory.install(writer)
    }

    @AfterTest
    fun tearDown() {
        LoggerFactory.clearWriters()
        LoggerFactory.minLogLevel = LogLevel.VERBOSE
    }

    @Test
    fun testZeroAllocation_whenLogLevelIsInfo_debugLambdaIsNotEvaluated() {
        LoggerFactory.minLogLevel = LogLevel.INFO
        val logger = LoggerFactory.getLogger("TestZeroAlloc")

        var debugLambdaInvoked = false
        var verboseLambdaInvoked = false

        logger.verbose {
            verboseLambdaInvoked = true
            "Expensive verbose computation"
        }

        logger.debug {
            debugLambdaInvoked = true
            "Expensive debug computation"
        }

        assertFalse(verboseLambdaInvoked, "Verbose lambda must not be invoked when minLogLevel is INFO")
        assertFalse(debugLambdaInvoked, "Debug lambda must not be invoked when minLogLevel is INFO")
        assertEquals(0, writer.entries.size)
    }

    @Test
    fun testZeroAllocation_whenLogLevelIsInfo_infoAndWarnAndErrorAreEvaluated() {
        LoggerFactory.minLogLevel = LogLevel.INFO
        val logger = LoggerFactory.getLogger("TestZeroAlloc")

        var infoLambdaInvoked = false
        var warnLambdaInvoked = false
        var errorLambdaInvoked = false

        logger.info {
            infoLambdaInvoked = true
            "Info message"
        }

        logger.warn {
            warnLambdaInvoked = true
            "Warn message"
        }

        logger.error {
            errorLambdaInvoked = true
            "Error message"
        }

        assertTrue(infoLambdaInvoked, "Info lambda must be evaluated when minLogLevel is INFO")
        assertTrue(warnLambdaInvoked, "Warn lambda must be evaluated when minLogLevel is INFO")
        assertTrue(errorLambdaInvoked, "Error lambda must be evaluated when minLogLevel is INFO")
        assertEquals(3, writer.entries.size)
    }

    @Test
    fun testAppLogger_inlineExtensions() {
        LoggerFactory.minLogLevel = LogLevel.DEBUG
        var invoked = false

        AppLogger.debug {
            invoked = true
            "AppLogger test message"
        }

        assertTrue(invoked)
        assertEquals(1, writer.entries.size)
        assertTrue(writer.entries.first().contains("AppLogger test message"))
    }

    @Test
    fun testLogLevelNone_disablesAllLogging() {
        LoggerFactory.minLogLevel = LogLevel.NONE
        val logger = LoggerFactory.getLogger("DisabledLogger")

        var invoked = false
        logger.error {
            invoked = true
            "Fatal error message"
        }

        assertFalse(invoked)
        assertEquals(0, writer.entries.size)
    }
}
