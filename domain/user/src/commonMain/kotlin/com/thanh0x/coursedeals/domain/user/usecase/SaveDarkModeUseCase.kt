package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.domain.user.UserProfileRepository

class SaveDarkModeUseCase constructor(private val userProfileRepository: UserProfileRepository) {
    suspend operator fun invoke(isDarkModeEnable: Boolean) {
        userProfileRepository.saveEnableDarkMode(isDarkModeEnable)
    }
}
