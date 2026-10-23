package com.thanh0x.coursedeals.feature.detail

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.core.ui.UiEvent
import com.thanh0x.coursedeals.core.ui.util.NetworkUtil
import com.thanh0x.coursedeals.domain.coupons.Coupon
import com.thanh0x.coursedeals.domain.coupons.CouponMetadata
import com.thanh0x.coursedeals.domain.coupons.CouponRepository
import com.thanh0x.coursedeals.domain.coupons.FilterData
import com.thanh0x.coursedeals.domain.coupons.usecase.FetchCouponDetailUseCase
import com.thanh0x.coursedeals.domain.coupons.usecase.ReportCourseUseCase
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CouponDetailViewModelTest {

    private lateinit var fakeCouponRepository: FakeCouponRepository
    private lateinit var fetchCouponDetailUseCase: FetchCouponDetailUseCase
    private lateinit var reportCourseUseCase: ReportCourseUseCase
    private lateinit var fakeNetworkUtil: FakeNetworkUtil
    private lateinit var viewModel: CouponDetailViewModel

    @Before
    fun setUp() {
        fakeCouponRepository = FakeCouponRepository()
        fetchCouponDetailUseCase = FetchCouponDetailUseCase(fakeCouponRepository)
        reportCourseUseCase = ReportCourseUseCase(fakeCouponRepository)
        fakeNetworkUtil = FakeNetworkUtil()
        viewModel = CouponDetailViewModel(
            fetchCouponDetailUseCase = fetchCouponDetailUseCase,
            reportCourseUseCase = reportCourseUseCase,
            networkUtil = fakeNetworkUtil,
        )
    }

    @Test
    fun reportCourse_success_emitsSuccessEventAndUpdatesState() = runBlocking {
        fakeCouponRepository.reportResult = AppResult.Success(Unit)

        var emittedEvent: UiEvent? = null
        val job = launch {
            viewModel.uiEvent.collect {
                emittedEvent = it
            }
        }

        viewModel.reportCourse(courseId = 123L, reason = "Not free", details = "Price is $10")

        // Wait brief moment for coroutines on Dispatchers.IO to finish
        var attempts = 0
        while (emittedEvent == null && attempts < 20) {
            kotlinx.coroutines.delay(50)
            attempts++
        }

        job.cancel()

        assertEquals(123L, fakeCouponRepository.lastReportedCourseId)
        assertEquals("Not free", fakeCouponRepository.lastReportedReason)
        assertEquals("Price is $10", fakeCouponRepository.lastReportedDetails)
        assertFalse(viewModel.uiState.value.isReporting)
        assertTrue(emittedEvent is UiEvent.ShowToast)
        assertEquals("ReportSubmittedSuccess", (emittedEvent as UiEvent.ShowToast).message)
    }

    @Test
    fun reportCourse_error_emitsErrorEventAndResetsReportingState() = runBlocking {
        fakeCouponRepository.reportResult = AppResult.Error("Network failure", 500)

        var emittedEvent: UiEvent? = null
        val job = launch {
            viewModel.uiEvent.collect {
                emittedEvent = it
            }
        }

        viewModel.reportCourse(courseId = 456L, reason = "Expired", details = null)

        var attempts = 0
        while (emittedEvent == null && attempts < 20) {
            kotlinx.coroutines.delay(50)
            attempts++
        }

        job.cancel()

        assertEquals(456L, fakeCouponRepository.lastReportedCourseId)
        assertEquals("Expired", fakeCouponRepository.lastReportedReason)
        assertNull(fakeCouponRepository.lastReportedDetails)
        assertFalse(viewModel.uiState.value.isReporting)
        assertTrue(emittedEvent is UiEvent.ShowToast)
        assertEquals("Network failure", (emittedEvent as UiEvent.ShowToast).message)
    }

    private class FakeNetworkUtil : NetworkUtil() {
        var isAvailable = true
        override fun isInternetAvailable(): Boolean = isAvailable
    }

    private class FakeCouponRepository : CouponRepository {
        var reportResult: AppResult<Unit> = AppResult.Success(Unit)
        var lastReportedCourseId: Long? = null
        var lastReportedReason: String? = null
        var lastReportedDetails: String? = null

        override suspend fun getAllCoupons(): List<Coupon> = emptyList()
        override fun getCouponsPager(query: String?, filter: FilterData): Flow<PagingData<Coupon>> =
            flowOf(PagingData.empty())
        override fun getFilteredCountFlow(query: String?, filter: FilterData): Flow<Int> = flowOf(0)
        override suspend fun syncAllCoupons(force: Boolean) {}
        override suspend fun insertCoupon(coupon: Coupon) {}
        override suspend fun clearALlCoupons(): Int = 0
        override suspend fun requestPostANewCoupon(couponUrl: String): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun requestDeleteACoupon(couponUrl: String): AppResult<Unit> = AppResult.Success(Unit)
        override fun getMetadataFlow(): Flow<CouponMetadata> = flowOf(CouponMetadata(0, 0L, 0L))
        override fun getShowLocalFetchTime(): Flow<Boolean> = flowOf(false)
        override suspend fun saveShowLocalFetchTime(show: Boolean) {}
        override suspend fun fetchCouponDetail(courseId: Int): AppResult<Coupon> = AppResult.Error("Not configured")

        override suspend fun reportCourse(
            courseId: Long,
            reason: String,
            details: String?,
        ): AppResult<Unit> {
            lastReportedCourseId = courseId
            lastReportedReason = reason
            lastReportedDetails = details
            return reportResult
        }
    }
}
