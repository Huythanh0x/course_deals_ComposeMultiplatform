package com.batdaulaptrinh.freeudemycoupons.data.model

data class ResponseStatusFromServer(
    val status: Int,
    val message: String,
    val route: String
)