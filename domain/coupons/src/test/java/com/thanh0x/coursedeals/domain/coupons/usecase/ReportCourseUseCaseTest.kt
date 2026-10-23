package com.thanh0x.coursedeals.domain.coupons.usecase

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.coupons.Coupon
import com.thanh0x.coursedeals.domain.coupons.CouponMetadata
import com.thanh0x.coursedeals.domain.coupons.CouponRepository
import com.thanh0x.coursedeals.domain.coupons.FilterData
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReportCourseUseCaseTest {

    private lateinit var fakeCouponRepository: FakeCouponRepository
    private lateinit var reportCourseUseCase: ReportCourseUseCase

    @Before
    fun setUp() {
        fakeCouponRepository = FakeCouponRepository()
        reportCourseUseCase = ReportCourseUseCase(fakeCouponRepository)
    }

    @Test
    fun reportCourse_success_delegatesToRepository() = runBlocking {
        fakeCouponRepository.reportResult = AppResult.Success(Unit)

        val result = reportCourseUseCase(
            courseId = 12345L,
            reason = "Not free",
            details = "Course has expired promo code",
        )

        assertTrue(result is AppResult.Success)
        assertEquals(12345L, fakeCouponRepository.lastReportedCourseId)
        assertEquals("Not free", fakeCouponRepository.lastReportedReason)
        assertEquals("Course has expired promo code", fakeCouponRepository.lastReportedDetails)
    }

    @Test
    fun reportCourse_error_returnsErrorFromRepository() = runBlocking {
        fakeCouponRepository.reportResult = AppResult.Error("Course not found", 404)

        val result = reportCourseUseCase(
            courseId = 99999L,
            reason = "Other",
            details = null,
        )

        assertTrue(result is AppResult.Error)
        assertEquals("Course not found", (result as AppResult.Error).message)
        assertEquals(404, result.code)
        assertEquals(99999L, fakeCouponRepository.lastReportedCourseId)
        assertEquals("Other", fakeCouponRepository.lastReportedReason)
        assertEquals(null, fakeCouponRepository.lastReportedDetails)
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
