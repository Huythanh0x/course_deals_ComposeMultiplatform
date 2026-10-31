package com.thanh0x.coursedeals.data.coupons.source

import androidx.paging.PagingSource
import com.thanh0x.coursedeals.data.coupons.model.Coupon
import com.thanh0x.coursedeals.data.coupons.model.SearchHistory
import com.thanh0x.coursedeals.domain.coupons.FilterData
import kotlinx.coroutines.flow.Flow

interface LocalCouponDataSource {
    suspend fun getAllCoupons(): List<Coupon>

    fun getPagingCoupons(): PagingSource<Int, Coupon>

    fun getFilteredCoupons(query: String?, filter: FilterData): PagingSource<Int, Coupon>

    fun getFilteredCount(query: String?, filter: FilterData): Flow<Int>

    suspend fun getSearchSuggestions(query: String): List<String>

    suspend fun getCouponById(courseId: Int): Coupon?

    suspend fun clearExpiredCachedAt(expirationThreshold: Long): Int

    suspend fun insertCoupon(coupon: Coupon)

    suspend fun insertCoupons(coupons: List<Coupon>)

    suspend fun clearALlCoupons(): Int

    suspend fun getRecentSearches(): List<String> = emptyList()

    suspend fun getMatchingHistory(query: String): List<String> = emptyList()

    suspend fun insertSearchHistory(history: SearchHistory) {}
}
