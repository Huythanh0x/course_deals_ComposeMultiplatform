package com.thanh0x.coursedeals.di

import android.content.Context
import androidx.room.Room
import com.thanh0x.coursedeals.data.source.local.CouponDatabase
import com.thanh0x.coursedeals.util.Constant
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
    fun provideDataBase(@ApplicationContext context: Context) =
        Room
            .databaseBuilder(context, CouponDatabase::class.java, Constant.COUPON_DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

}