package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository

class SaveRefreshTokenUseCase constructor(
    private val repository: UserAuthenticationRepository
) {
    suspend operator fun invoke(refreshToken: String) {
        repository.saveLocalRefreshToken(refreshToken)
    }
}
