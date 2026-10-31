package com.thanh0x.coursedeals.di

import com.thanh0x.coursedeals.core.network.di.coreNetworkModule
import com.thanh0x.coursedeals.core.ui.util.NetworkUtil
import com.thanh0x.coursedeals.data.coupons.di.couponDataModule
import com.thanh0x.coursedeals.data.user.di.userDataModule
import com.thanh0x.coursedeals.domain.coupons.di.couponDomainModule
import com.thanh0x.coursedeals.domain.user.di.userDomainModule
import com.thanh0x.coursedeals.feature.auth.di.authModule
import com.thanh0x.coursedeals.feature.course.di.courseModule
import com.thanh0x.coursedeals.feature.detail.di.detailModule
import com.thanh0x.coursedeals.feature.home.di.homeModule
import com.thanh0x.coursedeals.feature.profile.di.profileModule
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val utilModule = module {
    single { NetworkUtil(androidContext()) }
}

val appModules = listOf(
    coreNetworkModule,
    couponDataModule,
    userDataModule,
    couponDomainModule,
    userDomainModule,
    homeModule,
    courseModule,
    detailModule,
    authModule,
    profileModule,
    utilModule,
)
