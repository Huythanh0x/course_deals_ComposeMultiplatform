package com.thanh0x.coursedeals.core.logging

object AndroidCrashHandler {
    private var installed = false

    fun install() {
        if (installed) return
        installed = true
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                TracingReporter.captureException(
                    throwable = throwable,
                    tags = mapOf(
                        "mechanism" to "uncaught_thread",
                        "thread" to thread.name,
                    ),
                    isFatal = true,
                )
                AppLogger.error(throwable) {
                    "FATAL UNCAUGHT EXCEPTION on thread ${thread.name}: ${throwable.message}"
                }
                LogFileManager.flush()
                LoggerFactory.flush()
            } catch (_: Throwable) {
            } finally {
                previousHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
