package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.UserProfileRepository

class AcceptTermsUseCase constructor(
    private val userProfileRepository: UserProfileRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> {
        return userProfileRepository.acceptTerms()
    }
}
