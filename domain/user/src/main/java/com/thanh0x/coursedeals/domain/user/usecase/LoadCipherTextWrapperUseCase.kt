package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.domain.user.CiphertextWrapper
import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository
import com.google.gson.Gson
import javax.inject.Inject

class LoadCipherTextWrapperUseCase @Inject constructor(
    private val repository: UserAuthenticationRepository
) {
    suspend operator fun invoke(): CiphertextWrapper? {
        val json = repository.getCipherTextWrapper()
        return Gson().fromJson(json, CiphertextWrapper::class.java)
    }
}
