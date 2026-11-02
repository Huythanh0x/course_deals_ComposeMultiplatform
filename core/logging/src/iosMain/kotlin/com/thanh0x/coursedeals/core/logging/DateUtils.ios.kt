package com.thanh0x.coursedeals.core.logging

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()

actual fun formatLogDate(epochMillis: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970(epochMillis / 1000.0)
    val formatter = NSDateFormatter().apply {
        dateFormat = "yyyy-MM-dd"
    }
    return formatter.stringFromDate(date)
}

actual fun formatLogTimestamp(epochMillis: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970(epochMillis / 1000.0)
    val formatter = NSDateFormatter().apply {
        dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
    }
    return formatter.stringFromDate(date)
}

actual fun throwableToStackTraceString(throwable: Throwable): String {
    return throwable.stackTraceToString()
}
