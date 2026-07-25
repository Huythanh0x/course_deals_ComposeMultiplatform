package com.thanh0x.coursedeals.domain.usecase.authentication.fingerprint

import javax.inject.Inject

class SettingFingerprintUseCase @Inject constructor(
    val loadCipherTextWrapperUseCase: LoadCipherTextWrapperUseCase,
    val saveCipherTextWrapperUseCase: SaveCipherTextWrapperUseCase
)