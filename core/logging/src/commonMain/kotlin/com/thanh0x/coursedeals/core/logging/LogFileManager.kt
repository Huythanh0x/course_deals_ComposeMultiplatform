package com.thanh0x.coursedeals.core.logging

expect object LogFileManager {
    fun init(directoryPath: String? = null)
    fun getLogDirectoryPath(): String
    fun appendLog(entry: LogEntry)
    fun getLogFiles(): List<String>
    fun getRecentLogs(maxLines: Int = 500, levelFilter: LogLevel? = null): List<String>
    fun getFormattedLogs(maxLines: Int = 500, levelFilter: LogLevel? = null): String
    fun pruneOldLogs(retentionDays: Int = 3)
    fun flush()
    fun clearLogs()
}
