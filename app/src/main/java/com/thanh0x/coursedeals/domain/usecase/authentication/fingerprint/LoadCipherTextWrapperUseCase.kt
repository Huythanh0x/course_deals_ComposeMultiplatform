package com.thanh0x.coursedeals.domain.usecase.authentication.fingerprint

import com.thanh0x.coursedeals.data.repository.UserAuthenticationRepositoryImpl
import com.thanh0x.coursedeals.domain.logic.fingerprint.CiphertextWrapper
import com.google.gson.Gson
import javax.inject.Inject

class LoadCipherTextWrapperUseCase @Inject constructor(
    private val userAuthenticationRepositoryImpl: UserAuthenticationRepositoryImpl
) {
    suspend operator fun invoke(): CiphertextWrapper? {
        val json = userAuthenticationRepositoryImpl.getCipherTextWrapper()
        return Gson().fromJson(json, CiphertextWrapper::class.java)
    }
}