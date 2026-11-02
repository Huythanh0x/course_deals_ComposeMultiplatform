package com.thanh0x.coursedeals.core.logging

actual inline fun <R> syncLock(lock: Any, block: () -> R): R = synchronized(lock, block)
