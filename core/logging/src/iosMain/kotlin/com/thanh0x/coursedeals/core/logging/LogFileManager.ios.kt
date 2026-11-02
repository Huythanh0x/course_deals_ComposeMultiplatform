package com.thanh0x.coursedeals.core.logging

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.timeIntervalSince1970
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fputs

@OptIn(ExperimentalForeignApi::class)
actual object LogFileManager {
    private var customLogDirectory: String? = null
    private val memoryBuffer = ArrayDeque<LogEntry>(1000)
    private const val MAX_BUFFER_SIZE = 1000
    private val logFileRegex = Regex("""^app\.(\d{4}-\d{2}-\d{2})\.log$""")

    private fun resolveDirectory(): String {
        customLogDirectory?.let { return it }
        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )
        val logsDir = (documentDirectory?.path ?: ".") + "/logs"
        if (!NSFileManager.defaultManager.fileExistsAtPath(logsDir)) {
            NSFileManager.defaultManager.createDirectoryAtPath(
                path = logsDir,
                withIntermediateDirectories = true,
                attributes = null,
                error = null,
            )
        }
        return logsDir
    }

    actual fun init(directoryPath: String?) {
        if (directoryPath != null) {
            customLogDirectory = directoryPath
            if (!NSFileManager.defaultManager.fileExistsAtPath(directoryPath)) {
                NSFileManager.defaultManager.createDirectoryAtPath(
                    path = directoryPath,
                    withIntermediateDirectories = true,
                    attributes = null,
                    error = null,
                )
            }
        }
        pruneOldLogs(retentionDays = 3)
    }

    actual fun getLogDirectoryPath(): String = resolveDirectory()

    actual fun appendLog(entry: LogEntry) {
        if (memoryBuffer.size >= MAX_BUFFER_SIZE) {
            memoryBuffer.removeFirst()
        }
        memoryBuffer.addLast(entry)

        val dir = resolveDirectory()
        val dateStr = formatLogDate(entry.timestamp)
        val filePath = "$dir/app.$dateStr.log"
        val line = entry.toFormattedLine() + "\n"

        val file = fopen(filePath, "a")
        if (file != null) {
            fputs(line, file)
            fclose(file)
        }
    }

    actual fun getLogFiles(): List<String> {
        val dir = resolveDirectory()
        val fileManager = NSFileManager.defaultManager
        val contents = fileManager.contentsOfDirectoryAtPath(dir, error = null) ?: return emptyList()
        return contents.mapNotNull { it as? String }
            .filter { logFileRegex.matches(it) }
            .sortedDescending()
    }

    actual fun getRecentLogs(maxLines: Int, levelFilter: LogLevel?): List<String> {
        if (memoryBuffer.isNotEmpty()) {
            val filtered = memoryBuffer
                .filter { levelFilter == null || it.level == levelFilter }
                .takeLast(maxLines)
                .map { it.toFormattedLine() }
            if (filtered.isNotEmpty()) {
                return filtered
            }
        }

        val logFiles = getLogFiles()
        val dir = resolveDirectory()
        val lines = mutableListOf<String>()

        for (fileName in logFiles) {
            val filePath = "$dir/$fileName"
            val content = NSString.stringWithContentsOfFile(filePath, encoding = NSUTF8StringEncoding, error = null)
            if (content != null) {
                val fileLines = content.split("\n").filter { it.isNotBlank() }
                for (line in fileLines.reversed()) {
                    if (levelFilter == null || line.contains("[${levelFilter.shortCode}]")) {
                        lines.add(line)
                        if (lines.size >= maxLines) break
                    }
                }
            }
            if (lines.size >= maxLines) break
        }
        return lines.reversed()
    }

    actual fun getFormattedLogs(maxLines: Int, levelFilter: LogLevel?): String {
        val lines = getRecentLogs(maxLines, levelFilter)
        return if (lines.isEmpty()) {
            "No diagnostic logs available."
        } else {
            lines.joinToString("\n")
        }
    }

    actual fun pruneOldLogs(retentionDays: Int) {
        val dir = resolveDirectory()
        val files = getLogFiles()
        val fileManager = NSFileManager.defaultManager

        val formatter = NSDateFormatter().apply { dateFormat = "yyyy-MM-dd" }
        val now = currentTimeMillis()
        val cutoffMillis = now - (retentionDays.toLong() * 24L * 60L * 60L * 1000L)

        for (fileName in files) {
            val match = logFileRegex.find(fileName) ?: continue
            val dateStr = match.groupValues[1]
            val date = formatter.dateFromString(dateStr)
            if (date != null) {
                val epochMillis = (date.timeIntervalSince1970 * 1000.0).toLong()
                if (epochMillis < cutoffMillis) {
                    fileManager.removeItemAtPath("$dir/$fileName", error = null)
                }
            }
        }
    }

    actual fun flush() {}

    actual fun clearLogs() {
        memoryBuffer.clear()
        val dir = resolveDirectory()
        val files = getLogFiles()
        val fileManager = NSFileManager.defaultManager
        for (fileName in files) {
            fileManager.removeItemAtPath("$dir/$fileName", error = null)
        }
    }
}
