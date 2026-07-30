package com.thanh0x.coursedeals.domain.usecase.remotecoupon

import com.thanh0x.coursedeals.domain.repository.UserAuthenticationRepository
import javax.inject.Inject

class LoadJwtTokenUseCase @Inject constructor(private val userAuthenticationRepository: UserAuthenticationRepository) {
    suspend operator fun invoke(): String? {
        return userAuthenticationRepository.getLocalToken()
    }
}
