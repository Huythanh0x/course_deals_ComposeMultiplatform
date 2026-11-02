@file:Suppress("MatchingDeclarationName")

package com.thanh0x.coursedeals.core.logging

import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Locale

actual object LogFileManager {
    private val lock = Any()
    private var customLogDirectory: File? = null
    private val memoryBuffer = ArrayDeque<LogEntry>(1000)
    private const val MAX_BUFFER_SIZE = 1000
    private val logFileRegex = Regex("""^app\.(\d{4}-\d{2}-\d{2})\.log$""")

    private var activeFileWriter: FileWriter? = null
    private var activeFileDate: String? = null

    private fun resolveDirectory(): File {
        val dir = customLogDirectory ?: File(System.getProperty("java.io.tmpdir") ?: ".", "logs")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    actual fun init(directoryPath: String?) {
        synchronized(lock) {
            flushInternal()
            if (directoryPath != null) {
                customLogDirectory = File(directoryPath).apply { mkdirs() }
            }
            pruneOldLogs(retentionDays = 3)
        }
    }

    actual fun getLogDirectoryPath(): String {
        return synchronized(lock) {
            resolveDirectory().absolutePath
        }
    }

    @Suppress("TooGenericExceptionCaught")
    actual fun appendLog(entry: LogEntry) {
        synchronized(lock) {
            if (memoryBuffer.size >= MAX_BUFFER_SIZE) {
                memoryBuffer.removeFirst()
            }
            memoryBuffer.addLast(entry)

            val dir = resolveDirectory()
            val dateStr = formatLogDate(entry.timestamp)
            try {
                if (activeFileDate != dateStr || activeFileWriter == null) {
                    activeFileWriter?.flush()
                    activeFileWriter?.close()
                    val file = File(dir, "app.$dateStr.log")
                    activeFileWriter = FileWriter(file, true)
                    activeFileDate = dateStr
                }
                activeFileWriter?.write(entry.toFormattedLine() + "\n")
            } catch (e: Throwable) {
                System.err.println("LogFileManager: failed to append log to file: ${e.message}")
            }
        }
    }

    actual fun getLogFiles(): List<String> {
        return synchronized(lock) {
            val dir = resolveDirectory()
            val files = dir.listFiles { _, name -> logFileRegex.matches(name) }
            files?.map { it.name }?.sortedDescending() ?: emptyList()
        }
    }

    actual fun getRecentLogs(maxLines: Int, levelFilter: LogLevel?): List<String> {
        return synchronized(lock) {
            flushInternal()
            if (memoryBuffer.isNotEmpty()) {
                val filtered = memoryBuffer
                    .filter { levelFilter == null || it.level == levelFilter }
                    .takeLast(maxLines)
                    .map { it.toFormattedLine() }
                if (filtered.isNotEmpty()) {
                    return@synchronized filtered
                }
            }

            // Fallback: Read from disk files if memory buffer is empty or filtered yielded nothing
            val dir = resolveDirectory()
            val files = dir.listFiles { _, name -> logFileRegex.matches(name) }
                ?.sortedByDescending { it.name }
                ?: emptyList()

            val lines = mutableListOf<String>()
            for (file in files) {
                try {
                    val fileLines = file.readLines()
                    for (line in fileLines.reversed()) {
                        if (levelFilter == null || line.contains("[${levelFilter.shortCode}]")) {
                            lines.add(line)
                            if (lines.size >= maxLines) break
                        }
                    }
                } catch (_: Throwable) {}
                if (lines.size >= maxLines) break
            }
            lines.reversed()
        }
    }

    actual fun getFormattedLogs(maxLines: Int, levelFilter: LogLevel?): String {
        val lines = getRecentLogs(maxLines, levelFilter)
        return if (lines.isEmpty()) {
            "No diagnostic logs available."
        } else {
            lines.joinToString(separator = "\n")
        }
    }

    actual fun pruneOldLogs(retentionDays: Int) {
        synchronized(lock) {
            flushInternal()
            val dir = resolveDirectory()
            val files = dir.listFiles { _, name -> logFileRegex.matches(name) } ?: return
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val now = System.currentTimeMillis()
            val cutoffMillis = now - (retentionDays.toLong() * 24L * 60L * 60L * 1000L)

            for (file in files) {
                val match = logFileRegex.find(file.name) ?: continue
                val dateStr = match.groupValues[1]
                try {
                    val fileDate = sdf.parse(dateStr)
                    if (fileDate != null && fileDate.time < cutoffMillis) {
                        file.delete()
                    }
                } catch (_: Throwable) {}
            }
        }
    }

    actual fun flush() {
        synchronized(lock) {
            flushInternal()
        }
    }

    private fun flushInternal() {
        try {
            activeFileWriter?.flush()
        } catch (_: Throwable) {}
    }

    actual fun clearLogs() {
        synchronized(lock) {
            flushInternal()
            try {
                activeFileWriter?.close()
            } catch (_: Throwable) {}
            activeFileWriter = null
            activeFileDate = null
            memoryBuffer.clear()

            val dir = resolveDirectory()
            val files = dir.listFiles { _, name -> logFileRegex.matches(name) } ?: return
            for (file in files) {
                file.delete()
            }
        }
    }
}
