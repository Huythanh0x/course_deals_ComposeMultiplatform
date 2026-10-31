package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.domain.user.CiphertextWrapper
import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject

class LoadCipherTextWrapperUseCase @Inject constructor(
    private val repository: UserAuthenticationRepository
) {
    suspend operator fun invoke(): CiphertextWrapper? {
        val json = repository.getCipherTextWrapper() ?: return null
        return try {
            Json.decodeFromString<CiphertextWrapper>(json)
        } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
            null
        }
    }
}
