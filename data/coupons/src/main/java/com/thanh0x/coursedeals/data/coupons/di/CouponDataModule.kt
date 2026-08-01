package com.thanh0x.coursedeals.data.coupons.di

import com.thanh0x.coursedeals.data.coupons.repository.CouponRepositoryImpl
import com.thanh0x.coursedeals.data.coupons.repository.SearchRepositoryImpl
import com.thanh0x.coursedeals.data.coupons.source.LocalCouponDataSource
import com.thanh0x.coursedeals.data.coupons.source.local.LocalCouponDataSourceImpl
import com.thanh0x.coursedeals.data.coupons.source.remote.RemoteCouponDataSourceImpl
import com.thanh0x.coursedeals.data.coupons.source.remote.CouponService
import com.thanh0x.coursedeals.domain.coupons.CouponRepository
import com.thanh0x.coursedeals.domain.coupons.SearchRepository
import com.thanh0x.coursedeals.domain.coupons.source.RemoteCouponDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CouponDataModule {

    @Binds
    @Singleton
    abstract fun bindCouponRepository(
        impl: CouponRepositoryImpl
    ): CouponRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        impl: SearchRepositoryImpl
    ): SearchRepository

    @Binds
    @Singleton
    abstract fun bindLocalCouponDataSource(
        impl: LocalCouponDataSourceImpl
    ): LocalCouponDataSource

    @Binds
    @Singleton
    abstract fun bindRemoteCouponDataSource(
        impl: RemoteCouponDataSourceImpl
    ): RemoteCouponDataSource

    companion object {
        @Provides
        @Singleton
        fun provideCouponService(retrofit: Retrofit): CouponService =
            retrofit.create(CouponService::class.java)
    }
}
