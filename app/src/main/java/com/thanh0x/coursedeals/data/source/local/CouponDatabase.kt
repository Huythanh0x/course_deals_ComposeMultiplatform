package com.thanh0x.coursedeals.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.thanh0x.coursedeals.data.model.Coupon
import com.thanh0x.coursedeals.data.model.SearchHistory

@Database(entities = [Coupon::class, SearchHistory::class], version = 6, exportSchema = false)
abstract class CouponDatabase : RoomDatabase() {
    abstract fun couponDao(): CouponDao
    abstract fun searchHistoryDao(): SearchHistoryDao
}
