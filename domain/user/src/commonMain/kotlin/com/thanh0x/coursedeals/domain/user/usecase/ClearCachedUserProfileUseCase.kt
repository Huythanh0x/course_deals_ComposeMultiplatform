package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.domain.user.UserProfileRepository

class ClearCachedUserProfileUseCase constructor(
    private val userProfileRepository: UserProfileRepository,
) {
    suspend operator fun invoke() {
        userProfileRepository.clearCachedUserProfile()
    }
}
