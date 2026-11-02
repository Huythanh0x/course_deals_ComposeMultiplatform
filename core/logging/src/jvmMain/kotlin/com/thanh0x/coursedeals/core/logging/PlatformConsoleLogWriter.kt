package com.thanh0x.coursedeals.core.logging

import java.lang.reflect.Method

class PlatformConsoleLogWriter : LogWriter {
    private val printlnMethod: Method? by lazy {
        try {
            val logClass = Class.forName("android.util.Log")
            logClass.getMethod(
                "println",
                Int::class.javaPrimitiveType,
                String::class.java,
                String::class.java
            )
        } catch (_: Throwable) {
            null
        }
    }

    override fun write(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        val fullMessage = if (throwable != null) {
            "$message\n${throwableToStackTraceString(throwable)}"
        } else {
            message
        }

        val androidPrintln = printlnMethod
        if (androidPrintln != null) {
            try {
                androidPrintln.invoke(null, level.priority, tag, fullMessage)
            } catch (_: Throwable) {
                printFallback(level, tag, fullMessage)
            }
        } else {
            printFallback(level, tag, fullMessage)
        }
    }

    private fun printFallback(level: LogLevel, tag: String, fullMessage: String) {
        val line = "[${formatLogTimestamp(currentTimeMillis())}] [${level.shortCode}] [$tag] $fullMessage"
        if (level.priority >= LogLevel.WARN.priority) {
            System.err.println(line)
        } else {
            println(line)
        }
    }
}
