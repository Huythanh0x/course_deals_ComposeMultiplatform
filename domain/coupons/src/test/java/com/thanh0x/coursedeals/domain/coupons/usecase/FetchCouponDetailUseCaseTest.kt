package com.thanh0x.coursedeals.domain.coupons.usecase

import androidx.paging.PagingData
import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.coupons.Coupon
import com.thanh0x.coursedeals.domain.coupons.CouponMetadata
import com.thanh0x.coursedeals.domain.coupons.CouponRepository
import com.thanh0x.coursedeals.domain.coupons.FilterData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FetchCouponDetailUseCaseTest {

    @Test
    fun invoke_returnsCouponDetailFromRepository() = runBlocking {
        val expectedCoupon = Coupon(
            courseId = 42,
            title = "Test Course",
            author = "Test Author",
            category = "Design",
            contentLength = 60,
            couponCode = "TEST42",
            couponUrl = "https://example.com/course/42",
            description = "Description",
            expiredTime = 123456789L,
            heading = "Heading",
            language = "English",
            level = "Beginner",
            previewImage = "https://example.com/img.png",
            previewVideo = null,
            rating = 4.7,
            reviews = 80,
            students = 1200,
            subCategory = "UI/UX",
            usesRemaining = 10,
            isNew = true,
            cachedAt = 100000000L,
        )

        val fakeRepo = object : FakeCouponRepository() {
            override suspend fun fetchCouponDetail(courseId: Int): AppResult<Coupon> {
                return AppResult.Success(expectedCoupon)
            }
        }

        val useCase = FetchCouponDetailUseCase(fakeRepo)
        val result = useCase(42)

        assertTrue(result is AppResult.Success)
        assertEquals(expectedCoupon, (result as AppResult.Success).data)
        assertEquals(100000000L, result.data.cachedAt)
    }

    private open class FakeCouponRepository : CouponRepository {
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
