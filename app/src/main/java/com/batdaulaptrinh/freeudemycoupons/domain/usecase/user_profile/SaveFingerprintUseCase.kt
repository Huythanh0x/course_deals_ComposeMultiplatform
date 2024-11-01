package com.batdaulaptrinh.freeudemycoupons.domain.usecase.user_profile

import com.batdaulaptrinh.freeudemycoupons.data.repository.UserProfileRepositoryImpl
import javax.inject.Inject

class SaveFingerprintUseCase @Inject constructor(private val userProfileRepositoryImpl: UserProfileRepositoryImpl) {
    suspend operator fun invoke(isFingerPrintEnable: Boolean) {
        userProfileRepositoryImpl.saveEnableFingerPrint(isFingerPrintEnable)
    }
}
