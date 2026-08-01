package com.thanh0x.coursedeals.data.source

import androidx.paging.PagingSource
import com.thanh0x.coursedeals.data.model.Coupon
import com.thanh0x.coursedeals.domain.coupons.FilterData

interface LocalCouponDataSource {
    suspend fun getAllCoupons(): List<Coupon>

    fun getPagingCoupons(): PagingSource<Int, Coupon>

    fun getFilteredCoupons(query: String?, filter: FilterData): PagingSource<Int, Coupon>

    fun getFilteredCount(query: String?, filter: FilterData): kotlinx.coroutines.flow.Flow<Int>

    suspend fun getSearchSuggestions(query: String): List<String>

    suspend fun insertCoupon(coupon: Coupon)

    suspend fun insertCoupons(coupons: List<Coupon>)

    suspend fun clearALlCoupons(): Int
}
