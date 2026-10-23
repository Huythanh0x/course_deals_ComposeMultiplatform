package com.thanh0x.coursedeals.data.coupons.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.thanh0x.coursedeals.data.coupons.source.local.CouponDatabase
import com.thanh0x.coursedeals.core.common.Constant
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE ${Constant.COUPON_TABLE_NAME} ADD COLUMN cached_at INTEGER DEFAULT NULL")
        }
    }

    @Singleton
    @Provides
    fun provideDao(couponDatabase: CouponDatabase) = couponDatabase.couponDao()

    @Singleton
    @Provides
    fun provideSearchHistoryDao(couponDatabase: CouponDatabase) = couponDatabase.searchHistoryDao()

    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext context: Context) =
        Room
            .databaseBuilder(context, CouponDatabase::class.java, Constant.COUPON_DATABASE_NAME)
            .addMigrations(MIGRATION_6_7)
            .fallbackToDestructiveMigration(true)
            .build()
}
