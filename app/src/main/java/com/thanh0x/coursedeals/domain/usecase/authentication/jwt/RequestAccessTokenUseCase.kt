package com.thanh0x.coursedeals.domain.usecase.authentication.jwt

import com.thanh0x.coursedeals.domain.model.AppResult
import com.thanh0x.coursedeals.domain.model.TokenData
import com.thanh0x.coursedeals.domain.repository.UserAuthenticationRepository
import javax.inject.Inject

class RequestAccessTokenUseCase @Inject constructor(
    private val userAuthenticationRepository: UserAuthenticationRepository
) {
    suspend operator fun invoke(): AppResult<TokenData> {
        return userAuthenticationRepository.requestAccessToken()
    }
}
