package com.thanh0x.coursedeals.domain.logic.fingerprint


data class CiphertextWrapper(
    val ciphertext: ByteArray,
    val initializationVector: ByteArray
)