package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.domain.user.UserProfileRepository
import javax.inject.Inject

class SaveDarkModeUseCase @Inject constructor(private val userProfileRepository: UserProfileRepository) {
    suspend operator fun invoke(isDarkModeEnable: Boolean) {
        userProfileRepository.saveEnableDarkMode(isDarkModeEnable)
    }
}
