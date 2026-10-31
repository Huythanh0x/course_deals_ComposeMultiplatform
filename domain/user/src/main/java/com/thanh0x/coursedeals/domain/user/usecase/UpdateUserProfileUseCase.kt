package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.UserProfile
import com.thanh0x.coursedeals.domain.user.UserProfileRepository

class UpdateUserProfileUseCase constructor(
    private val userProfileRepository: UserProfileRepository,
) {
    suspend operator fun invoke(
        fullName: String? = null,
        avatarUrl: String? = null,
        fcmToken: String? = null,
    ): AppResult<UserProfile> {
        return userProfileRepository.updateUserProfile(
            fullName = fullName,
            avatarUrl = avatarUrl,
            fcmToken = fcmToken,
        )
    }
}
