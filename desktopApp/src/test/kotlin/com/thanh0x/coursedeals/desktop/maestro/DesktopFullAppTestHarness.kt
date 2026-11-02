package com.thanh0x.coursedeals.desktop.maestro

import androidx.paging.PagingData
import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.core.logging.LogFileManager
import com.thanh0x.coursedeals.core.logging.LoggerFactory
import com.thanh0x.coursedeals.core.logging.debug
import com.thanh0x.coursedeals.core.logging.error
import com.thanh0x.coursedeals.core.logging.info
import com.thanh0x.coursedeals.core.logging.warn
import com.thanh0x.coursedeals.core.ui.util.NetworkUtil
import com.thanh0x.coursedeals.desktop.installDesktopPlatformActions
import com.thanh0x.coursedeals.desktop.installDesktopRenderers
import com.thanh0x.coursedeals.domain.coupons.Coupon
import com.thanh0x.coursedeals.domain.coupons.CouponMetadata
import com.thanh0x.coursedeals.domain.coupons.CouponRepository
import com.thanh0x.coursedeals.domain.coupons.CourseLanguage
import com.thanh0x.coursedeals.domain.coupons.FilterData
import com.thanh0x.coursedeals.domain.coupons.SearchRepository
import com.thanh0x.coursedeals.domain.coupons.SortOption
import com.thanh0x.coursedeals.domain.user.AuthCredentials
import com.thanh0x.coursedeals.domain.user.FeatureFlagManager
import com.thanh0x.coursedeals.domain.user.TokenData
import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository
import com.thanh0x.coursedeals.domain.user.UserPreferences
import com.thanh0x.coursedeals.domain.user.UserProfile
import com.thanh0x.coursedeals.domain.user.UserProfileRepository
import com.thanh0x.coursedeals.domain.user.model.AppConfig
import com.thanh0x.coursedeals.shared.sharedKoinModules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

private const val COURSE_ID_WEB_DEV = 101
private const val COURSE_ID_PROCUREMENT = 102
private const val COURSE_ID_KMP = 103
private const val ONE_HOUR_MS = 3_600_000L
private const val TWO_MINUTES_MS = 120_000L

class DesktopFullAppTestEnvironment {
    val couponRepository = FakeDesktopCouponRepository()
    val searchRepository = FakeDesktopSearchRepository()
    val authRepository = FakeDesktopAuthRepository()
    val userProfileRepository = FakeDesktopUserProfileRepository(authRepository)
    val featureFlagManager = FakeDesktopFeatureFlagManager()

    val openedUrls = mutableListOf<String>()
    val copiedClipboardEntries = mutableListOf<String>()
    val sentEmails = mutableListOf<Pair<String, String>>()
}

fun installFullAppDesktopTestKoin(): DesktopFullAppTestEnvironment {
    LogFileManager.init(null)
    val logger = LoggerFactory.getLogger("DesktopE2eHarness")
    logger.debug { "Desktop E2E debug log entry initialized" }
    logger.info { "Desktop E2E info log entry initialized" }
    logger.warn { "Desktop E2E warn log entry initialized" }
    logger.error { "Desktop E2E error log entry initialized" }

    val env = DesktopFullAppTestEnvironment()
    installDesktopPlatformActions()
    installDesktopRenderers(enableNativeWebEngine = false)

    com.thanh0x.coursedeals.core.ui.util.PlatformActions.onOpenUrl = { url ->
        env.openedUrls.add(url)
    }
    com.thanh0x.coursedeals.core.ui.util.PlatformActions.onCopyToClipboard = { text ->
        env.copiedClipboardEntries.add(text)
    }
    com.thanh0x.coursedeals.core.ui.util.PlatformActions.onSendEmail = { email, subject ->
        env.sentEmails.add(email to subject)
        true
    }

    val testOverrideModule = module {
        single<CouponRepository> { env.couponRepository }
        single<SearchRepository> { env.searchRepository }
        single<UserAuthenticationRepository> { env.authRepository }
        single<UserProfileRepository> { env.userProfileRepository }
        single<FeatureFlagManager> { env.featureFlagManager }
        single<NetworkUtil> {
            object : NetworkUtil(null) {
                override fun isInternetAvailable(): Boolean = true
            }
        }
    }

    runCatching { stopKoin() }
    startKoin {
        allowOverride(true)
        modules(sharedKoinModules + testOverrideModule)
    }

    return env
}

@Suppress("TooManyFunctions")
class FakeDesktopCouponRepository : CouponRepository {
    private val now = System.currentTimeMillis()

    private val initialCoupons = listOf(
        Coupon(
            courseId = COURSE_ID_WEB_DEV,
            author = "Dr. Angela Yu",
            category = "Development",
            contentLength = 3240,
            couponCode = "WEBDEV2026",
            couponUrl = "https://www.udemy.com/course/complete-web-development-bootcamp/?couponCode=WEBDEV2026",
            description = "Master HTML, CSS, Javascript, Node, React, PostgreSQL, Web3 and DApps.",
            expiredTime = now + ONE_HOUR_MS * 24,
            heading = "Become a Full-Stack Web Developer with just ONE course.",
            language = "English",
            level = "All Levels",
            previewImage = null,
            previewVideo = null,
            rating = 4.8,
            reviews = 12450,
            students = 85000,
            subCategory = "Web Development",
            title = "Complete Web Development Bootcamp 2026",
            usesRemaining = 420,
            isNew = true,
            createdAt = now - ONE_HOUR_MS,
            updatedAt = now - TWO_MINUTES_MS,
            cachedAt = now - TWO_MINUTES_MS,
        ),
        Coupon(
            courseId = COURSE_ID_PROCUREMENT,
            author = "MTF Institute",
            category = "Business",
            contentLength = 180,
            couponCode = "PROCUREFREE",
            couponUrl = "https://www.udemy.com/course/procurement-management/?couponCode=PROCUREFREE",
            description = "Comprehensive procurement, strategic sourcing, and supply chain management course.",
            expiredTime = now + ONE_HOUR_MS * 12,
            heading = "Learn strategic sourcing, supplier relationship management and negotiation.",
            language = "English",
            level = "Intermediate",
            previewImage = null,
            previewVideo = null,
            rating = 4.6,
            reviews = 920,
            students = 14200,
            subCategory = "Management",
            title = "Professional Diploma in Procurement Management",
            usesRemaining = 180,
            isNew = false,
            createdAt = now - ONE_HOUR_MS * 2,
            updatedAt = now - TWO_MINUTES_MS,
            cachedAt = now - TWO_MINUTES_MS,
        ),
        Coupon(
            courseId = COURSE_ID_KMP,
            author = "Huy Thanh",
            category = "IT & Software",
            contentLength = 1560,
            couponCode = "KMPDESKTOP",
            couponUrl = "https://www.udemy.com/course/kotlin-multiplatform-masterclass/?couponCode=KMPDESKTOP",
            description = "Deep dive into KMP, Koin, Ktor, Skia, and Maestro E2E testing.",
            expiredTime = now + ONE_HOUR_MS * 48,
            heading = "Build Android, iOS, and Desktop apps with 100% shared Compose UI.",
            language = "Others",
            level = "Beginner",
            previewImage = null,
            previewVideo = null,
            rating = 4.9,
            reviews = 3400,
            students = 28500,
            subCategory = "Mobile & Desktop",
            title = "Kotlin Multiplatform & Compose Desktop Masterclass",
            usesRemaining = 950,
            isNew = true,
            createdAt = now - TWO_MINUTES_MS,
            updatedAt = now - TWO_MINUTES_MS,
            cachedAt = now - TWO_MINUTES_MS,
        ),
    )

    private val couponsFlow = MutableStateFlow(initialCoupons)
    private val showLocalFetchTimeFlow = MutableStateFlow(false)
    val submittedDealUrls = mutableListOf<String>()
    val reportedCourses = mutableListOf<Triple<Int, String, String?>>()

    override suspend fun getAllCoupons(): List<Coupon> = couponsFlow.value

    override fun getCouponsPager(query: String?, filter: FilterData): Flow<PagingData<Coupon>> =
        couponsFlow.map { list ->
            PagingData.from(filterCoupons(list, query, filter))
        }

    override fun getFilteredCountFlow(query: String?, filter: FilterData): Flow<Int> =
        couponsFlow.map { list ->
            filterCoupons(list, query, filter).size
        }

    override suspend fun syncAllCoupons(force: Boolean) = Unit

    override suspend fun insertCoupon(coupon: Coupon) {
        couponsFlow.value = couponsFlow.value + coupon
    }

    override suspend fun clearALlCoupons(): Int {
        val count = couponsFlow.value.size
        couponsFlow.value = emptyList()
        return count
    }

    override suspend fun requestPostANewCoupon(couponUrl: String): AppResult<Unit> {
        submittedDealUrls.add(couponUrl)
        return AppResult.Success(Unit)
    }

    override suspend fun requestDeleteACoupon(couponUrl: String): AppResult<Unit> =
        AppResult.Success(Unit)

    override fun getMetadataFlow(): Flow<CouponMetadata> = couponsFlow.map { list ->
        CouponMetadata(
            totalCoupon = list.size.toLong(),
            lastFetchTime = now - TWO_MINUTES_MS,
            localFetchTime = now - TWO_MINUTES_MS / 2,
        )
    }

    override fun getShowLocalFetchTime(): Flow<Boolean> = showLocalFetchTimeFlow.asStateFlow()

    override suspend fun saveShowLocalFetchTime(show: Boolean) {
        showLocalFetchTimeFlow.value = show
    }

    override suspend fun fetchCouponDetail(courseId: Int): AppResult<Coupon> {
        val found = couponsFlow.value.firstOrNull { it.courseId == courseId }
            ?: couponsFlow.value.first()
        return AppResult.Success(found)
    }

    override suspend fun reportCourse(
        courseId: Int,
        reason: String,
        details: String?,
    ): AppResult<Unit> {
        reportedCourses.add(Triple(courseId, reason, details))
        return AppResult.Success(Unit)
    }

    private fun filterCoupons(
        source: List<Coupon>,
        query: String?,
        filter: FilterData,
    ): List<Coupon> {
        val trimmedQuery = query?.trim().orEmpty()
        val selectedCategoryLabels = filter.categories.map { it.dbValue.lowercase() }.toSet()

        val filtered = source.filter { coupon ->
            val matchesQuery = trimmedQuery.isEmpty() ||
                coupon.title?.contains(trimmedQuery, ignoreCase = true) == true ||
                coupon.heading?.contains(trimmedQuery, ignoreCase = true) == true ||
                coupon.category?.contains(trimmedQuery, ignoreCase = true) == true ||
                coupon.author?.contains(trimmedQuery, ignoreCase = true) == true

            val matchesCategory = selectedCategoryLabels.isEmpty() ||
                selectedCategoryLabels.any { cat ->
                    coupon.category?.lowercase()?.contains(cat) == true
                }

            val matchesLang = when (filter.language) {
                CourseLanguage.ALL -> true
                CourseLanguage.ENGLISH -> coupon.language.equals("English", ignoreCase = true)
                CourseLanguage.OTHERS -> !coupon.language.equals("English", ignoreCase = true)
            }

            val matchesRating = (coupon.rating ?: 0.0) >= filter.minRating

            matchesQuery && matchesCategory && matchesLang && matchesRating
        }

        return when (filter.sortBy) {
            SortOption.NEWEST -> filtered.sortedByDescending { it.createdAt ?: 0L }
            SortOption.RATING -> filtered.sortedByDescending { it.rating ?: 0.0 }
            SortOption.STUDENTS -> filtered.sortedByDescending { it.students ?: 0 }
            SortOption.REVIEWS -> filtered.sortedByDescending { it.reviews ?: 0 }
            SortOption.EXPIRING_SOON -> filtered.sortedBy { it.expiredTime ?: Long.MAX_VALUE }
        }
    }
}

class FakeDesktopSearchRepository : SearchRepository {
    private val recent = mutableListOf("Management", "Kotlin")
    private val suggestions = listOf(
        "Management",
        "Web Development",
        "Kotlin Multiplatform",
        "Procurement",
    )

    override suspend fun getRecentSearches(): List<String> = recent.toList()

    override suspend fun getMatchingHistory(query: String): List<String> =
        recent.filter { it.contains(query, ignoreCase = true) }

    override suspend fun getSearchSuggestions(query: String): List<String> =
        suggestions.filter { it.contains(query, ignoreCase = true) }

    override suspend fun saveSearchQuery(query: String) {
        if (query.isNotBlank()) {
            recent.remove(query)
            recent.add(0, query)
        }
    }

    override suspend fun rebuildKeywordCache() = Unit
}

@Suppress("TooManyFunctions")
class FakeDesktopAuthRepository : UserAuthenticationRepository {
    var localToken: String? = null
    var localRefreshToken: String? = null
    var cipherTextWrapper: String? = null
    var currentEmail: String = "brandnewuser@test.com"
    var isNewUserOnVerify: Boolean = true
    var isNewUserOnSocial: Boolean = false

    override suspend fun login(credentials: AuthCredentials): AppResult<TokenData> {
        currentEmail = credentials.username
        return AppResult.Success(
            TokenData(
                accessToken = "fake-jwt-${credentials.username}",
                refreshToken = "fake-refresh-${credentials.username}",
                isNewUser = false,
            )
        )
    }

    override suspend fun register(
        email: String,
        password: String,
        fullName: String?,
    ): AppResult<String> {
        currentEmail = email
        return AppResult.Success("Registered $email")
    }

    override suspend fun sendOtp(email: String): AppResult<String> {
        currentEmail = email
        return AppResult.Success("OTP sent to $email")
    }

    override suspend fun verifyOtp(email: String, otp: String): AppResult<TokenData> {
        currentEmail = email
        return if (otp == "000000") {
            AppResult.Success(
                TokenData(
                    accessToken = "fake-jwt-$email",
                    refreshToken = "fake-refresh-$email",
                    isNewUser = isNewUserOnVerify,
                )
            )
        } else {
            AppResult.Error(code = 400, message = "Invalid OTP code")
        }
    }

    override suspend fun refreshToken(refreshToken: String): AppResult<TokenData> =
        AppResult.Success(TokenData(accessToken = "refreshed-jwt", refreshToken = refreshToken))

    override suspend fun getLocalToken(): String? = localToken

    override suspend fun saveLocalToken(token: String) {
        localToken = token
    }

    override suspend fun clearLocalToken() {
        localToken = null
    }

    override suspend fun checkIfTokenExpired(): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun getCipherTextWrapper(): String? = cipherTextWrapper

    override suspend fun saveCipherTextWrapper(fingerprintToken: String) {
        cipherTextWrapper = fingerprintToken
    }

    override suspend fun clearCipherTextWrapper() {
        cipherTextWrapper = null
    }

    override suspend fun getLocalRefreshToken(): String? = localRefreshToken

    override suspend fun saveLocalRefreshToken(refreshToken: String) {
        localRefreshToken = refreshToken
    }

    override suspend fun clearLocalRefreshToken() {
        localRefreshToken = null
    }

    override suspend fun requestFingerprintToken(): AppResult<TokenData> =
        AppResult.Success(TokenData(accessToken = "fake-fingerprint-jwt"))

    override suspend fun requestAccessToken(): AppResult<TokenData> =
        AppResult.Success(TokenData(accessToken = "fake-access-jwt"))

    override suspend fun socialLogin(provider: String, idToken: String): AppResult<TokenData> {
        currentEmail = "google.user@coursedeals.com"
        return AppResult.Success(
            TokenData(
                accessToken = "fake-social-jwt",
                refreshToken = "fake-social-refresh",
                isNewUser = isNewUserOnSocial,
            )
        )
    }
}

@Suppress("TooManyFunctions")
class FakeDesktopUserProfileRepository(
    private val authRepository: FakeDesktopAuthRepository,
) : UserProfileRepository {
    var darkModeEnabled: Boolean = false
    var fingerprintEnabled: Boolean = false
    var notificationsEnabled: Boolean = true
    val favoriteCategories = linkedSetOf("Development")
    val favoriteKeywords = linkedSetOf("Kotlin")
    var termsAcceptedCount: Int = 0
    var accountDeletedCount: Int = 0

    override suspend fun getEnableDarkMode(): Boolean = darkModeEnabled

    override suspend fun saveEnableDarkMode(isDarkModeEnable: Boolean) {
        darkModeEnabled = isDarkModeEnable
    }

    override suspend fun getEnableFingerPrint(): Boolean = fingerprintEnabled

    override suspend fun saveEnableFingerPrint(isFingerPrintEnable: Boolean) {
        fingerprintEnabled = isFingerPrintEnable
    }

    override suspend fun syncPreferences(): AppResult<UserPreferences> =
        AppResult.Success(currentPreferences())

    override suspend fun getFavoriteCategories(): Set<String> = favoriteCategories.toSet()

    override suspend fun saveFavoriteCategories(categories: Set<String>): AppResult<UserPreferences> {
        favoriteCategories.clear()
        favoriteCategories.addAll(categories)
        return AppResult.Success(currentPreferences())
    }

    override suspend fun getFavoriteKeywords(): Set<String> = favoriteKeywords.toSet()

    override suspend fun saveFavoriteKeywords(keywords: Set<String>): AppResult<UserPreferences> {
        favoriteKeywords.clear()
        favoriteKeywords.addAll(keywords)
        return AppResult.Success(currentPreferences())
    }

    override suspend fun getNotificationsEnabled(): Boolean = notificationsEnabled

    override suspend fun saveNotificationsEnabled(enabled: Boolean): AppResult<UserPreferences> {
        notificationsEnabled = enabled
        return AppResult.Success(currentPreferences())
    }

    override suspend fun getUserProfile(): AppResult<UserProfile> =
        AppResult.Success(getCachedUserProfile())

    override suspend fun updateUserProfile(
        fullName: String?,
        avatarUrl: String?,
        fcmToken: String?,
    ): AppResult<UserProfile> = AppResult.Success(getCachedUserProfile())

    override suspend fun getCachedUserProfile(): UserProfile = UserProfile(
        id = 1,
        email = authRepository.currentEmail,
        fullName = "Course Deals Member",
        avatarUrl = null,
        preferences = currentPreferences(),
    )

    override suspend fun clearCachedUserProfile() = Unit

    override suspend fun deleteAccount(): AppResult<Unit> {
        accountDeletedCount++
        return AppResult.Success(Unit)
    }

    override suspend fun acceptTerms(): AppResult<Unit> {
        termsAcceptedCount++
        return AppResult.Success(Unit)
    }

    private fun currentPreferences(): UserPreferences = UserPreferences(
        categories = favoriteCategories.toList(),
        keywords = favoriteKeywords.toList(),
        notificationsEnabled = notificationsEnabled,
        updatedAt = System.currentTimeMillis(),
    )
}

class FakeDesktopFeatureFlagManager : FeatureFlagManager {
    private val _configFlow = MutableStateFlow(
        AppConfig(
            minSupportedVersionCode = 105,
            forceUpdateUrl = "https://coursedeals.thanh0x.com/download",
        )
    )
    override val configFlow: StateFlow<AppConfig> = _configFlow.asStateFlow()

    override suspend fun refresh(): Result<AppConfig> = Result.success(_configFlow.value)

    override fun boolean(key: String, default: Boolean): Boolean = default

    override fun double(key: String, default: Double): Double = default

    override fun string(key: String, default: String): String = default
}
