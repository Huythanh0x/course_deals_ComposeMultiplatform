package com.thanh0x.coursedeals.feature.detail.di

import com.thanh0x.coursedeals.feature.detail.CouponDetailViewModel
import org.koin.dsl.module

val detailModule = module {
    factory {
        CouponDetailViewModel(
            fetchCouponDetailUseCase = get(),
            reportCourseUseCase = get(),
            networkUtil = get(),
        )
    }
}
