package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.domain.user.UserProfileRepository
import javax.inject.Inject

class LoadDarkModeUseCase @Inject constructor(private val userProfileRepository: UserProfileRepository) {
    suspend operator fun invoke(): Boolean? {
        return userProfileRepository.getEnableDarkMode()
    }
}
