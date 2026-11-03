@file:Suppress("MatchingDeclarationName")

package com.thanh0x.coursedeals.core.logging

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ReportUnhandledExceptionHook
import kotlin.native.setUnhandledExceptionHook
import kotlin.native.terminateWithUnhandledException
import platform.Foundation.NSBundle
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIDevice

@OptIn(ExperimentalNativeApi::class)
internal actual object PlatformTracingBridge {
    private val lock = Any()
    private var crashHookInstalled = false

    actual fun detectOsName(): String {
        val device = UIDevice.currentDevice
        return "${device.systemName} ${device.systemVersion}"
    }

    actual fun detectOsArch(): String = "arm64"

    actual fun resolveDefaultDsn(): String {
        val envDsn = (NSProcessInfo.processInfo.environment["SENTRY_DSN"] as? String)
            ?.trim()
            .orEmpty()
        if (envDsn.isNotEmpty()) {
            return envDsn
        }
        return (NSBundle.mainBundle.objectForInfoDictionaryKey("SENTRY_DSN") as? String)
            ?.trim()
            .orEmpty()
    }

    @Suppress("TooGenericExceptionCaught")
    actual fun installCrashHandler() {
        val shouldInstall = syncLock(lock) {
            if (crashHookInstalled) {
                false
            } else {
                crashHookInstalled = true
                true
            }
        }
        if (!shouldInstall) return

        var previousHook: ReportUnhandledExceptionHook? = null
        previousHook = setUnhandledExceptionHook { throwable ->
            try {
                val logger = LoggerFactory.getLogger("CrashHandler")
                logger.error(throwable) { "FATAL UNCAUGHT EXCEPTION on iOS" }
                TracingReporter.captureException(
                    throwable = throwable,
                    tags = mapOf(
                        "mechanism" to "ios_unhandled_exception_hook",
                    ),
                    isFatal = true,
                )
                LogFileManager.flush()
                LoggerFactory.flush()
            } catch (_: Throwable) {
                // Never throw inside unhandled exception hook
            } finally {
                val delegated = previousHook
                if (delegated != null) {
                    delegated(throwable)
                } else {
                    terminateWithUnhandledException(throwable)
                }
            }
        }
    }
}
