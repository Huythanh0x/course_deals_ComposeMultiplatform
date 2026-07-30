package com.thanh0x.coursedeals.domain.usecase.authentication

import com.thanh0x.coursedeals.domain.model.AppResult
import com.thanh0x.coursedeals.domain.model.AuthCredentials
import com.thanh0x.coursedeals.domain.model.TokenData
import com.thanh0x.coursedeals.domain.repository.UserAuthenticationRepository
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
