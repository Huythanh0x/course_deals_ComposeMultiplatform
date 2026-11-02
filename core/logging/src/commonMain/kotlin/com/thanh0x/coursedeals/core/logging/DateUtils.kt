package com.thanh0x.coursedeals.core.logging

expect fun currentTimeMillis(): Long

expect fun formatLogDate(epochMillis: Long): String

expect fun formatLogTimestamp(epochMillis: Long): String

expect fun throwableToStackTraceString(throwable: Throwable): String
