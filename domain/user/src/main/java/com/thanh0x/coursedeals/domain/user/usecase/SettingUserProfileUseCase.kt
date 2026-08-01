package com.thanh0x.coursedeals.domain.user.usecase

import javax.inject.Inject

class SettingUserProfileUseCase @Inject constructor(
    val saveDarkModeUseCase: SaveDarkModeUseCase,
    val saveFingerPrintUseCase: SaveFingerprintUseCase,
    val loadDarkModeUseCase: LoadDarkModeUseCase,
    val loadFingerprintUseCase: LoadFingerprintUseCase
)
