package com.batdaulaptrinh.freeudemycoupons.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.batdaulaptrinh.freeudemycoupons.data.model.Coupon
import com.batdaulaptrinh.freeudemycoupons.util.Constant

@Dao
interface CouponDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: Coupon)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupons(coupons: List<Coupon>)

    @Query("SELECT * FROM ${Constant.COUPON_TABLE_NAME}")
    suspend fun getAllCoupons(): List<Coupon>

    @Query("SELECT * FROM ${Constant.COUPON_TABLE_NAME} WHERE LOWER(title) LIKE '%' || :searchQuery || '%'")
    suspend fun queryCouponByName(searchQuery: String): List<Coupon>

    @Query("DELETE FROM ${Constant.COUPON_TABLE_NAME}")
    suspend fun clearAllCoupons(): Int
}