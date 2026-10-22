package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.TokenData
import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository
import javax.inject.Inject

class VerifyOtpUseCase @Inject constructor(
    private val repository: UserAuthenticationRepository
) {
    suspend operator fun invoke(
        email: String,
        otp: String
    ): AppResult<TokenData> {
        return repository.verifyOtp(email = email, otp = otp)
    }
}
