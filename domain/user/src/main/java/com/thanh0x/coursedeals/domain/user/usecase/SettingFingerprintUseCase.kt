package com.thanh0x.coursedeals.domain.user.usecase

import javax.inject.Inject

class SettingFingerprintUseCase @Inject constructor(
    val loadCipherTextWrapperUseCase: LoadCipherTextWrapperUseCase,
    val saveCipherTextWrapperUseCase: SaveCipherTextWrapperUseCase
)
