package com.batdaulaptrinh.freeudemycoupons.domain.usecase.user_profile

import com.batdaulaptrinh.freeudemycoupons.data.repository.UserProfileRepositoryImpl
import javax.inject.Inject

class LoadFingerprintUseCase @Inject constructor(private val userProfileRepositoryImpl: UserProfileRepositoryImpl) {
    suspend operator fun invoke(): Boolean? {
        return userProfileRepositoryImpl.getEnableFingerPrint()
    }
}