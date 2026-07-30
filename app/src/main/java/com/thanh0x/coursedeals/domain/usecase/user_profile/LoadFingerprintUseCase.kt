package com.thanh0x.coursedeals.domain.usecase.user_profile

import com.thanh0x.coursedeals.data.repository.UserProfileRepositoryImpl
import javax.inject.Inject

class LoadFingerprintUseCase @Inject constructor(private val userProfileRepositoryImpl: UserProfileRepositoryImpl) {
    suspend operator fun invoke(): Boolean? {
        return userProfileRepositoryImpl.getEnableFingerPrint()
    }
}
