package com.thanh0x.coursedeals.core.logging

import java.io.File

object AndroidLogger {
    fun install(logDir: File? = null, minLevel: LogLevel = LogLevel.DEBUG) {
        if (logDir != null) {
            LogFileManager.init(logDir.absolutePath)
        }
        LoggerFactory.minLogLevel = minLevel
        LoggerFactory.install(PlatformConsoleLogWriter())
        LoggerFactory.install(DailyRollingFileLogWriter())
        AndroidCrashHandler.install()
    }
}
