package com.thanh0x.coursedeals.data.coupons.sync

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

class CouponSyncManagerImplTest {

    @Test
    fun syncCoupons_delegatesToRepository_andReturnsSuccess() = runTest {
        val fakeRepo = FakeCouponRepository()
        val syncManager = CouponSyncManagerImpl(fakeRepo)

        val result = syncManager.syncCoupons(force = true)

        assertTrue(result is AppResult.Success)
        assertEquals(1, fakeRepo.syncCallCount)
        assertEquals(true, fakeRepo.lastForceValue)
    }

    @Test
    fun syncCoupons_whenRepositoryThrows_returnsError() = runTest {
        val fakeRepo = FakeCouponRepository(shouldThrow = true)
        val syncManager = CouponSyncManagerImpl(fakeRepo)

        val result = syncManager.syncCoupons(force = false)

        assertTrue(result is AppResult.Error)
        assertEquals("Network connection failed", result.message)
    }

    private class FakeCouponRepository(private val shouldThrow: Boolean = false) : CouponRepository {
        var syncCallCount = 0
        var lastForceValue: Boolean? = null

        override suspend fun syncAllCoupons(force: Boolean) {
            syncCallCount++
            lastForceValue = force
            if (shouldThrow) {
                throw RuntimeException("Network connection failed")
            }
        }

        override suspend fun getAllCoupons(): List<Coupon> = emptyList()
        override fun getCouponsPager(query: String?, filter: FilterData) = emptyFlow<androidx.paging.PagingData<Coupon>>()
        override fun getFilteredCountFlow(query: String?, filter: FilterData): Flow<Int> = emptyFlow()
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
