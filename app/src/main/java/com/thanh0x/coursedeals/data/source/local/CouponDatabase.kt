package com.thanh0x.coursedeals.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.thanh0x.coursedeals.data.model.Coupon
import com.thanh0x.coursedeals.data.model.RemoteKey

@Database(entities = [Coupon::class, RemoteKey::class], version = 3, exportSchema = false)
abstract class CouponDatabase : RoomDatabase() {
    abstract fun couponDao(): CouponDao
    abstract fun remoteKeyDao(): RemoteKeyDao
}
