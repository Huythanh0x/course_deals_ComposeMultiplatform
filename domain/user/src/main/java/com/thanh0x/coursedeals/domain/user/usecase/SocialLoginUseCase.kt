package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.TokenData
import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository

class SocialLoginUseCase constructor(
    private val userAuthenticationRepository: UserAuthenticationRepository
) {
    suspend operator fun invoke(provider: String, idToken: String): AppResult<TokenData> =
        userAuthenticationRepository.socialLogin(provider, idToken)
}
