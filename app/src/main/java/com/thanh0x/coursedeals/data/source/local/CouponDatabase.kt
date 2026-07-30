package com.thanh0x.coursedeals.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.thanh0x.coursedeals.data.model.Coupon
@Database(entities = [Coupon::class], version = 4, exportSchema = false)
abstract class CouponDatabase : RoomDatabase() {
    abstract fun couponDao(): CouponDao
}
