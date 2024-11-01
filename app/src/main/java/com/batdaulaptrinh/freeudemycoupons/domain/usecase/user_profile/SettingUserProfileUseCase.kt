package com.batdaulaptrinh.freeudemycoupons.domain.usecase.user_profile

import javax.inject.Inject

class SettingUserProfileUseCase @Inject constructor(
    val saveDarkModeUseCase: SaveDarkModeUseCase,
    val saveFingerPrintUseCase: SaveFingerprintUseCase,
    val loadDarkModeUseCase: LoadDarkModeUseCase,
    val loadFingerprintUseCase: LoadFingerprintUseCase
)
