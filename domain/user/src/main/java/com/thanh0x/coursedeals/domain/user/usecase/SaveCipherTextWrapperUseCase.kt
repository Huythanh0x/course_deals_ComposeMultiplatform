package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.domain.user.CiphertextWrapper
import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SaveCipherTextWrapperUseCase @Inject constructor(
    private val repository: UserAuthenticationRepository
) {
    suspend operator fun invoke(ciphertextWrapper: CiphertextWrapper) {
        val json = Json.encodeToString(ciphertextWrapper)
        repository.saveCipherTextWrapper(json)
    }
}
