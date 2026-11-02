package com.thanh0x.coursedeals.feature.splash.di

import com.thanh0x.coursedeals.feature.splash.SplashViewModel
import org.koin.dsl.module

val splashModule = module {
    factory {
        SplashViewModel(
            featureFlagManager = get(),
            getLocalTokenUseCase = get(),
        )
    }
}
