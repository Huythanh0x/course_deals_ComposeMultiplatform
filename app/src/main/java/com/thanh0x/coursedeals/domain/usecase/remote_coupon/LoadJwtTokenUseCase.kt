package com.batdaulaptrinh.freeudemycoupons.domain.usecase.remote_coupon

import com.batdaulaptrinh.freeudemycoupons.data.repository.UserAuthenticationRepositoryImpl
import javax.inject.Inject

class LoadJwtTokenUseCase @Inject constructor(private val userAuthenticationRepositoryImpl: UserAuthenticationRepositoryImpl) {
    suspend operator fun invoke(): String? {
        return userAuthenticationRepositoryImpl.getLocalToken()
    }
}