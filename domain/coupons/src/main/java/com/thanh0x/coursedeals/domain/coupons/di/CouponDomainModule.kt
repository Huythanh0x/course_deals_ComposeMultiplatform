package com.thanh0x.coursedeals.domain.coupons.di

import com.thanh0x.coursedeals.domain.coupons.usecase.FetchCouponDetailUseCase
import com.thanh0x.coursedeals.domain.coupons.usecase.LoadJwtTokenUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val couponDomainModule = module {
    factoryOf(::FetchCouponDetailUseCase)
    factoryOf(::LoadJwtTokenUseCase)
}
