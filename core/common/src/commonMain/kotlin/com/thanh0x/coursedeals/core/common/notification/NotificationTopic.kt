package com.thanh0x.coursedeals.core.common.notification

enum class NotificationTopic(
    val topicId: String,
    val displayName: String,
) {
    ALL("deals_all", "All Deals"),
    DEVELOPMENT("deals_development", "Development"),
    BUSINESS("deals_business", "Business"),
    DESIGN("deals_design", "Design"),
    IT_SOFTWARE("deals_it_software", "IT & Software");

    companion object {
        const val TOPIC_DEALS_ALL = "deals_all"
        const val TOPIC_DEALS_DEVELOPMENT = "deals_development"
        const val TOPIC_DEALS_BUSINESS = "deals_business"
        const val TOPIC_DEALS_DESIGN = "deals_design"
        const val TOPIC_DEALS_IT_SOFTWARE = "deals_it_software"

        val ALL_TOPIC_IDS = entries.map { it.topicId }.toSet()

        fun fromTopicId(topicId: String): NotificationTopic? =
            entries.find { it.topicId == topicId }
    }
}
