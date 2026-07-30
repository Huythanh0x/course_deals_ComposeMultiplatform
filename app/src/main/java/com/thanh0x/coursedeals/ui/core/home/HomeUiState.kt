package com.thanh0x.coursedeals.ui.core.home

import com.thanh0x.coursedeals.domain.model.FilterData

data class HomeUiState(
    val isInternetAvailable: Boolean = true,
    val isSyncing: Boolean = false,
    val query: String = "",
    val filter: FilterData = FilterData(),
    val statDeals: Long = 0,
    val statUpdatedTimestamp: Long? = null,
    val statFetchedTimestamp: Long? = null,
    val showLocalFetchTime: Boolean = false
)
