package com.thanh0x.coursedeals.core.logging

expect inline fun <R> syncLock(lock: Any, block: () -> R): R
