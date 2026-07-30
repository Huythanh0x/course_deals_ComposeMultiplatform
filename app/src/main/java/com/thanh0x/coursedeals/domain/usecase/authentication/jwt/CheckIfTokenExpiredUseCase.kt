package com.thanh0x.coursedeals.domain.usecase.authentication.jwt

import com.thanh0x.coursedeals.domain.model.AppResult
import com.thanh0x.coursedeals.domain.repository.UserAuthenticationRepository
import javax.inject.Inject

class CheckIfTokenExpiredUseCase @Inject constructor(
    private val repository: UserAuthenticationRepository
) {
    suspend operator fun invoke(): AppResult<Unit> {
        return repository.checkIfTokenExpired()
    }
}
