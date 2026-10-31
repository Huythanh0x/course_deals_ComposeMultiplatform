package com.thanh0x.coursedeals.domain.user.usecase


class SettingFingerprintUseCase constructor(
    val loadCipherTextWrapperUseCase: LoadCipherTextWrapperUseCase,
    val saveCipherTextWrapperUseCase: SaveCipherTextWrapperUseCase
)
