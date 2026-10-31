package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository

class RegisterUseCase constructor(
    private val repository: UserAuthenticationRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        fullName: String? = null
    ): AppResult<String> {
        return repository.register(email = email, password = password, fullName = fullName)
    }
}
