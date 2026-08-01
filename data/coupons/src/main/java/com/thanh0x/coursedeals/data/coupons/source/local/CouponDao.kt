package com.thanh0x.coursedeals.data.coupons.source.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.thanh0x.coursedeals.data.coupons.model.Coupon
import com.thanh0x.coursedeals.core.common.Constant

@Dao
interface CouponDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: Coupon)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupons(coupons: List<Coupon>)

    @Query("SELECT * FROM ${Constant.COUPON_TABLE_NAME}")
    suspend fun getAllCoupons(): List<Coupon>

    @Query("SELECT * FROM ${Constant.COUPON_TABLE_NAME} ORDER BY created_at DESC")
    fun getPagingCoupons(): PagingSource<Int, Coupon>

    @Query(
        "SELECT * FROM ${Constant.COUPON_TABLE_NAME} " +
            "WHERE LOWER(title) LIKE '%' || :searchQuery || '%' ORDER BY created_at DESC",
    )
    fun queryCouponByName(searchQuery: String): PagingSource<Int, Coupon>

    @RawQuery(observedEntities = [Coupon::class])
    fun getFilteredCoupons(query: SupportSQLiteQuery): PagingSource<Int, Coupon>

    @RawQuery(observedEntities = [Coupon::class])
    fun getFilteredCount(query: SupportSQLiteQuery): kotlinx.coroutines.flow.Flow<Int>

    @Query("SELECT title FROM ${Constant.COUPON_TABLE_NAME} WHERE LOWER(title) LIKE '%' || :query || '%' LIMIT 10")
    suspend fun getSearchSuggestions(query: String): List<String>

    @Query("DELETE FROM ${Constant.COUPON_TABLE_NAME}")
    suspend fun clearAllCoupons(): Int
}
