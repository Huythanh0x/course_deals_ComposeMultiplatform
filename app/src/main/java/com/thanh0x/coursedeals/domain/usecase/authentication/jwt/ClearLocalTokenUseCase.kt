package com.thanh0x.coursedeals.domain.usecase.authentication.jwt

import com.thanh0x.coursedeals.data.repository.UserAuthenticationRepositoryImpl
import javax.inject.Inject

class ClearLocalTokenUseCase @Inject constructor(
    private val userAuthenticationRepositoryImpl: UserAuthenticationRepositoryImpl
) {
    suspend operator fun invoke() {
        userAuthenticationRepositoryImpl.clearLocalToken()
    }
}