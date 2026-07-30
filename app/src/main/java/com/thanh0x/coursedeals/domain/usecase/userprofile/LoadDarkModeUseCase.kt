package com.thanh0x.coursedeals.domain.usecase.userprofile

import com.thanh0x.coursedeals.domain.repository.UserProfileRepository
import javax.inject.Inject

class LoadDarkModeUseCase @Inject constructor(private val userProfileRepository: UserProfileRepository) {
    suspend operator fun invoke(): Boolean? {
        return userProfileRepository.getEnableDarkMode()
    }
}
