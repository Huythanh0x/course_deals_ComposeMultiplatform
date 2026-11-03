package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.domain.user.CiphertextWrapper

private const val GCM_IV_LENGTH_BYTES = 12

actual class CryptographyManagerUseCase actual constructor() {
    actual fun encryptData(plaintext: String): CiphertextWrapper {
        return CiphertextWrapper(
            ciphertext = plaintext.encodeToByteArray(),
            initializationVector = ByteArray(GCM_IV_LENGTH_BYTES)
        )
    }

    actual fun decryptData(ciphertext: ByteArray, initializationVector: ByteArray): String {
        return ciphertext.decodeToString()
    }
}
