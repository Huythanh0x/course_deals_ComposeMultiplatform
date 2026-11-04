package com.thanh0x.coursedeals.shared.watch

import com.thanh0x.coursedeals.core.common.BuildConfig
import com.thanh0x.coursedeals.domain.coupons.Coupon
import kotlin.math.abs
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
private const val RATING_TENTHS_MULTIPLIER = 10.0
private const val TENTHS_DIVISOR = 10
private const val HALF_ROUNDING_OFFSET = 0.5
private const val ONE_MILLION_STUDENTS = 1_000_000
private const val HUNDRED_THOUSAND_STUDENTS = 100_000.0
private const val ONE_THOUSAND_STUDENTS = 1_000
private const val EPOCH_MILLIS_THRESHOLD = 10_000_000_000L
private const val MILLIS_PER_SECOND = 1_000L
private const val MILLIS_PER_MINUTE = 60_000L
private const val MINUTES_PER_HOUR = 60L
private const val HOURS_PER_DAY = 24L
private const val DEFAULT_TIME_LEFT_LABEL = "2h left"
private const val RATING_EPSILON = 0.001
private const val SAMPLE_EXPIRY_OFFSET_SECONDS = 7_200L
private const val SAMPLE_RATING = 4.9
private const val SAMPLE_REVIEWS = 420
private const val SAMPLE_USES_REMAINING = 18
private const val SAMPLE_STUDENTS = 48_500

private val SUPPORTED_WATCH_DEEP_LINK_PREFIXES = listOf(
    "coursedeals://courses/",
    "coursedeals://coupon/",
    "https://coursedeals.com/deal/",
    "https://coursedeals.com/courses/",
)

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
    var isBookmarked: Boolean = false,
    val students: Int? = null,
    val isHot: Boolean = true,
) {
    val discountBadgeText: String
        get() = "100% OFF"

    val deepLinkUrlString: String
        get() = "coursedeals://courses/$id"

    val formattedRating: String
        get() {
            val value = rating?.takeIf { it > 0.0 } ?: DEFAULT_RATING
            val totalTenths = (value * RATING_TENTHS_MULTIPLIER + HALF_ROUNDING_OFFSET).toInt()
            val whole = totalTenths / TENTHS_DIVISOR
            val tenth = abs(totalTenths % TENTHS_DIVISOR)
            return "★ $whole.$tenth"
        }

    val formattedStudents: String
        get() {
            val count = students ?: return ""
            if (count <= 0) return ""
            return when {
                count >= ONE_MILLION_STUDENTS -> {
                    val totalTenths = (count.toDouble() / HUNDRED_THOUSAND_STUDENTS + HALF_ROUNDING_OFFSET).toInt()
                    val whole = totalTenths / TENTHS_DIVISOR
                    val tenth = abs(totalTenths % TENTHS_DIVISOR)
                    "$whole.${tenth}M students"
                }
                count >= ONE_THOUSAND_STUDENTS -> {
                    val thousands = (count.toDouble() / ONE_THOUSAND_STUDENTS.toDouble() + HALF_ROUNDING_OFFSET).toInt()
                    "${thousands}k students"
                }
                else -> "$count students"
            }
        }

    fun isExpiredAt(nowEpochMillis: Long): Boolean {
        val exp = expiredTime?.takeIf { it > 0L } ?: return false
        val expireMillis = if (exp > EPOCH_MILLIS_THRESHOLD) exp else exp * MILLIS_PER_SECOND
        return expireMillis <= nowEpochMillis
    }

    fun formattedTimeLeftAt(nowEpochMillis: Long): String {
        val exp = expiredTime?.takeIf { it > 0L } ?: return DEFAULT_TIME_LEFT_LABEL
        val expireMillis = if (exp > EPOCH_MILLIS_THRESHOLD) exp else exp * MILLIS_PER_SECOND
        val diffMillis = expireMillis - nowEpochMillis
        if (diffMillis <= 0L) return "Expired"
        val totalMinutes = diffMillis / MILLIS_PER_MINUTE
        val hours = totalMinutes / MINUTES_PER_HOUR
        val minutes = totalMinutes % MINUTES_PER_HOUR
        return when {
            hours > HOURS_PER_DAY -> "${hours / HOURS_PER_DAY}d left"
            hours > 0L -> "${hours}h ${minutes}m left"
            else -> "${maxOf(1L, minutes)}m left"
        }
    }

    fun withBookmark(bookmarked: Boolean): WatchDealDto = copy(isBookmarked = bookmarked)
}

fun Coupon.toWatchDealDto(isBookmarked: Boolean = false): WatchDealDto {
    val resolvedId = if (courseId > 0) courseId else DEFAULT_COURSE_ID
    val resolvedReviews = reviews ?: DEFAULT_REVIEWS
    val resolvedTitle = title?.takeIf { it.isNotBlank() } ?: "Course Deal"
    val resolvedUrl = couponUrl?.takeIf { it.isNotBlank() }
        ?: "https://coursedeals.com/deal/$resolvedId"
    return WatchDealDto(
        id = resolvedId,
        title = resolvedTitle,
        heading = heading,
        author = author ?: "Udemy Instructor",
        category = category ?: "Development",
        couponCode = couponCode ?: "HOT100",
        couponUrl = resolvedUrl,
        expiredTime = expiredTime,
        rating = rating ?: DEFAULT_RATING,
        reviews = resolvedReviews,
        usesRemaining = usesRemaining ?: maxOf(MIN_USES_REMAINING, resolvedReviews / DEFAULT_USES_DIVISOR),
        platform = WatchCoursePlatform.fromRawOrInfer(
            previewImage = previewImage,
            title = resolvedTitle,
        ),
        isBookmarked = isBookmarked,
        students = students ?: 0,
        isHot = true,
    )
}

@Serializable
data class WatchApiCourseDto(
    val id: Int? = null,
    val courseId: Int = DEFAULT_COURSE_ID,
    val title: String = "Course Deal",
    val heading: String? = null,
    val author: String? = null,
    val category: String? = null,
    val couponCode: String? = null,
    val couponUrl: String? = null,
    val targetUrl: String? = null,
    val previewImage: String? = null,
    val expiredTime: Double? = null,
    val expiresAtEpochSeconds: Double? = null,
    val rating: Double? = null,
    val reviews: Int? = null,
    val usesRemaining: Int? = null,
    val usesLeft: Int? = null,
    val students: Int? = null,
    val platform: String? = null,
    val isBookmarked: Boolean = false,
    val isHot: Boolean = true,
) {
    fun resolvedCourseId(): Int {
        val candidate = id?.takeIf { it > 0 } ?: courseId
        return if (candidate > 0) candidate else DEFAULT_COURSE_ID
    }

    fun toWatchDealDto(bookmarkedOverride: Boolean? = null): WatchDealDto {
        val resolvedId = resolvedCourseId()
        val resolvedReviews = reviews ?: DEFAULT_REVIEWS
        val resolvedUrl = couponUrl?.takeIf { it.isNotBlank() }
            ?: targetUrl?.takeIf { it.isNotBlank() }
            ?: previewImage?.takeIf { it.isNotBlank() }
            ?: "https://coursedeals.com/deal/$resolvedId"
        val resolvedExpired = (expiredTime ?: expiresAtEpochSeconds)?.toLong()
        val fallbackUses = maxOf(MIN_USES_REMAINING, resolvedReviews / DEFAULT_USES_DIVISOR)
        val resolvedUses = usesRemaining ?: usesLeft ?: fallbackUses
        return WatchDealDto(
            id = resolvedId,
            title = title,
            heading = heading,
            author = author ?: "Udemy Instructor",
            category = category ?: "Development",
            couponCode = couponCode ?: "HOT100",
            couponUrl = resolvedUrl,
            expiredTime = resolvedExpired,
            rating = rating ?: DEFAULT_RATING,
            reviews = resolvedReviews,
            usesRemaining = resolvedUses,
            platform = WatchCoursePlatform.fromRawOrInfer(
                platformRaw = platform,
                previewImage = previewImage,
                title = title,
            ),
            isBookmarked = bookmarkedOverride ?: isBookmarked,
            students = students ?: 0,
            isHot = isHot,
        )
    }
}

@Serializable
data class WatchHotDealsPageDto(
    val courses: List<WatchApiCourseDto> = emptyList(),
    val deals: List<WatchApiCourseDto> = emptyList(),
)

object WatchDealJsonCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    fun defaultBaseUrl(): String =
        BuildConfig.LOCAL_API_BASE_URL.ifBlank { DEFAULT_HOT_DEALS_BASE_URL }

    fun buildHotDealsUrl(
        baseUrl: String = defaultBaseUrl(),
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

    fun parseCourseIdFromDeepLink(url: String): Int? {
        val trimmed = url.trim()
        val matchingPrefix = SUPPORTED_WATCH_DEEP_LINK_PREFIXES.firstOrNull { trimmed.startsWith(it) }
            ?: return null
        val pathPart = trimmed
            .removePrefix(matchingPrefix)
            .substringBefore("?")
            .substringBefore("#")
            .substringBefore("/")
        return pathPart.toIntOrNull()?.takeIf { it > 0 }
    }

    fun decodeDealsFromJson(
        jsonString: String,
        limit: Int = DEFAULT_WATCH_DEAL_LIMIT,
    ): List<WatchDealDto> {
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty()) return emptyList()
        val maxItems = limit.coerceAtLeast(1)
        return runCatching {
            if (trimmed.startsWith("[")) {
                json.decodeFromString<List<WatchApiCourseDto>>(trimmed)
                    .take(maxItems)
                    .map { it.toWatchDealDto() }
            } else {
                val page = json.decodeFromString<WatchHotDealsPageDto>(trimmed)
                val rawList = page.courses.ifEmpty { page.deals }
                rawList
                    .take(maxItems)
                    .map { it.toWatchDealDto() }
            }
        }.getOrDefault(emptyList())
    }

    fun encodeDealsToJson(deals: List<WatchDealDto>): String =
        runCatching { json.encodeToString(deals) }.getOrDefault("[]")

    fun mapHotDealsApiJsonToWatchDeals(
        apiJson: String,
        bookmarkedIds: Set<Int> = emptySet(),
        limit: Int = DEFAULT_WATCH_DEAL_LIMIT,
    ): List<WatchDealDto> =
        decodeDealsFromJson(jsonString = apiJson, limit = limit).map { deal ->
            if (deal.id in bookmarkedIds) deal.withBookmark(true) else deal
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
        return encodeDealsToJson(deals)
    }

    fun selectTopActiveDeal(
        deals: List<WatchDealDto>,
        nowEpochMillis: Long,
    ): WatchDealDto? {
        if (deals.isEmpty()) return null
        val activeDeals = deals.filterNot { it.isExpiredAt(nowEpochMillis) }
        val pool = activeDeals.ifEmpty { deals }
        return pool.sortedWith(
            compareByDescending<WatchDealDto> { it.isHot }
                .thenComparator { lhs, rhs ->
                    val lRating = lhs.rating ?: 0.0
                    val rRating = rhs.rating ?: 0.0
                    if (abs(lRating - rRating) > RATING_EPSILON) {
                        rRating.compareTo(lRating)
                    } else {
                        (rhs.students ?: 0).compareTo(lhs.students ?: 0)
                    }
                }
        ).firstOrNull()
    }

    fun countActiveDeals(
        deals: List<WatchDealDto>,
        nowEpochMillis: Long,
    ): Int = deals.count { !it.isExpiredAt(nowEpochMillis) }

    fun sampleDeal(nowEpochSeconds: Long): WatchDealDto = WatchDealDto(
        id = DEFAULT_COURSE_ID,
        title = "Complete Kotlin Multiplatform & SwiftUI Masterclass",
        author = "Course Deals Academy",
        category = "Mobile Development",
        couponCode = "KMP100FREE",
        couponUrl = "https://coursedeals.com/deal/$DEFAULT_COURSE_ID",
        expiredTime = nowEpochSeconds + SAMPLE_EXPIRY_OFFSET_SECONDS,
        rating = SAMPLE_RATING,
        reviews = SAMPLE_REVIEWS,
        usesRemaining = SAMPLE_USES_REMAINING,
        platform = WatchCoursePlatform.UDEMY,
        isBookmarked = true,
        students = SAMPLE_STUDENTS,
        isHot = true,
    )
}
