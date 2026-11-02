package com.thanh0x.coursedeals.core.common

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

private const val MILLIS_PER_SECOND = 1000.0

actual fun platformCurrentTimeMillis(): Long =
    (NSDate().timeIntervalSince1970 * MILLIS_PER_SECOND).toLong()
