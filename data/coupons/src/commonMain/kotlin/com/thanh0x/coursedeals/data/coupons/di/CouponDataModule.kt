package com.thanh0x.coursedeals.data.coupons.di

import com.thanh0x.coursedeals.data.coupons.repository.CouponRepositoryImpl
import com.thanh0x.coursedeals.data.coupons.repository.SearchRepositoryImpl
import com.thanh0x.coursedeals.data.coupons.source.LocalCouponDataSource
import com.thanh0x.coursedeals.data.coupons.source.local.LocalCouponDataSourceImpl
import com.thanh0x.coursedeals.data.coupons.source.remote.CouponService
import com.thanh0x.coursedeals.data.coupons.source.remote.CouponServiceImpl
import com.thanh0x.coursedeals.data.coupons.source.remote.RemoteCouponDataSourceImpl
import com.thanh0x.coursedeals.domain.coupons.CouponRepository
import com.thanh0x.coursedeals.domain.coupons.SearchRepository
import com.thanh0x.coursedeals.domain.coupons.source.RemoteCouponDataSource
import org.koin.dsl.module

val couponDataModule = module {
    single<CouponService> { CouponServiceImpl(httpClient = get()) }
    single<LocalCouponDataSource> { LocalCouponDataSourceImpl() }
    single<RemoteCouponDataSource> { RemoteCouponDataSourceImpl(couponService = get()) }

    single<SearchRepository> {
        SearchRepositoryImpl(
            localCouponDataSource = get(),
        )
    }

    single<CouponRepository> {
        CouponRepositoryImpl(
            localCouponDataSource = get(),
            remoteCouponDataSource = get(),
            localSettingsDataSource = get(),
            couponService = get(),
            searchRepository = get(),
        )
    }
}
