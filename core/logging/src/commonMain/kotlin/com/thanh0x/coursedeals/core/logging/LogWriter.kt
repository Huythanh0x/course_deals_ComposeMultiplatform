package com.thanh0x.coursedeals.core.logging

interface LogWriter {
    fun write(level: LogLevel, tag: String, message: String, throwable: Throwable?)
    fun flush() {}
}
