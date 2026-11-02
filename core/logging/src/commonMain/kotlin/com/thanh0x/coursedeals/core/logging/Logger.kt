package com.thanh0x.coursedeals.core.logging

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

interface Logger {
    val tag: String
    fun isLoggable(level: LogLevel): Boolean
    fun log(level: LogLevel, message: String, throwable: Throwable? = null)
}

@OptIn(ExperimentalContracts::class)
inline fun Logger.verbose(throwable: Throwable? = null, message: () -> String) {
    contract { callsInPlace(message, InvocationKind.AT_MOST_ONCE) }
    if (isLoggable(LogLevel.VERBOSE)) {
        log(LogLevel.VERBOSE, message(), throwable)
    }
}

@OptIn(ExperimentalContracts::class)
inline fun Logger.debug(throwable: Throwable? = null, message: () -> String) {
    contract { callsInPlace(message, InvocationKind.AT_MOST_ONCE) }
    if (isLoggable(LogLevel.DEBUG)) {
        log(LogLevel.DEBUG, message(), throwable)
    }
}

@OptIn(ExperimentalContracts::class)
inline fun Logger.info(throwable: Throwable? = null, message: () -> String) {
    contract { callsInPlace(message, InvocationKind.AT_MOST_ONCE) }
    if (isLoggable(LogLevel.INFO)) {
        log(LogLevel.INFO, message(), throwable)
    }
}

@OptIn(ExperimentalContracts::class)
inline fun Logger.warn(throwable: Throwable? = null, message: () -> String) {
    contract { callsInPlace(message, InvocationKind.AT_MOST_ONCE) }
    if (isLoggable(LogLevel.WARN)) {
        log(LogLevel.WARN, message(), throwable)
    }
}

@OptIn(ExperimentalContracts::class)
inline fun Logger.error(throwable: Throwable? = null, message: () -> String) {
    contract { callsInPlace(message, InvocationKind.AT_MOST_ONCE) }
    if (isLoggable(LogLevel.ERROR)) {
        log(LogLevel.ERROR, message(), throwable)
    }
}
