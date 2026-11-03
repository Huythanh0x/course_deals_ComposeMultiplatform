package com.thanh0x.coursedeals.core.common.notification

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NotificationTopicTest {

    @Test
    fun all12Topics_areDefinedAndDistinct() {
        assertEquals(12, NotificationTopic.entries.size)
        assertEquals(12, NotificationTopic.ALL_TOPIC_IDS.size)
        assertTrue(NotificationTopic.ALL_TOPIC_IDS.contains("deals_all"))
        assertTrue(NotificationTopic.ALL_TOPIC_IDS.contains("deals_hot"))
        assertTrue(NotificationTopic.ALL_TOPIC_IDS.contains("deals_development"))
        assertTrue(NotificationTopic.ALL_TOPIC_IDS.contains("deals_design"))
        assertTrue(NotificationTopic.ALL_TOPIC_IDS.contains("deals_business"))
        assertTrue(NotificationTopic.ALL_TOPIC_IDS.contains("deals_it_software"))
        assertTrue(NotificationTopic.ALL_TOPIC_IDS.contains("deals_marketing"))
        assertTrue(NotificationTopic.ALL_TOPIC_IDS.contains("deals_personal_development"))
        assertTrue(NotificationTopic.ALL_TOPIC_IDS.contains("deals_photography"))
        assertTrue(NotificationTopic.ALL_TOPIC_IDS.contains("deals_music"))
        assertTrue(NotificationTopic.ALL_TOPIC_IDS.contains("deals_health_fitness"))
        assertTrue(NotificationTopic.ALL_TOPIC_IDS.contains("deals_finance"))
    }

    @Test
    fun fromTopicId_returnsCorrectTopic() {
        assertEquals(NotificationTopic.ALL, NotificationTopic.fromTopicId("deals_all"))
        assertEquals(NotificationTopic.HOT_DEALS, NotificationTopic.fromTopicId("deals_hot"))
        assertEquals(NotificationTopic.MARKETING, NotificationTopic.fromTopicId("deals_marketing"))
        assertEquals(NotificationTopic.FINANCE, NotificationTopic.fromTopicId("deals_finance"))
        assertEquals(null, NotificationTopic.fromTopicId("unknown_topic"))
    }

    @Test
    fun fromCategoryName_caseInsensitiveMapping() {
        assertEquals(NotificationTopic.DEVELOPMENT, NotificationTopic.fromCategoryName("Development"))
        assertEquals(NotificationTopic.DEVELOPMENT, NotificationTopic.fromCategoryName("development"))
        assertEquals(NotificationTopic.IT_SOFTWARE, NotificationTopic.fromCategoryName("IT & Software"))
        assertEquals(NotificationTopic.PERSONAL_DEVELOPMENT, NotificationTopic.fromCategoryName("Personal Development"))
        assertEquals(NotificationTopic.HEALTH_FITNESS, NotificationTopic.fromCategoryName("Health & Fitness"))
        assertEquals(null, NotificationTopic.fromCategoryName(null))
        assertEquals(null, NotificationTopic.fromCategoryName(""))
    }

    @Test
    fun topicForCategory_returnsExpectedTopicId() {
        assertEquals("deals_development", NotificationTopic.topicForCategory("Development"))
        assertEquals("deals_it_software", NotificationTopic.topicForCategory("IT & Software"))
        assertEquals("deals_all", NotificationTopic.topicForCategory(null))
        assertEquals("deals_all", NotificationTopic.topicForCategory(""))
        assertEquals("deals_custom_topic", NotificationTopic.topicForCategory("Custom Topic"))
    }
}
