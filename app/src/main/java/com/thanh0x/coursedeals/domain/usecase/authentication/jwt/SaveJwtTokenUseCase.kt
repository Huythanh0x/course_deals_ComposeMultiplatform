package com.batdaulaptrinh.freeudemycoupons.domain.usecase.authentication.jwt

import com.batdaulaptrinh.freeudemycoupons.data.repository.UserAuthenticationRepositoryImpl
import javax.inject.Inject

class SaveJwtTokenUseCase @Inject constructor(private val userAuthenticationRepositoryImpl: UserAuthenticationRepositoryImpl) {
    suspend operator fun invoke(newToken: String) {
        userAuthenticationRepositoryImpl.saveLocalToken(newToken)
    }
}