package com.thanh0x.coursedeals.domain.coupons.usecase

import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository

class LoadJwtTokenUseCase(
    private val repository: UserAuthenticationRepository
) {
    suspend operator fun invoke(): String? {
        return repository.getLocalToken()
    }
}
