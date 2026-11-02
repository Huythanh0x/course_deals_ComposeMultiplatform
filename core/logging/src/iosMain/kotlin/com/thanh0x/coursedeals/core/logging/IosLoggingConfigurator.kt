package com.thanh0x.coursedeals.core.logging

object IosLoggingConfigurator {
    fun install(minLevel: LogLevel = LogLevel.DEBUG) {
        LoggerFactory.minLogLevel = minLevel
        LoggerFactory.install(DarwinLogWriter())
        LoggerFactory.install(DailyRollingFileLogWriter())
        LogFileManager.init()
    }

    fun flush() {
        LogFileManager.flush()
        LoggerFactory.flush()
    }
}
