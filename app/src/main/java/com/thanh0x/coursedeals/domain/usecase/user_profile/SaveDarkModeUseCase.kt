package com.batdaulaptrinh.freeudemycoupons.domain.usecase.user_profile

import com.batdaulaptrinh.freeudemycoupons.data.repository.UserProfileRepositoryImpl
import javax.inject.Inject

class SaveDarkModeUseCase @Inject constructor(private val userProfileRepositoryImpl: UserProfileRepositoryImpl) {
    suspend operator fun invoke(isDarkModeEnable: Boolean) {
        userProfileRepositoryImpl.saveEnableDarkMode(isDarkModeEnable)
    }
}
