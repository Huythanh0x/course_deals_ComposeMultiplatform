package com.batdaulaptrinh.freeudemycoupons.domain.logic.fingerprint


data class CiphertextWrapper(
    val ciphertext: ByteArray,
    val initializationVector: ByteArray
)