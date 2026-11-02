package com.thanh0x.coursedeals.core.logging

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogRetentionTest {

    private lateinit var tempLogDir: File

    @BeforeTest
    fun setUp() {
        val baseDir = File(System.getProperty("java.io.tmpdir") ?: ".", "test_logs_${System.currentTimeMillis()}")
        baseDir.mkdirs()
        tempLogDir = baseDir
        LogFileManager.init(tempLogDir.absolutePath)
    }

    @AfterTest
    fun tearDown() {
        LogFileManager.clearLogs()
        tempLogDir.deleteRecursively()
    }

    @Test
    fun testPruneOldLogs_deletesFilesOlderThan3Days() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val now = System.currentTimeMillis()
        val oneDayMillis = 24L * 60L * 60L * 1000L

        val todayDate = sdf.format(Date(now))
        val yesterdayDate = sdf.format(Date(now - oneDayMillis))
        val twoDaysAgoDate = sdf.format(Date(now - 2 * oneDayMillis))
        val fiveDaysAgoDate = sdf.format(Date(now - 5 * oneDayMillis))
        val tenDaysAgoDate = sdf.format(Date(now - 10 * oneDayMillis))

        val todayFile = File(tempLogDir, "app.$todayDate.log").apply { writeText("today log\n") }
        val yesterdayFile = File(tempLogDir, "app.$yesterdayDate.log").apply { writeText("yesterday log\n") }
        val twoDaysAgoFile = File(tempLogDir, "app.$twoDaysAgoDate.log").apply { writeText("2 days ago log\n") }
        val fiveDaysAgoFile = File(tempLogDir, "app.$fiveDaysAgoDate.log").apply { writeText("5 days ago log\n") }
        val tenDaysAgoFile = File(tempLogDir, "app.$tenDaysAgoDate.log").apply { writeText("10 days ago log\n") }

        LogFileManager.pruneOldLogs(retentionDays = 3)

        assertTrue(todayFile.exists(), "Today file should be retained")
        assertTrue(yesterdayFile.exists(), "Yesterday file should be retained")
        assertTrue(twoDaysAgoFile.exists(), "2 days ago file should be retained")
        assertFalse(fiveDaysAgoFile.exists(), "5 days ago file should be pruned")
        assertFalse(tenDaysAgoFile.exists(), "10 days ago file should be pruned")
    }

    @Test
    fun testAppendAndRetrieveRecentLogs_filteringByLevel() {
        LogFileManager.clearLogs()

        val now = System.currentTimeMillis()
        LogFileManager.appendLog(
            LogEntry(now, "12:00:00.000", LogLevel.DEBUG, "TagD", "Debug log 1")
        )
        LogFileManager.appendLog(
            LogEntry(now + 10, "12:00:00.010", LogLevel.INFO, "TagI", "Info log 1")
        )
        LogFileManager.appendLog(
            LogEntry(now + 20, "12:00:00.020", LogLevel.ERROR, "TagE", "Error log 1")
        )
        LogFileManager.flush()

        val allLogs = LogFileManager.getRecentLogs(maxLines = 10, levelFilter = null)
        assertEquals(3, allLogs.size)

        val errorLogs = LogFileManager.getRecentLogs(maxLines = 10, levelFilter = LogLevel.ERROR)
        assertEquals(1, errorLogs.size)
        assertTrue(errorLogs.first().contains("Error log 1"))

        val infoLogs = LogFileManager.getRecentLogs(maxLines = 10, levelFilter = LogLevel.INFO)
        assertEquals(1, infoLogs.size)
        assertTrue(infoLogs.first().contains("Info log 1"))
    }
}
