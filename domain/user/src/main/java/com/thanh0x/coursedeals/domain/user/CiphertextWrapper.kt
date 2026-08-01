package com.thanh0x.coursedeals.domain.user

data class CiphertextWrapper(
    val ciphertext: ByteArray,
    val initializationVector: ByteArray
)
