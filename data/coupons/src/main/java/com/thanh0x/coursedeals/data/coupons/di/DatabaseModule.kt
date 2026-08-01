package com.thanh0x.coursedeals.data.coupons.di

import android.content.Context
import androidx.room.Room
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
            .fallbackToDestructiveMigration(true)
            .build()
}
