package com.thanh0x.coursedeals.domain.user

import kotlinx.serialization.Serializable

@Serializable
data class CiphertextWrapper(
    val ciphertext: ByteArray,
    val initializationVector: ByteArray
)
