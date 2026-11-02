package com.thanh0x.coursedeals.core.logging

import java.io.File

object AndroidLogFileManager {
    fun init(directory: File) {
        LogFileManager.init(directory.absolutePath)
    }

    fun init(directoryPath: String) {
        LogFileManager.init(directoryPath)
    }

    fun getLogDirectoryPath(): String = LogFileManager.getLogDirectoryPath()

    fun pruneOldLogs(retentionDays: Int = 3) {
        LogFileManager.pruneOldLogs(retentionDays)
    }

    fun flush() {
        LogFileManager.flush()
    }
}
