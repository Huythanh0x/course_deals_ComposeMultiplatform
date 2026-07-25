package com.thanh0x.coursedeals.data.model

data class ResponseStatusFromServer(
    val status: Int,
    val message: String,
    val route: String
)