package com.thanh0x.coursedeals.core.common

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun platformCurrentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()
