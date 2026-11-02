package com.thanh0x.coursedeals.domain.coupons.usecase

import androidx.paging.PagingData
import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.coupons.Coupon
import com.thanh0x.coursedeals.domain.coupons.CouponMetadata
import com.thanh0x.coursedeals.domain.coupons.CouponRepository
import com.thanh0x.coursedeals.domain.coupons.FilterData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReportCourseUseCaseTest {

    @Test
    fun invoke_whenRepositorySucceeds_returnsSuccess() = runTest {
        val fakeRepo = FakeCouponRepository().apply {
            reportResult = AppResult.Success(Unit)
        }
        val useCase = ReportCourseUseCase(fakeRepo)

        val result = useCase(courseId = 101, reason = "NOT_FREE", details = "Code expired")

        assertTrue(result is AppResult.Success)
        assertEquals(101, fakeRepo.lastReportedCourseId)
        assertEquals("NOT_FREE", fakeRepo.lastReportedReason)
        assertEquals("Code expired", fakeRepo.lastReportedDetails)
    }

    @Test
    fun invoke_whenRepositoryFails_returnsError() = runTest {
        val fakeRepo = FakeCouponRepository().apply {
            reportResult = AppResult.Error("Network error", 500)
        }
        val useCase = ReportCourseUseCase(fakeRepo)

        val result = useCase(courseId = 202, reason = "EXPIRED", details = null)

        assertTrue(result is AppResult.Error)
        assertEquals("Network error", result.message)
        assertEquals(500, result.code)
        assertEquals(202, fakeRepo.lastReportedCourseId)
        assertEquals("EXPIRED", fakeRepo.lastReportedReason)
        assertEquals(null, fakeRepo.lastReportedDetails)
    }

    private class FakeCouponRepository : CouponRepository {
        var reportResult: AppResult<Unit> = AppResult.Success(Unit)
        var lastReportedCourseId: Int? = null
        var lastReportedReason: String? = null
        var lastReportedDetails: String? = null

        override suspend fun reportCourse(
            courseId: Int,
            reason: String,
            details: String?,
        ): AppResult<Unit> {
            lastReportedCourseId = courseId
            lastReportedReason = reason
            lastReportedDetails = details
            return reportResult
        }

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
        override suspend fun fetchCouponDetail(courseId: Int): AppResult<Coupon> = AppResult.Error("Not implemented")
    }
}
