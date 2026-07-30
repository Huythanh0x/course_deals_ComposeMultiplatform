package com.thanh0x.coursedeals.domain.usecase.authentication.jwt

import com.thanh0x.coursedeals.domain.repository.UserAuthenticationRepository
import javax.inject.Inject

class SaveJwtTokenUseCase @Inject constructor(
    private val repository: UserAuthenticationRepository
) {
    suspend operator fun invoke(newToken: String) {
        repository.saveLocalToken(newToken)
    }
}
