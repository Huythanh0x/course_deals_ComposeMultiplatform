package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository
import javax.inject.Inject

class SaveJwtTokenUseCase @Inject constructor(
    private val repository: UserAuthenticationRepository
) {
    suspend operator fun invoke(newToken: String) {
        repository.saveLocalToken(newToken)
    }
}
