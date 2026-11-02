package com.thanh0x.coursedeals.core.logging

data class LogEntry(
    val timestamp: Long,
    val formattedTime: String,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val throwableString: String? = null,
) {
    fun toFormattedLine(): String {
        val base = "$formattedTime [${level.shortCode}] [$tag] $message"
        return if (throwableString != null) {
            "$base\n$throwableString"
        } else {
            base
        }
    }
}
