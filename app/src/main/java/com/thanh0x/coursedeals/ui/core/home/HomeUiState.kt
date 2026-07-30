package com.thanh0x.coursedeals.ui.core.home

data class HomeUiState(
    val isInternetAvailable: Boolean = true,
    val query: String = "",
    val statDeals: Long = 0,
    val statUpdatedTimestamp: Long? = null,
    val statFetchedTimestamp: Long? = null,
    val showLocalFetchTime: Boolean = false
)
