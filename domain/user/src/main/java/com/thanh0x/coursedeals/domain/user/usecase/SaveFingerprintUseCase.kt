package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.domain.user.UserProfileRepository

class SaveFingerprintUseCase constructor(private val userProfileRepository: UserProfileRepository) {
    suspend operator fun invoke(isFingerPrintEnable: Boolean) {
        userProfileRepository.saveEnableFingerPrint(isFingerPrintEnable)
    }
}
