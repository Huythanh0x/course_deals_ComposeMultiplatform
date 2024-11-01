package com.batdaulaptrinh.freeudemycoupons.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.batdaulaptrinh.freeudemycoupons.data.model.Coupon

@Database(entities = [Coupon::class], version = 1, exportSchema = false)
abstract class CouponDatabase : RoomDatabase() {
    abstract fun couponDao(): CouponDao
}