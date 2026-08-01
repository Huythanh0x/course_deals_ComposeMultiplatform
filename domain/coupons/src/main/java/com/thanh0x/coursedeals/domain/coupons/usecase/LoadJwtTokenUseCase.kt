package com.thanh0x.coursedeals.domain.coupons.usecase

import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository
import javax.inject.Inject

class LoadJwtTokenUseCase @Inject constructor(
    private val repository: UserAuthenticationRepository
) {
    suspend operator fun invoke(): String? {
        return repository.getLocalToken()
    }
}
