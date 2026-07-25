package com.thanh0x.coursedeals.domain.usecase.user_profile

import com.thanh0x.coursedeals.data.repository.UserProfileRepositoryImpl
import javax.inject.Inject

class SaveFingerprintUseCase @Inject constructor(private val userProfileRepositoryImpl: UserProfileRepositoryImpl) {
    suspend operator fun invoke(isFingerPrintEnable: Boolean) {
        userProfileRepositoryImpl.saveEnableFingerPrint(isFingerPrintEnable)
    }
}
