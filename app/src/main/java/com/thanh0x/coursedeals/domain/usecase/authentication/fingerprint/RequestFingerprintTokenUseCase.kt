package com.thanh0x.coursedeals.domain.usecase.authentication.fingerprint

import com.thanh0x.coursedeals.data.model.TokenResponseData
import com.thanh0x.coursedeals.domain.repository.UserAuthenticationRepository
import retrofit2.Response
import javax.inject.Inject

class RequestFingerprintTokenUseCase @Inject constructor(
    private val userAuthenticationRepository: UserAuthenticationRepository
) {
    suspend operator fun invoke(): Response<TokenResponseData> {
        return userAuthenticationRepository.requestFingerprintToken()
    }
}