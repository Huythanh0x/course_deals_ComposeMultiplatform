package com.thanh0x.coursedeals.core.logging

internal expect object PlatformTracingBridge {
    fun detectOsName(): String
    fun detectOsArch(): String
    fun resolveDefaultDsn(): String
    fun installCrashHandler()
}
