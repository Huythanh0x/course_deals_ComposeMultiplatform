package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.domain.user.CiphertextWrapper

expect class CryptographyManagerUseCase() {
    fun encryptData(plaintext: String): CiphertextWrapper
    fun decryptData(ciphertext: ByteArray, initializationVector: ByteArray): String
}
