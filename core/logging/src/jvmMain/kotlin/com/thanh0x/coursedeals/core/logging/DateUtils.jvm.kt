package com.thanh0x.coursedeals.core.logging

import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun formatLogDate(epochMillis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return sdf.format(Date(epochMillis))
}

actual fun formatLogTimestamp(epochMillis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    return sdf.format(Date(epochMillis))
}

actual fun throwableToStackTraceString(throwable: Throwable): String {
    val sw = StringWriter()
    throwable.printStackTrace(PrintWriter(sw))
    return sw.toString().trimEnd()
}
