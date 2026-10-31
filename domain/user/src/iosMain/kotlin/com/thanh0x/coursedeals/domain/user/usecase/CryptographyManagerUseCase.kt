package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.domain.user.CiphertextWrapper

actual class CryptographyManagerUseCase actual constructor() {
    actual fun encryptData(plaintext: String): CiphertextWrapper {
        return CiphertextWrapper(
            ciphertext = plaintext.encodeToByteArray(),
            initializationVector = ByteArray(12)
        )
    }

    actual fun decryptData(ciphertext: ByteArray, initializationVector: ByteArray): String {
        return ciphertext.decodeToString()
    }
}
