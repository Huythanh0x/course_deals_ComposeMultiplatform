package com.batdaulaptrinh.freeudemycoupons.domain.usecase.authentication.fingerprint

import com.batdaulaptrinh.freeudemycoupons.data.repository.UserAuthenticationRepositoryImpl
import com.batdaulaptrinh.freeudemycoupons.domain.logic.fingerprint.CiphertextWrapper
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