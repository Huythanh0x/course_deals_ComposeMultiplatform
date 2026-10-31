package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.domain.user.UserProfileRepository

class LoadDarkModeUseCase constructor(private val userProfileRepository: UserProfileRepository) {
    suspend operator fun invoke(): Boolean? {
        return userProfileRepository.getEnableDarkMode()
    }
}
