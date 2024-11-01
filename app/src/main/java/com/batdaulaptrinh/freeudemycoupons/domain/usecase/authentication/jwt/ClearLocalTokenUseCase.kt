package com.batdaulaptrinh.freeudemycoupons.domain.usecase.authentication.jwt

import com.batdaulaptrinh.freeudemycoupons.data.repository.UserAuthenticationRepositoryImpl
import javax.inject.Inject

class ClearLocalTokenUseCase @Inject constructor(
    private val userAuthenticationRepositoryImpl: UserAuthenticationRepositoryImpl
) {
    suspend operator fun invoke() {
        userAuthenticationRepositoryImpl.clearLocalToken()
    }
}