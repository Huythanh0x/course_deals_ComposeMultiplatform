package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository
import javax.inject.Inject

class CheckIfTokenExpiredUseCase @Inject constructor(
    private val repository: UserAuthenticationRepository
) {
    suspend operator fun invoke(): AppResult<Unit> {
        return repository.checkIfTokenExpired()
    }
}
