package com.thanh0x.coursedeals.feature.auth.di

import com.thanh0x.coursedeals.feature.auth.LoginViewModel
import org.koin.dsl.module

val authModule = module {
    factory {
        LoginViewModel(
            loginUseCase = get(),
            registerUseCase = get(),
            verifyOtpUseCase = get(),
            sendOtpUseCase = get(),
            socialLoginUseCase = get(),
            saveJwtTokenUseCase = get(),
            saveRefreshTokenUseCase = get(),
            settingFingerprintUseCase = get(),
            cryptographyManagerUseCase = get(),
            clearLocalTokenUseCase = get(),
            clearCachedUserProfileUseCase = get(),
            requestAccessTokenUseCase = get(),
            settingUserProfileUseCase = get(),
            networkUtil = get(),
        )
    }
}
