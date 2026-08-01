package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.TokenData
import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository
import javax.inject.Inject

class RequestFingerprintTokenUseCase @Inject constructor(
    private val userAuthenticationRepository: UserAuthenticationRepository
) {
    suspend operator fun invoke(): AppResult<TokenData> {
        return userAuthenticationRepository.requestFingerprintToken()
    }
}
