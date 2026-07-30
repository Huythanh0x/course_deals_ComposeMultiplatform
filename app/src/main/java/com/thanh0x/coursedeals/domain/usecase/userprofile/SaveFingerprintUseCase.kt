package com.thanh0x.coursedeals.domain.usecase.userprofile

import com.thanh0x.coursedeals.domain.repository.UserProfileRepository
import javax.inject.Inject

class SaveFingerprintUseCase @Inject constructor(private val userProfileRepository: UserProfileRepository) {
    suspend operator fun invoke(isFingerPrintEnable: Boolean) {
        userProfileRepository.saveEnableFingerPrint(isFingerPrintEnable)
    }
}
