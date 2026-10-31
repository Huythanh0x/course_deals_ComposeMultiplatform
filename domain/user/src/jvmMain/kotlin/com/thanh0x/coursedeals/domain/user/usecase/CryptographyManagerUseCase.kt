package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.core.common.Constant
import com.thanh0x.coursedeals.domain.user.CiphertextWrapper
import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val KEY_SIZE = 256
private const val ENCRYPTION_ALGORITHM = "AES"
private const val ENCRYPTION_TRANSFORMATION = "AES/GCM/NoPadding"

actual class CryptographyManagerUseCase actual constructor() {
    private val secretKey: SecretKey by lazy {
        val keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM)
        keyGenerator.init(KEY_SIZE)
        keyGenerator.generateKey()
    }

    fun getInitializedCipherForEncryption(): Cipher {
        val cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    fun getInitializedCipherForDecryption(initializationVector: ByteArray): Cipher {
        val cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKey,
            GCMParameterSpec(Constant.GCM_TAG_LENGTH, initializationVector)
        )
        return cipher
    }

    fun encryptData(plaintext: String, cipher: Cipher): CiphertextWrapper {
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charset.forName("UTF-8")))
        return CiphertextWrapper(ciphertext, cipher.iv)
    }

    fun decryptData(ciphertext: ByteArray, cipher: Cipher): String {
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charset.forName("UTF-8"))
    }

    actual fun encryptData(plaintext: String): CiphertextWrapper {
        val cipher = getInitializedCipherForEncryption()
        return encryptData(plaintext, cipher)
    }

    actual fun decryptData(ciphertext: ByteArray, initializationVector: ByteArray): String {
        val cipher = getInitializedCipherForDecryption(initializationVector)
        return decryptData(ciphertext, cipher)
    }
}
