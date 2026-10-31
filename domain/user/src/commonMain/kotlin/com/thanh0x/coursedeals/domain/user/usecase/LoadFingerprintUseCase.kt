package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.domain.user.UserProfileRepository

class LoadFingerprintUseCase constructor(private val userProfileRepository: UserProfileRepository) {
    suspend operator fun invoke(): Boolean? {
        return userProfileRepository.getEnableFingerPrint()
    }
}
