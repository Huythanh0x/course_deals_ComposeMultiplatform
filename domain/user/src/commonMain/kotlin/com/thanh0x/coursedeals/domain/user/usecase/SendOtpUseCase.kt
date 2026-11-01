package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository

class SendOtpUseCase(
    private val userAuthenticationRepository: UserAuthenticationRepository
) {
    suspend operator fun invoke(email: String): AppResult<String> {
        return userAuthenticationRepository.sendOtp(email)
    }
}
