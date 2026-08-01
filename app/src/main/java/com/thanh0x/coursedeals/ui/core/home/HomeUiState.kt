package com.thanh0x.coursedeals.ui.core.home

import com.thanh0x.coursedeals.domain.coupons.FilterData
import com.thanh0x.coursedeals.domain.coupons.SearchSuggestion

data class HomeUiState(
    val isInternetAvailable: Boolean = true,
    val isSyncing: Boolean = false,
    val query: String = "",
    val suggestions: List<SearchSuggestion> = emptyList(),
    val filter: FilterData = FilterData(),
    val statDeals: Long = 0,
    val matchingDeals: Int = 0,
    val isEmptyState: Boolean = false,
    val statUpdatedTimestamp: Long? = null,
    val statFetchedTimestamp: Long? = null,
    val showLocalFetchTime: Boolean = false,
)
