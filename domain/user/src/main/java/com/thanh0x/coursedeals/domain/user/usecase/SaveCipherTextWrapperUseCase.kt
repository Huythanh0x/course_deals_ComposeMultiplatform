package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.domain.user.CiphertextWrapper
import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository
import com.google.gson.Gson
import javax.inject.Inject

class SaveCipherTextWrapperUseCase @Inject constructor(
    private val repository: UserAuthenticationRepository
) {
    suspend operator fun invoke(ciphertextWrapper: CiphertextWrapper) {
        val json = Gson().toJson(ciphertextWrapper)
        repository.saveCipherTextWrapper(json)
    }
}
