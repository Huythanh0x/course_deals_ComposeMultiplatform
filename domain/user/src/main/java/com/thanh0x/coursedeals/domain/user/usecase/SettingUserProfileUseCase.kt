package com.thanh0x.coursedeals.domain.user.usecase


class SettingUserProfileUseCase constructor(
    val saveDarkModeUseCase: SaveDarkModeUseCase,
    val saveFingerPrintUseCase: SaveFingerprintUseCase,
    val loadDarkModeUseCase: LoadDarkModeUseCase,
    val loadFingerprintUseCase: LoadFingerprintUseCase
)
