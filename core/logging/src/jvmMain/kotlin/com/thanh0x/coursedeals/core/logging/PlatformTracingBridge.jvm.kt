@file:Suppress("MatchingDeclarationName")

package com.thanh0x.coursedeals.core.logging

internal actual object PlatformTracingBridge {
    actual fun detectOsName(): String {
        val isAndroid = runCatching {
            Class.forName("android.os.Build")
            true
        }.getOrDefault(false)
        return if (isAndroid) {
            "Android"
        } else {
            System.getProperty("os.name") ?: "JVM"
        }
    }

    actual fun detectOsArch(): String {
        return System.getProperty("os.arch") ?: "unknown"
    }

    actual fun resolveDefaultDsn(): String {
        return System.getenv("SENTRY_DSN")?.trim().orEmpty()
    }

    actual fun installCrashHandler() {
        AndroidCrashHandler.install()
    }
}
