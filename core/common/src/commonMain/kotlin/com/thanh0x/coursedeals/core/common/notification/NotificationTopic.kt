package com.thanh0x.coursedeals.core.common.notification

enum class NotificationTopic(
    val topicId: String,
    val displayName: String,
    val categoryName: String? = null,
) {
    ALL("deals_all", "All Deals"),
    HOT_DEALS("deals_hot", "Hot Deals"),
    DEVELOPMENT("deals_development", "Development", "Development"),
    DESIGN("deals_design", "Design", "Design"),
    BUSINESS("deals_business", "Business", "Business"),
    IT_SOFTWARE("deals_it_software", "IT & Software", "IT & Software"),
    MARKETING("deals_marketing", "Marketing", "Marketing"),
    PERSONAL_DEVELOPMENT("deals_personal_development", "Personal Development", "Personal Development"),
    PHOTOGRAPHY("deals_photography", "Photography", "Photography"),
    MUSIC("deals_music", "Music", "Music"),
    HEALTH_FITNESS("deals_health_fitness", "Health & Fitness", "Health & Fitness"),
    FINANCE("deals_finance", "Finance", "Finance");

    companion object {
        const val TOPIC_DEALS_ALL = "deals_all"
        const val TOPIC_DEALS_HOT = "deals_hot"
        const val TOPIC_DEALS_DEVELOPMENT = "deals_development"
        const val TOPIC_DEALS_DESIGN = "deals_design"
        const val TOPIC_DEALS_BUSINESS = "deals_business"
        const val TOPIC_DEALS_IT_SOFTWARE = "deals_it_software"
        const val TOPIC_DEALS_MARKETING = "deals_marketing"
        const val TOPIC_DEALS_PERSONAL_DEVELOPMENT = "deals_personal_development"
        const val TOPIC_DEALS_PHOTOGRAPHY = "deals_photography"
        const val TOPIC_DEALS_MUSIC = "deals_music"
        const val TOPIC_DEALS_HEALTH_FITNESS = "deals_health_fitness"
        const val TOPIC_DEALS_FINANCE = "deals_finance"

        val ALL_TOPIC_IDS = entries.map { it.topicId }.toSet()

        fun fromTopicId(topicId: String): NotificationTopic? =
            entries.find { it.topicId == topicId }

        fun fromCategoryName(categoryName: String?): NotificationTopic? {
            if (categoryName.isNullOrBlank()) return null
            return entries.find { it.categoryName?.equals(categoryName, ignoreCase = true) == true }
        }

        fun topicForCategory(categoryName: String?): String {
            val matched = fromCategoryName(categoryName)
            if (matched != null) return matched.topicId
            if (categoryName.isNullOrBlank()) return TOPIC_DEALS_ALL
            val sanitized = categoryName.trim().lowercase()
                .replace("&", "")
                .replace(Regex("[^a-z0-9]+"), "_")
                .trim('_')
            return "deals_$sanitized"
        }
    }
}
