package com.thanh0x.coursedeals.core.logging

object AndroidCrashHandler {
    private var installed = false

    fun install() {
        if (installed) return
        installed = true
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
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
