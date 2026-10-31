package com.thanh0x.coursedeals.feature.detail

import androidx.paging.PagingData
import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.core.ui.util.NetworkUtil
import com.thanh0x.coursedeals.domain.coupons.Coupon
import com.thanh0x.coursedeals.domain.coupons.CouponMetadata
import com.thanh0x.coursedeals.domain.coupons.CouponRepository
import com.thanh0x.coursedeals.domain.coupons.FilterData
import com.thanh0x.coursedeals.domain.coupons.usecase.FetchCouponDetailUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CouponDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeCouponRepository: FakeCouponRepository
    private lateinit var fakeNetworkUtil: FakeNetworkUtil
    private lateinit var fetchCouponDetailUseCase: FetchCouponDetailUseCase
    private lateinit var viewModel: CouponDetailViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeCouponRepository = FakeCouponRepository()
        fakeNetworkUtil = FakeNetworkUtil()
        fetchCouponDetailUseCase = FetchCouponDetailUseCase(fakeCouponRepository)
        viewModel = CouponDetailViewModel(fetchCouponDetailUseCase, fakeNetworkUtil)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun fetchCouponDetail_whenSuccessfulWithCachedAt_updatesStateWithCacheInfo() = runTest {
        val cachedTime = 1700000000000L
        val testCoupon = Coupon(
            courseId = 123,
            title = "Cached Course",
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
            cachedAt = cachedTime,
        )
        fakeCouponRepository.detailResult = AppResult.Success(testCoupon)

        viewModel.fetchCouponDetail(123)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(testCoupon, state.coupon)
        assertTrue(state.isFromCache)
        assertEquals(cachedTime, state.cachedAt)
    }

    @Test
    fun fetchCouponDetail_whenSuccessfulWithoutCachedAt_setsIsFromCacheFalse() = runTest {
        val testCoupon = Coupon(
            courseId = 456,
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
            cachedAt = null,
        )
        fakeCouponRepository.detailResult = AppResult.Success(testCoupon)

        viewModel.fetchCouponDetail(456)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(testCoupon, state.coupon)
        assertFalse(state.isFromCache)
        assertNull(state.cachedAt)
    }

    @Test
    fun fetchCouponDetail_whenError_updatesErrorState() = runTest {
        fakeCouponRepository.detailResult = AppResult.Error("Network Failed")

        viewModel.fetchCouponDetail(999)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Network Failed", state.error)
        assertNull(state.coupon)
    }

    @Test
    fun checkIfInternetAvailable_updatesState() {
        fakeNetworkUtil.isAvailable = false
        viewModel.checkIfInternetAvailable()
        assertFalse(viewModel.uiState.value.isInternetAvailable)

        fakeNetworkUtil.isAvailable = true
        viewModel.checkIfInternetAvailable()
        assertTrue(viewModel.uiState.value.isInternetAvailable)
    }

    private class FakeCouponRepository : CouponRepository {
        var detailResult: AppResult<Coupon> = AppResult.Error("Not configured")

        override suspend fun getAllCoupons(): List<Coupon> = emptyList()
        override fun getCouponsPager(query: String?, filter: FilterData): Flow<PagingData<Coupon>> = emptyFlow()
        override fun getFilteredCountFlow(query: String?, filter: FilterData): Flow<Int> = emptyFlow()
        override suspend fun syncAllCoupons(force: Boolean) {}
        override suspend fun insertCoupon(coupon: Coupon) {}
        override suspend fun clearALlCoupons(): Int = 0
        override suspend fun requestPostANewCoupon(couponUrl: String): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun requestDeleteACoupon(couponUrl: String): AppResult<Unit> = AppResult.Success(Unit)
        override fun getMetadataFlow(): Flow<CouponMetadata> = emptyFlow()
        override fun getShowLocalFetchTime(): Flow<Boolean> = emptyFlow()
        override suspend fun saveShowLocalFetchTime(show: Boolean) {}
        override suspend fun fetchCouponDetail(courseId: Int): AppResult<Coupon> = detailResult
    }

    private class FakeNetworkUtil : NetworkUtil(null) {
        var isAvailable: Boolean = true
        override fun isInternetAvailable(): Boolean = isAvailable
    }
}
