package com.thanh0x.coursedeals.data.coupons.repository

import androidx.paging.PagingSource
import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.data.coupons.model.Coupon as CouponEntity
import com.thanh0x.coursedeals.data.coupons.model.CourseDealResponse
import com.thanh0x.coursedeals.data.coupons.model.CouponDto
import com.thanh0x.coursedeals.data.coupons.model.ResponseStatusFromServer
import com.thanh0x.coursedeals.data.coupons.source.LocalCouponDataSource
import com.thanh0x.coursedeals.data.coupons.source.local.CouponDatabase
import com.thanh0x.coursedeals.data.coupons.source.remote.CouponService
import com.thanh0x.coursedeals.domain.coupons.Coupon
import com.thanh0x.coursedeals.domain.coupons.FilterData
import com.thanh0x.coursedeals.domain.coupons.SearchRepository
import com.thanh0x.coursedeals.domain.coupons.source.RemoteCouponDataSource
import com.thanh0x.coursedeals.domain.user.source.LocalSettingsDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.thanh0x.coursedeals.core.network.NetworkResponse

class CouponRepositoryImplTest {

    private lateinit var fakeLocalCouponDataSource: FakeLocalCouponDataSource
    private lateinit var fakeRemoteCouponDataSource: FakeRemoteCouponDataSource
    private lateinit var fakeLocalSettingsDataSource: FakeLocalSettingsDataSource
    private lateinit var fakeSearchRepository: FakeSearchRepository
    private lateinit var repository: CouponRepositoryImpl

    @Before
    fun setUp() {
        fakeLocalCouponDataSource = FakeLocalCouponDataSource()
        fakeRemoteCouponDataSource = FakeRemoteCouponDataSource()
        fakeLocalSettingsDataSource = FakeLocalSettingsDataSource()
        fakeSearchRepository = FakeSearchRepository()

        repository = CouponRepositoryImpl(
            localCouponDataSource = fakeLocalCouponDataSource,
            remoteCouponDataSource = fakeRemoteCouponDataSource,
            localSettingsDataSource = fakeLocalSettingsDataSource,
            couponDatabase = FakeCouponDatabaseStub.create(),
            couponService = FakeCouponService(),
            searchRepository = fakeSearchRepository,
        )
    }

    @Test
    fun fetchCouponDetail_whenRemoteSucceeds_cachesAndReturnsCouponWithoutCachedAt() = runBlocking {
        val remoteCoupon = Coupon(
            courseId = 101,
            author = "Author Test",
            category = "Development",
            contentLength = 120,
            couponCode = "CODE101",
            couponUrl = "https://example.com/101",
            description = "Description 101",
            expiredTime = System.currentTimeMillis() + 100000L,
            heading = "Heading 101",
            language = "English",
            level = "All Levels",
            previewImage = "https://example.com/img.png",
            previewVideo = null,
            rating = 4.8,
            reviews = 200,
            students = 5000,
            subCategory = "Kotlin",
            title = "Kotlin Mastery",
            usesRemaining = 15,
            isNew = true,
        )
        fakeRemoteCouponDataSource.detailResult = AppResult.Success(remoteCoupon)

        val beforeCall = System.currentTimeMillis()
        val result = repository.fetchCouponDetail(101)
        val afterCall = System.currentTimeMillis()

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals(101, data.courseId)
        assertEquals("Kotlin Mastery", data.title)
        assertNull(data.cachedAt)

        // Verify it was stored in local Room cache with cachedAt
        val cachedInDb = fakeLocalCouponDataSource.getCouponById(101)
        assertNotNull(cachedInDb)
        assertEquals(101, cachedInDb?.courseId)
        assertNotNull(cachedInDb?.cachedAt)
        assertTrue(cachedInDb!!.cachedAt!! in beforeCall..afterCall)
    }

    @Test
    fun fetchCouponDetail_whenRemoteSucceeds_cleansUpExpiredCache() = runBlocking {
        val now = System.currentTimeMillis()
        val expiredEntity = CouponEntity(
            courseId = 102,
            title = "Expired Course",
            cachedAt = now - (25 * 60 * 60 * 1000L),
        )
        fakeLocalCouponDataSource.insertCoupon(expiredEntity)

        val remoteCoupon = Coupon(
            courseId = 101,
            title = "Fresh Course",
            author = "Author",
            category = "Dev",
            contentLength = 60,
            couponCode = "CODE",
            couponUrl = "https://example.com",
            description = "Desc",
            expiredTime = null,
            heading = null,
            language = "English",
            level = "All",
            previewImage = null,
            previewVideo = null,
            rating = 4.5,
            reviews = 10,
            students = 100,
            subCategory = null,
            usesRemaining = null,
            isNew = null,
        )
        fakeRemoteCouponDataSource.detailResult = AppResult.Success(remoteCoupon)

        val result = repository.fetchCouponDetail(101)
        assertTrue(result is AppResult.Success)

        // Expired cache should have cachedAt set to null by clearExpiredCachedAt
        val checkExpired = fakeLocalCouponDataSource.getCouponById(102)
        assertNull(checkExpired?.cachedAt)
    }

    @Test
    fun fetchCouponDetail_whenRemoteFails_returnsCachedDataIfWithin24Hours() = runBlocking {
        val now = System.currentTimeMillis()
        val cachedAtTwoHoursAgo = now - (2 * 60 * 60 * 1000L) // 2 hours ago
        val cachedEntity = CouponEntity(
            courseId = 202,
            title = "Cached Python Course",
            cachedAt = cachedAtTwoHoursAgo,
        )
        fakeLocalCouponDataSource.insertCoupon(cachedEntity)
        fakeRemoteCouponDataSource.detailResult = AppResult.Error("Network offline", 503)

        val result = repository.fetchCouponDetail(202)

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals(202, data.courseId)
        assertEquals("Cached Python Course", data.title)
        assertEquals(cachedAtTwoHoursAgo, data.cachedAt)
    }

    @Test
    fun fetchCouponDetail_whenRemoteThrows_returnsCachedDataIfWithin24Hours() = runBlocking {
        val now = System.currentTimeMillis()
        val cachedAtOneHourAgo = now - (1 * 60 * 60 * 1000L)
        val cachedEntity = CouponEntity(
            courseId = 303,
            title = "Cached Android Course",
            cachedAt = cachedAtOneHourAgo,
        )
        fakeLocalCouponDataSource.insertCoupon(cachedEntity)
        fakeRemoteCouponDataSource.shouldThrow = true

        val result = repository.fetchCouponDetail(303)

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals(303, data.courseId)
        assertEquals("Cached Android Course", data.title)
        assertEquals(cachedAtOneHourAgo, data.cachedAt)
    }

    @Test
    fun fetchCouponDetail_whenRemoteFails_andCacheExpired_returnsOriginalError() = runBlocking {
        val now = System.currentTimeMillis()
        val cachedAt25HoursAgo = now - (25 * 60 * 60 * 1000L) // 25 hours ago (> 24h)
        val cachedEntity = CouponEntity(
            courseId = 404,
            title = "Expired Course",
            cachedAt = cachedAt25HoursAgo,
        )
        fakeLocalCouponDataSource.insertCoupon(cachedEntity)
        fakeRemoteCouponDataSource.detailResult = AppResult.Error("Service Unavailable", 503)

        val result = repository.fetchCouponDetail(404)

        assertTrue(result is AppResult.Error)
        val error = result as AppResult.Error
        assertEquals("Service Unavailable", error.message)
        assertEquals(503, error.code)
    }

    @Test
    fun fetchCouponDetail_whenRemoteFails_andNoCacheExists_returnsOriginalError() = runBlocking {
        fakeRemoteCouponDataSource.detailResult = AppResult.Error("Not Found", 404)

        val result = repository.fetchCouponDetail(999)

        assertTrue(result is AppResult.Error)
        val error = result as AppResult.Error
        assertEquals("Not Found", error.message)
    }

    private class FakeLocalCouponDataSource : LocalCouponDataSource {
        private val storage = mutableMapOf<Int, CouponEntity>()

        override suspend fun getAllCoupons(): List<CouponEntity> = storage.values.toList()

        override fun getPagingCoupons(): PagingSource<Int, CouponEntity> =
            throw UnsupportedOperationException()

        override fun getFilteredCoupons(
            query: String?,
            filter: FilterData,
        ): PagingSource<Int, CouponEntity> = throw UnsupportedOperationException()

        override fun getFilteredCount(query: String?, filter: FilterData): Flow<Int> =
            flowOf(storage.size)

        override suspend fun getSearchSuggestions(query: String): List<String> = emptyList()

        override suspend fun getCouponById(courseId: Int): CouponEntity? = storage[courseId]

        override suspend fun clearExpiredCachedAt(expirationThreshold: Long): Int {
            var updatedCount = 0
            storage.forEach { (id, coupon) ->
                val cachedAt = coupon.cachedAt
                if (cachedAt != null && cachedAt < expirationThreshold) {
                    storage[id] = coupon.copy(cachedAt = null)
                    updatedCount++
                }
            }
            return updatedCount
        }

        override suspend fun insertCoupon(coupon: CouponEntity) {
            storage[coupon.courseId] = coupon
        }

        override suspend fun insertCoupons(coupons: List<CouponEntity>) {
            coupons.forEach { storage[it.courseId] = it }
        }

        override suspend fun clearALlCoupons(): Int {
            val count = storage.size
            storage.clear()
            return count
        }
    }

    private class FakeRemoteCouponDataSource : RemoteCouponDataSource {
        var detailResult: AppResult<Coupon> = AppResult.Error("Not implemented")
        var shouldThrow: Boolean = false

        override suspend fun requestPostANewCoupon(couponUrl: String): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun requestDeleteACoupon(couponUrl: String): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun fetchCouponDetail(courseId: Int): AppResult<Coupon> {
            if (shouldThrow) {
                throw RuntimeException("Network timeout")
            }
            return detailResult
        }
    }

    private class FakeLocalSettingsDataSource : LocalSettingsDataSource {
        private var showFetchTime = true
        override fun getShowLocalFetchTime(): Flow<Boolean> = flowOf(showFetchTime)
        override suspend fun saveShowLocalFetchTime(show: Boolean) {
            showFetchTime = show
        }
    }

    private class FakeSearchRepository : SearchRepository {
        override suspend fun getRecentSearches(): List<String> = emptyList()
        override suspend fun getMatchingHistory(query: String): List<String> = emptyList()
        override suspend fun getSearchSuggestions(query: String): List<String> = emptyList()
        override suspend fun saveSearchQuery(query: String) {}
        override suspend fun rebuildKeywordCache() {}
    }

    private class FakeCouponService : CouponService {
        override suspend fun fetchAllCoupons(): NetworkResponse<CourseDealResponse> =
            throw UnsupportedOperationException()

        override suspend fun fetchPagedCoupons(
            pageIndex: Int,
            numberPerPage: Int,
        ): NetworkResponse<CourseDealResponse> = throw UnsupportedOperationException()

        override suspend fun fetchCouponDetail(courseId: Int): NetworkResponse<CouponDto> =
            throw UnsupportedOperationException()

        override suspend fun fetchEnhancedCouponDetail(courseId: Int): NetworkResponse<CouponDto> =
            throw UnsupportedOperationException()

        override suspend fun searchCourseCoupon(
            query: String,
            pageIndex: Int,
            numberPerPage: Int,
        ): NetworkResponse<CourseDealResponse> = throw UnsupportedOperationException()

        override suspend fun postCoupon(couponUrl: String): NetworkResponse<ResponseStatusFromServer> =
            throw UnsupportedOperationException()

        override suspend fun deleteCoupon(couponUrl: String): NetworkResponse<ResponseStatusFromServer> =
            throw UnsupportedOperationException()
    }

    private object FakeCouponDatabaseStub {
        fun create(): CouponDatabase {
            val mockObj = object : CouponDatabase() {
                override fun couponDao() = throw UnsupportedOperationException()
                override fun searchHistoryDao() = throw UnsupportedOperationException()
                override fun clearAllTables() {}
                override fun createInvalidationTracker() = throw UnsupportedOperationException()
                override fun createOpenDelegate() = throw UnsupportedOperationException()
            }
            return mockObj
        }
    }
}
