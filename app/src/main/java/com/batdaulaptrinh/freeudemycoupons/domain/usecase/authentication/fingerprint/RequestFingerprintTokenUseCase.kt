package com.batdaulaptrinh.freeudemycoupons.domain.usecase.authentication.fingerprint

import com.batdaulaptrinh.freeudemycoupons.data.model.TokenResponseData
import com.batdaulaptrinh.freeudemycoupons.domain.repository.UserAuthenticationRepository
import retrofit2.Response
import javax.inject.Inject

class RequestFingerprintTokenUseCase @Inject constructor(
    private val userAuthenticationRepository: UserAuthenticationRepository
) {
    suspend operator fun invoke(): Response<TokenResponseData> {
        return userAuthenticationRepository.requestFingerprintToken()
    }
}