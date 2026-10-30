package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository
import javax.inject.Inject

class SaveRefreshTokenUseCase @Inject constructor(
    private val repository: UserAuthenticationRepository
) {
    suspend operator fun invoke(refreshToken: String) {
        repository.saveLocalRefreshToken(refreshToken)
    }
}
