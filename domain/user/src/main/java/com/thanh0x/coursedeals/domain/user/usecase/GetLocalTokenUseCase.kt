package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository
import javax.inject.Inject

class GetLocalTokenUseCase @Inject constructor(
    private val userAuthenticationRepository: UserAuthenticationRepository
) {
    suspend operator fun invoke(): String? = userAuthenticationRepository.getLocalToken()
}
