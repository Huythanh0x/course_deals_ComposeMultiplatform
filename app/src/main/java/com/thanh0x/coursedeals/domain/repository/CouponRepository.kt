package com.thanh0x.coursedeals.domain.repository

import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.thanh0x.coursedeals.domain.model.AppResult
import com.thanh0x.coursedeals.domain.model.Coupon
import com.thanh0x.coursedeals.domain.model.CouponMetadata
import com.thanh0x.coursedeals.domain.model.FilterData
import kotlinx.coroutines.flow.Flow

interface CouponRepository {
    suspend fun getAllCoupons(): List<Coupon>

    fun getCouponsPager(query: String? = null, filter: FilterData = FilterData()): Flow<PagingData<Coupon>>

    fun getFilteredCountFlow(query: String?, filter: FilterData): Flow<Int>

    suspend fun syncAllCoupons(force: Boolean = false)

    suspend fun insertCoupon(coupon: Coupon)

    suspend fun clearALlCoupons(): Int

    suspend fun requestPostANewCoupon(couponUrl: String): AppResult<Unit>

    suspend fun requestDeleteACoupon(couponUrl: String): AppResult<Unit>

    fun getMetadataFlow(): Flow<CouponMetadata>

    fun getShowLocalFetchTime(): Flow<Boolean>

    suspend fun saveShowLocalFetchTime(show: Boolean)

    suspend fun fetchCouponDetail(courseId: Int): AppResult<Coupon>
}
