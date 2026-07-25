package com.batdaulaptrinh.freeudemycoupons.domain.usecase.authentication.fingerprint

import com.batdaulaptrinh.freeudemycoupons.data.repository.UserAuthenticationRepositoryImpl
import com.batdaulaptrinh.freeudemycoupons.domain.logic.fingerprint.CiphertextWrapper
import com.google.gson.Gson
import javax.inject.Inject

class SaveCipherTextWrapperUseCase @Inject constructor(
    private val userAuthenticationRepositoryImpl: UserAuthenticationRepositoryImpl
) {
    suspend operator fun invoke(ciphertextWrapper: CiphertextWrapper) {
        val json = Gson().toJson(ciphertextWrapper)
        userAuthenticationRepositoryImpl.saveCipherTextWrapper(json)
    }
}