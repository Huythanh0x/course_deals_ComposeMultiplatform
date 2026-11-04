package com.thanh0x.coursedeals.shared.watch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val DEFAULT_COURSE_ID = 501
private const val DEFAULT_RATING = 4.8
private const val DEFAULT_REVIEWS = 240
private const val DEFAULT_USES_DIVISOR = 20
private const val MIN_USES_REMAINING = 5
private const val DEFAULT_WATCH_DEAL_LIMIT = 5
private const val DEFAULT_HOT_DEALS_BASE_URL = "http://127.0.0.1:8081"

@Serializable
enum class WatchCoursePlatform(val displayName: String) {
    @SerialName("Udemy")
    UDEMY("Udemy"),

    @SerialName("Coursera")
    COURSERA("Coursera"),

    @SerialName("edX")
    EDX("edX"),
    ;

    companion object {
        fun fromRawOrInfer(
            platformRaw: String? = null,
            previewImage: String? = null,
            title: String? = null,
        ): WatchCoursePlatform {
            val normalizedPlatform = platformRaw?.trim()?.lowercase().orEmpty()
            val combinedHint = "${previewImage.orEmpty()} ${title.orEmpty()}".lowercase()
            return when {
                normalizedPlatform == "coursera" || combinedHint.contains("coursera") -> COURSERA
                normalizedPlatform == "edx" || combinedHint.contains("edx") -> EDX
                else -> UDEMY
            }
        }
    }
}

@Serializable
data class WatchDealDto(
    val id: Int = DEFAULT_COURSE_ID,
    val title: String,
    val heading: String? = null,
    val author: String? = null,
    val category: String? = null,
    val couponCode: String? = null,
    val couponUrl: String? = null,
    val expiredTime: Long? = null,
    val rating: Double? = null,
    val reviews: Int? = null,
    val usesRemaining: Int? = null,
    val platform: WatchCoursePlatform = WatchCoursePlatform.UDEMY,
    val isBookmarked: Boolean = false,
    val students: Int? = null,
    val isHot: Boolean = true,
)

@Serializable
data class WatchApiCourseDto(
    val courseId: Int = DEFAULT_COURSE_ID,
    val title: String = "Course Deal",
    val heading: String? = null,
    val author: String? = null,
    val category: String? = null,
    val couponCode: String? = null,
    val targetUrl: String? = null,
    val previewImage: String? = null,
    val expiredTime: Double? = null,
    val rating: Double? = null,
    val reviews: Int? = null,
    val students: Int? = null,
    val platform: String? = null,
) {
    fun toWatchDealDto(isBookmarked: Boolean = false): WatchDealDto {
        val resolvedId = if (courseId > 0) courseId else DEFAULT_COURSE_ID
        val resolvedReviews = reviews ?: DEFAULT_REVIEWS
        val resolvedUrl = targetUrl?.takeIf { it.isNotBlank() }
            ?: "https://coursedeals.com/deal/$resolvedId"
        return WatchDealDto(
            id = resolvedId,
            title = title,
            heading = heading,
            author = author ?: "Udemy Instructor",
            category = category ?: "Development",
            couponCode = couponCode ?: "HOT100",
            couponUrl = resolvedUrl,
            expiredTime = expiredTime?.toLong(),
            rating = rating ?: DEFAULT_RATING,
            reviews = resolvedReviews,
            usesRemaining = maxOf(MIN_USES_REMAINING, resolvedReviews / DEFAULT_USES_DIVISOR),
            platform = WatchCoursePlatform.fromRawOrInfer(
                platformRaw = platform,
                previewImage = previewImage,
                title = title,
            ),
            isBookmarked = isBookmarked,
            students = students ?: 0,
            isHot = true,
        )
    }
}

@Serializable
data class WatchHotDealsPageDto(
    val courses: List<WatchApiCourseDto> = emptyList(),
)

object WatchDealJsonCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    fun buildHotDealsUrl(
        baseUrl: String = DEFAULT_HOT_DEALS_BASE_URL,
        preference: String? = null,
    ): String {
        val cleanBase = baseUrl.trimEnd('/')
        val baseQuery = "$cleanBase/api/v1/coupons/hot-deals?pageIndex=0&numberPerPage=20&minRating=4.7"
        val cleanPref = preference?.trim().orEmpty()
        return if (cleanPref.isNotEmpty()) {
            val encodedPref = cleanPref.replace(" ", "%20")
            "$baseQuery&preference=$encodedPref"
        } else {
            baseQuery
        }
    }

    fun mapHotDealsApiJsonToWatchDeals(
        apiJson: String,
        bookmarkedIds: Set<Int> = emptySet(),
        limit: Int = DEFAULT_WATCH_DEAL_LIMIT,
    ): List<WatchDealDto> {
        if (apiJson.isBlank()) return emptyList()
        return runCatching {
            val page = json.decodeFromString<WatchHotDealsPageDto>(apiJson)
            page.courses
                .take(limit.coerceAtLeast(1))
                .map { course ->
                    val resolvedId = if (course.courseId > 0) course.courseId else DEFAULT_COURSE_ID
                    course.toWatchDealDto(isBookmarked = resolvedId in bookmarkedIds)
                }
        }.getOrDefault(emptyList())
    }

    fun mapHotDealsApiJsonToWatchDealsJson(
        apiJson: String,
        bookmarkedIds: Set<Int> = emptySet(),
        limit: Int = DEFAULT_WATCH_DEAL_LIMIT,
    ): String {
        val deals = mapHotDealsApiJsonToWatchDeals(
            apiJson = apiJson,
            bookmarkedIds = bookmarkedIds,
            limit = limit,
        )
        return json.encodeToString(deals)
    }
}
