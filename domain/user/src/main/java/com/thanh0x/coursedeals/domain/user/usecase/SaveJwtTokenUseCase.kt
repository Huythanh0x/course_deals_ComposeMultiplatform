package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository

class SaveJwtTokenUseCase constructor(
    private val repository: UserAuthenticationRepository
) {
    suspend operator fun invoke(newToken: String) {
        repository.saveLocalToken(newToken)
    }
}
