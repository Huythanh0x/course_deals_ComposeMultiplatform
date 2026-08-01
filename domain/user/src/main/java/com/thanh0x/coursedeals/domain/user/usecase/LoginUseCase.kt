package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.AuthCredentials
import com.thanh0x.coursedeals.domain.user.TokenData
import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val userAuthenticationRepository: UserAuthenticationRepository
) {
    suspend operator fun invoke(
        username: String,
        password: String
    ): AppResult<TokenData> {
        return userAuthenticationRepository.login(AuthCredentials(username, password))
    }
}
