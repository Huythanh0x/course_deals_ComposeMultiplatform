package com.thanh0x.coursedeals.feature.profile.di

import com.thanh0x.coursedeals.feature.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
    viewModel {
        ProfileViewModel(
            userProfileRepository = get(),
            getUserProfileUseCase = get(),
            saveJwtTokenUseCase = get(),
            cryptographyManagerUseCase = get(),
            settingFingerprintUseCase = get(),
            requestFingerprintTokenUseCase = get(),
            clearLocalTokenUseCase = get(),
            checkIfTokenExpiredUseCase = get(),
            getLocalTokenUseCase = get(),
            networkUtil = get()
        )
    }
}
