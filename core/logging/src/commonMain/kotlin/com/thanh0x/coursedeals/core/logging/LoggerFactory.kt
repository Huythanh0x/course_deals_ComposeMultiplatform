package com.thanh0x.coursedeals.core.logging

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass

object LoggerFactory {
    private val lock = Any()
    private val loggers = mutableMapOf<String, Logger>()
    private val writersList = mutableListOf<LogWriter>()

    var minLogLevel: LogLevel = LogLevel.VERBOSE

    val writers: List<LogWriter>
        get() = syncLock(lock) { writersList.toList() }

    fun install(writer: LogWriter) {
        syncLock(lock) {
            if (!writersList.contains(writer)) {
                writersList.add(writer)
            }
        }
    }

    fun removeWriter(writer: LogWriter) {
        syncLock(lock) {
            writersList.remove(writer)
        }
    }

    fun clearWriters() {
        syncLock(lock) {
            writersList.clear()
        }
    }

    fun getLogger(tag: String): Logger {
        return syncLock(lock) {
            loggers.getOrPut(tag) { DefaultLogger(tag) }
        }
    }

    fun getLogger(clazz: KClass<*>): Logger {
        return getLogger(clazz.simpleName ?: "Anonymous")
    }

    fun flush() {
        val currentWriters = writers
        for (i in currentWriters.indices) {
            currentWriters[i].flush()
        }
    }
}

internal class DefaultLogger(override val tag: String) : Logger {
    override fun isLoggable(level: LogLevel): Boolean {
        return level != LogLevel.NONE && level.priority >= LoggerFactory.minLogLevel.priority
    }

    override fun log(level: LogLevel, message: String, throwable: Throwable?) {
        if (!isLoggable(level)) return
        val currentWriters = LoggerFactory.writers
        for (i in currentWriters.indices) {
            currentWriters[i].write(level, tag, message, throwable)
        }
    }
}

object AppLogger : Logger {
    private const val DEFAULT_TAG = "CourseDeals"
    private val rootLogger: Logger get() = LoggerFactory.getLogger(DEFAULT_TAG)

    override val tag: String get() = DEFAULT_TAG

    override fun isLoggable(level: LogLevel): Boolean = rootLogger.isLoggable(level)

    override fun log(level: LogLevel, message: String, throwable: Throwable?) {
        rootLogger.log(level, message, throwable)
    }

    fun tag(customTag: String): Logger = LoggerFactory.getLogger(customTag)

    fun v(message: String) = log(LogLevel.VERBOSE, message)
    fun d(message: String) = log(LogLevel.DEBUG, message)
    fun i(message: String) = log(LogLevel.INFO, message)
    fun w(message: String) = log(LogLevel.WARN, message)
    fun e(message: String) = log(LogLevel.ERROR, message)
    fun e(throwable: Throwable?, message: String) = log(LogLevel.ERROR, message, throwable)

    @OptIn(ExperimentalContracts::class)
    inline fun verbose(throwable: Throwable? = null, message: () -> String) {
        contract { callsInPlace(message, InvocationKind.AT_MOST_ONCE) }
        if (isLoggable(LogLevel.VERBOSE)) {
            log(LogLevel.VERBOSE, message(), throwable)
        }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun debug(throwable: Throwable? = null, message: () -> String) {
        contract { callsInPlace(message, InvocationKind.AT_MOST_ONCE) }
        if (isLoggable(LogLevel.DEBUG)) {
            log(LogLevel.DEBUG, message(), throwable)
        }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun info(throwable: Throwable? = null, message: () -> String) {
        contract { callsInPlace(message, InvocationKind.AT_MOST_ONCE) }
        if (isLoggable(LogLevel.INFO)) {
            log(LogLevel.INFO, message(), throwable)
        }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun warn(throwable: Throwable? = null, message: () -> String) {
        contract { callsInPlace(message, InvocationKind.AT_MOST_ONCE) }
        if (isLoggable(LogLevel.WARN)) {
            log(LogLevel.WARN, message(), throwable)
        }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun error(throwable: Throwable? = null, message: () -> String) {
        contract { callsInPlace(message, InvocationKind.AT_MOST_ONCE) }
        if (isLoggable(LogLevel.ERROR)) {
            log(LogLevel.ERROR, message(), throwable)
        }
    }
}
