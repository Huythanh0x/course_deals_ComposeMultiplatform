package com.thanh0x.coursedeals.core.logging

import io.sentry.kotlin.multiplatform.Sentry
import io.sentry.kotlin.multiplatform.SentryLevel
import io.sentry.kotlin.multiplatform.protocol.Breadcrumb
import io.sentry.kotlin.multiplatform.protocol.User

private const val MAX_BREADCRUMBS = 200
private const val MAX_CAPTURED_EVENTS = 100
private const val HASH_RADIX = 16
private const val HASH_HEX_LENGTH = 8
private const val FNV_OFFSET_BASIS = 0x811c9dc5L
private const val FNV_PRIME = 0x01000193L
private const val UNSIGNED_32_MASK = 0xffffffffL

data class TracingContext(
    val os: String,
    val arch: String,
    val appVersion: String,
    val distroChannel: String,
    val userId: String,
    val currentRoute: String,
    val networkType: String = "online",
)

data class TracingBreadcrumb(
    val timestamp: Long,
    val category: String,
    val message: String,
    val level: LogLevel,
    val metadata: Map<String, String> = emptyMap(),
)

data class CapturedExceptionEvent(
    val timestamp: Long,
    val throwableClass: String,
    val message: String,
    val isFatal: Boolean,
    val tags: Map<String, String>,
    val context: TracingContext,
)

data class CapturedHttpServerErrorEvent(
    val timestamp: Long,
    val method: String,
    val path: String,
    val statusCode: Int,
    val tags: Map<String, String>,
)

data class StartupMetricsReport(
    val stepsMs: Map<String, Long>,
    val totalMs: Long,
    val context: TracingContext,
)

object CommonTracingInitializer {
    private val lock = Any()
    private var initialized: Boolean = false
    private var sentrySdkActive: Boolean = false
    private var contextState = TracingContext(
        os = PlatformTracingBridge.detectOsName(),
        arch = PlatformTracingBridge.detectOsArch(),
        appVersion = "1.0.0",
        distroChannel = "unknown",
        userId = "guest",
        currentRoute = "splash",
        networkType = "online",
    )

    val isInitialized: Boolean
        get() = syncLock(lock) { initialized }

    val isSentrySdkActive: Boolean
        get() = syncLock(lock) { sentrySdkActive }

    val currentRoute: String
        get() = syncLock(lock) { contextState.currentRoute }

    val userId: String
        get() = syncLock(lock) { contextState.userId }

    val isGuest: Boolean
        get() = syncLock(lock) { contextState.userId == "guest" }

    val networkType: String
        get() = syncLock(lock) { contextState.networkType }

    fun initialize(
        distroChannel: String,
        appVersion: String,
        dsn: String = "",
        enableExternalSdk: Boolean = true,
    ) {
        LoggerFactory.install(DailyRollingFileLogWriter())
        LoggerFactory.install(TracingLogWriter)
        PlatformTracingBridge.installCrashHandler()

        val updated = syncLock(lock) {
            contextState = contextState.copy(
                os = PlatformTracingBridge.detectOsName(),
                arch = PlatformTracingBridge.detectOsArch(),
                appVersion = appVersion.ifBlank { "1.0.0" },
                distroChannel = distroChannel.ifBlank { "unknown" },
            )
            initialized = true
            contextState
        }

        val effectiveDsn = dsn.ifBlank { PlatformTracingBridge.resolveDefaultDsn() }
        if (enableExternalSdk && effectiveDsn.isNotBlank()) {
            initSentryMultiplatform(effectiveDsn, updated)
        } else {
            syncSentryScope(updated)
        }

        TracingReporter.addBreadcrumb(
            category = "lifecycle",
            message = "CommonTracingInitializer initialized",
            level = LogLevel.INFO,
            metadata = currentTags(),
        )

        AppLogger.tag("Tracing").info {
            "CommonTracingInitializer initialized (" +
                "os=${updated.os}, arch=${updated.arch}, " +
                "app_version=${updated.appVersion}, distro_channel=${updated.distroChannel}, " +
                "userId=${updated.userId})"
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun initSentryMultiplatform(dsn: String, context: TracingContext) {
        try {
            Sentry.init { options ->
                options.dsn = dsn
                options.release = context.appVersion
                options.environment = context.distroChannel
            }
            syncLock(lock) {
                sentrySdkActive = true
            }
            syncSentryScope(context)
        } catch (_: Throwable) {
            syncLock(lock) {
                sentrySdkActive = false
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    internal fun syncSentryScope(context: TracingContext) {
        if (!isSentrySdkActive) return
        try {
            Sentry.configureScope { scope ->
                scope.setTag("os", context.os)
                scope.setTag("arch", context.arch)
                scope.setTag("app_version", context.appVersion)
                scope.setTag("distro_channel", context.distroChannel)
                scope.setTag("userId", context.userId)
                scope.setTag("current_route", context.currentRoute)
                scope.user = User().apply {
                    id = context.userId
                }
            }
        } catch (_: Throwable) {
        }
    }

    fun setUserId(rawUserId: String?) {
        val sanitized = sanitizeUserId(rawUserId)
        val updated = syncLock(lock) {
            if (contextState.userId == sanitized) return
            contextState = contextState.copy(userId = sanitized)
            contextState
        }
        syncSentryScope(updated)
        TracingReporter.addBreadcrumb(
            category = "auth",
            message = "User context updated to $sanitized",
            level = LogLevel.INFO,
            metadata = mapOf("userId" to sanitized),
        )
    }

    fun setCurrentRoute(route: String) {
        val trimmed = route.trim()
        if (trimmed.isEmpty()) return
        val updated = syncLock(lock) {
            if (contextState.currentRoute == trimmed) return
            contextState = contextState.copy(currentRoute = trimmed)
            contextState
        }
        syncSentryScope(updated)
        TracingReporter.addBreadcrumb(
            category = "navigation",
            message = "Navigated to $trimmed",
            level = LogLevel.INFO,
            metadata = mapOf("route" to trimmed),
        )
    }

    fun setNetworkType(type: String) {
        val trimmed = type.trim().ifEmpty { "unknown" }
        val updated = syncLock(lock) {
            if (contextState.networkType == trimmed) return
            contextState = contextState.copy(networkType = trimmed)
            contextState
        }
        syncSentryScope(updated)
    }

    fun currentContext(): TracingContext = syncLock(lock) { contextState }

    fun currentTags(): Map<String, String> {
        val ctx = currentContext()
        return mapOf(
            "os" to ctx.os,
            "arch" to ctx.arch,
            "app_version" to ctx.appVersion,
            "distro_channel" to ctx.distroChannel,
            "userId" to ctx.userId,
            "current_route" to ctx.currentRoute,
        )
    }

    internal fun sanitizeUserId(rawUserId: String?): String {
        val trimmed = rawUserId?.trim().orEmpty()
        if (trimmed.isEmpty() || trimmed.equals("guest", ignoreCase = true)) {
            return "guest"
        }
        val isNumericId = trimmed.all { it.isDigit() }
        return if (isNumericId) {
            "user_$trimmed"
        } else {
            "user_${fnv1a32Hex(trimmed)}"
        }
    }

    private fun fnv1a32Hex(input: String): String {
        var hash = FNV_OFFSET_BASIS
        for (ch in input) {
            hash = hash xor ch.code.toLong()
            hash = (hash * FNV_PRIME) and UNSIGNED_32_MASK
        }
        return hash.toString(HASH_RADIX).padStart(HASH_HEX_LENGTH, '0')
    }

    fun resetForTesting() {
        syncLock(lock) {
            initialized = false
            sentrySdkActive = false
            contextState = TracingContext(
                os = PlatformTracingBridge.detectOsName(),
                arch = PlatformTracingBridge.detectOsArch(),
                appVersion = "1.0.0",
                distroChannel = "unknown",
                userId = "guest",
                currentRoute = "splash",
                networkType = "online",
            )
        }
        TracingReporter.clearForTesting()
    }
}

object TracingReporter {
    private val lock = Any()
    private val breadcrumbsList = ArrayDeque<TracingBreadcrumb>(MAX_BREADCRUMBS)
    private val exceptionsList = ArrayDeque<CapturedExceptionEvent>(MAX_CAPTURED_EVENTS)
    private val httpErrorsList = ArrayDeque<CapturedHttpServerErrorEvent>(MAX_CAPTURED_EVENTS)
    private var startupReport: StartupMetricsReport? = null

    val breadcrumbs: List<TracingBreadcrumb>
        get() = syncLock(lock) { breadcrumbsList.toList() }

    val capturedExceptions: List<CapturedExceptionEvent>
        get() = syncLock(lock) { exceptionsList.toList() }

    val capturedHttpErrors: List<CapturedHttpServerErrorEvent>
        get() = syncLock(lock) { httpErrorsList.toList() }

    val lastStartupMetrics: StartupMetricsReport?
        get() = syncLock(lock) { startupReport }

    fun addBreadcrumb(
        category: String,
        message: String,
        level: LogLevel = LogLevel.INFO,
        metadata: Map<String, String> = emptyMap(),
    ) {
        val breadcrumb = TracingBreadcrumb(
            timestamp = currentTimeMillis(),
            category = category,
            message = message,
            level = level,
            metadata = metadata,
        )
        syncLock(lock) {
            if (breadcrumbsList.size >= MAX_BREADCRUMBS) {
                breadcrumbsList.removeFirst()
            }
            breadcrumbsList.addLast(breadcrumb)
        }
        forwardBreadcrumbToSentry(breadcrumb)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun forwardBreadcrumbToSentry(breadcrumb: TracingBreadcrumb) {
        if (!CommonTracingInitializer.isSentrySdkActive) return
        try {
            val sentryBreadcrumb = Breadcrumb().apply {
                category = breadcrumb.category
                message = breadcrumb.message
                level = when (breadcrumb.level) {
                    LogLevel.VERBOSE, LogLevel.DEBUG -> SentryLevel.DEBUG
                    LogLevel.INFO -> SentryLevel.INFO
                    LogLevel.WARN -> SentryLevel.WARNING
                    LogLevel.ERROR, LogLevel.NONE -> SentryLevel.ERROR
                }
                breadcrumb.metadata.forEach { (key, value) ->
                    setData(key, value)
                }
            }
            Sentry.addBreadcrumb(sentryBreadcrumb)
        } catch (_: Throwable) {
        }
    }

    fun captureException(
        throwable: Throwable,
        tags: Map<String, String> = emptyMap(),
        isFatal: Boolean = false,
    ) {
        val context = CommonTracingInitializer.currentContext()
        val mergedTags = CommonTracingInitializer.currentTags() + tags
        val event = CapturedExceptionEvent(
            timestamp = currentTimeMillis(),
            throwableClass = throwable::class.simpleName ?: "Throwable",
            message = throwable.message ?: "",
            isFatal = isFatal,
            tags = mergedTags,
            context = context,
        )
        syncLock(lock) {
            if (exceptionsList.size >= MAX_CAPTURED_EVENTS) {
                exceptionsList.removeFirst()
            }
            exceptionsList.addLast(event)
        }
        addBreadcrumb(
            category = if (isFatal) "crash" else "exception",
            message = "${event.throwableClass}: ${event.message}",
            level = LogLevel.ERROR,
            metadata = mergedTags,
        )
        forwardExceptionToSentry(throwable, event)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun forwardExceptionToSentry(throwable: Throwable, event: CapturedExceptionEvent) {
        if (!CommonTracingInitializer.isSentrySdkActive) return
        try {
            Sentry.configureScope { scope ->
                scope.level = if (event.isFatal) SentryLevel.FATAL else SentryLevel.ERROR
                event.tags.forEach { (key, value) ->
                    scope.setTag(key, value)
                }
            }
            Sentry.captureException(throwable)
        } catch (_: Throwable) {
        }
    }

    fun captureHttpServerError(
        method: String,
        path: String,
        statusCode: Int,
    ) {
        val sanitizedPath = path.substringBefore('?')
        val tags = CommonTracingInitializer.currentTags() + mapOf(
            "http.method" to method,
            "http.path" to sanitizedPath,
            "http.status_code" to statusCode.toString(),
        )
        val event = CapturedHttpServerErrorEvent(
            timestamp = currentTimeMillis(),
            method = method,
            path = sanitizedPath,
            statusCode = statusCode,
            tags = tags,
        )
        syncLock(lock) {
            if (httpErrorsList.size >= MAX_CAPTURED_EVENTS) {
                httpErrorsList.removeFirst()
            }
            httpErrorsList.addLast(event)
        }
        addBreadcrumb(
            category = "http.5xx",
            message = "HTTP $statusCode on $method $sanitizedPath",
            level = LogLevel.ERROR,
            metadata = mapOf(
                "http.method" to method,
                "http.path" to sanitizedPath,
                "http.status_code" to statusCode.toString(),
            ),
        )
        AppLogger.tag("TracingHttp").warn {
            "Captured HTTP 5xx server error: method=$method, path=$sanitizedPath, status=$statusCode"
        }
    }

    fun recordStartupMetrics(
        stepsMs: Map<String, Long>,
        totalMs: Long,
    ): StartupMetricsReport {
        val report = StartupMetricsReport(
            stepsMs = stepsMs,
            totalMs = totalMs,
            context = CommonTracingInitializer.currentContext(),
        )
        syncLock(lock) {
            startupReport = report
        }
        addBreadcrumb(
            category = "startup",
            message = "ColdStart complete in ${totalMs}ms",
            level = LogLevel.INFO,
            metadata = stepsMs.mapValues { "${it.value}ms" } + ("total_ms" to "${totalMs}ms"),
        )
        return report
    }

    internal fun clearForTesting() {
        syncLock(lock) {
            breadcrumbsList.clear()
            exceptionsList.clear()
            httpErrorsList.clear()
            startupReport = null
        }
    }
}

internal object TracingLogWriter : LogWriter {
    private const val INTERNAL_TRACING_TAG_PREFIX = "Tracing"

    override fun write(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        if (level.priority < LogLevel.WARN.priority) return
        if (tag.startsWith(INTERNAL_TRACING_TAG_PREFIX)) return

        TracingReporter.addBreadcrumb(
            category = "log.${tag.lowercase()}",
            message = message,
            level = level,
            metadata = buildMap {
                put("tag", tag)
                put("level", level.name)
                if (throwable != null) {
                    put("exception", throwable::class.simpleName ?: "Throwable")
                }
            },
        )
    }
}
