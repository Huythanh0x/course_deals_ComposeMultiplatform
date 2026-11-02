package com.thanh0x.coursedeals.core.logging

import platform.Foundation.NSLog

class DarwinLogWriter : LogWriter {
    override fun write(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        val fullMessage = if (throwable != null) {
            "$message\n${throwableToStackTraceString(throwable)}"
        } else {
            message
        }
        NSLog("[%s] [%s] %s", level.shortCode, tag, fullMessage)
    }
}
