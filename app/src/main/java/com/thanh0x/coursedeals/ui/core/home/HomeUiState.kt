package com.thanh0x.coursedeals.ui.core.home

data class HomeUiState(
    val isInternetAvailable: Boolean = true,
    val query: String = "",
    val statDeals: Int = 0,
    val statUpdatedTime: String = "0h"
)
