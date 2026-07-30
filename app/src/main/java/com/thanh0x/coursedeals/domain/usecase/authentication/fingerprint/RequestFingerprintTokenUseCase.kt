package com.thanh0x.coursedeals.domain.usecase.authentication.fingerprint

import com.thanh0x.coursedeals.domain.model.AppResult
import com.thanh0x.coursedeals.domain.model.TokenData
import com.thanh0x.coursedeals.domain.repository.UserAuthenticationRepository
import javax.inject.Inject

class RequestFingerprintTokenUseCase @Inject constructor(
    private val userAuthenticationRepository: UserAuthenticationRepository
) {
    suspend operator fun invoke(): AppResult<TokenData> {
        return userAuthenticationRepository.requestFingerprintToken()
    }
}
