package com.thanh0x.coursedeals.core.logging

class DailyRollingFileLogWriter : LogWriter {
    override fun write(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        val now = currentTimeMillis()
        val entry = LogEntry(
            timestamp = now,
            formattedTime = formatLogTimestamp(now),
            level = level,
            tag = tag,
            message = message,
            throwableString = throwable?.let { throwableToStackTraceString(it) }
        )
        LogFileManager.appendLog(entry)
    }

    override fun flush() {
        LogFileManager.flush()
    }
}
