package com.thanh0x.coursedeals.core.logging

enum class LogLevel(val priority: Int, val shortCode: String) {
    VERBOSE(2, "V"),
    DEBUG(3, "D"),
    INFO(4, "I"),
    WARN(5, "W"),
    ERROR(6, "E"),
    NONE(7, "N");

    companion object {
        fun fromPriority(priority: Int): LogLevel =
            entries.firstOrNull { it.priority == priority } ?: DEBUG
    }
}
