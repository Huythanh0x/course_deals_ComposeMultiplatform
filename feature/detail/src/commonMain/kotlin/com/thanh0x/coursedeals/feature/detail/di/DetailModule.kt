package com.thanh0x.coursedeals.feature.detail.di

import com.thanh0x.coursedeals.feature.detail.CouponDetailViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val detailModule = module {
    factoryOf(::CouponDetailViewModel)
}
