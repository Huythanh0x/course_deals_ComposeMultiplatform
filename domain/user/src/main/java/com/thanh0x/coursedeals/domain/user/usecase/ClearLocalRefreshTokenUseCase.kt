package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository

class ClearLocalRefreshTokenUseCase constructor(
    private val repository: UserAuthenticationRepository
) {
    suspend operator fun invoke() {
        repository.clearLocalRefreshToken()
    }
}
