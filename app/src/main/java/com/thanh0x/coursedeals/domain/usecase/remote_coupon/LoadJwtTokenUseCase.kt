package com.thanh0x.coursedeals.domain.usecase.remote_coupon

import com.thanh0x.coursedeals.data.repository.UserAuthenticationRepositoryImpl
import javax.inject.Inject

class LoadJwtTokenUseCase @Inject constructor(private val userAuthenticationRepositoryImpl: UserAuthenticationRepositoryImpl) {
    suspend operator fun invoke(): String? {
        return userAuthenticationRepositoryImpl.getLocalToken()
    }
}
