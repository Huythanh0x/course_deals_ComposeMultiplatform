package com.thanh0x.coursedeals.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thanh0x.coursedeals.domain.coupons.Coupon
import com.thanh0x.coursedeals.domain.coupons.CourseLanguage
import com.thanh0x.coursedeals.domain.coupons.FilterData
import com.thanh0x.coursedeals.domain.coupons.SearchSuggestion
import com.thanh0x.coursedeals.domain.coupons.SortOption
import com.thanh0x.coursedeals.domain.coupons.SuggestionType
import com.thanh0x.coursedeals.domain.coupons.CouponRepository
import com.thanh0x.coursedeals.domain.coupons.SearchRepository
import com.thanh0x.coursedeals.core.ui.UiEvent
import com.thanh0x.coursedeals.core.ui.util.NetworkUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val couponRepository: CouponRepository,
    private val searchRepository: SearchRepository,
    private val networkUtil: NetworkUtil,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _queryTrigger = MutableStateFlow("")

    val items: Flow<PagingData<Coupon>> = combine(
        _queryTrigger,
        uiState.map { it.filter }.distinctUntilChanged(),
    ) { query, filter ->
        query to filter
    }.flatMapLatest { (query, filter) ->
        couponRepository.getCouponsPager(query, filter)
    }.cachedIn(viewModelScope)

    init {
        observeMetadata()
        observeSettings()
        observeFilteredCount()
        observeSuggestions()
        observeQueryChanges()
        syncAllCoupons()
    }

    private fun observeQueryChanges() {
        viewModelScope.launch {
            uiState
                .map { it.query }
                .distinctUntilChanged()
                .debounce(FILTER_DEBOUNCE_MS.milliseconds)
                .collectLatest { query ->
                    _queryTrigger.value = query
                }
        }
    }

    private fun observeSuggestions() {
        viewModelScope.launch {
            uiState
                .map { it.query }
                .distinctUntilChanged()
                .debounce(DEBOUNCE_MS.milliseconds)
                .collectLatest { query ->
                    val history: List<SearchSuggestion>
                    val keywords: List<SearchSuggestion>

                    if (query.isBlank()) {
                        history = searchRepository.getRecentSearches().map {
                            SearchSuggestion(it, SuggestionType.HISTORY)
                        }
                        keywords = emptyList()
                    } else {
                        history = searchRepository.getMatchingHistory(query).map {
                            SearchSuggestion(it, SuggestionType.HISTORY)
                        }
                        keywords = if (query.length >= MIN_QUERY_LENGTH) {
                            searchRepository.getSearchSuggestions(query).map {
                                SearchSuggestion(it, SuggestionType.KEYWORD)
                            }
                        } else {
                            emptyList()
                        }
                    }

                    // Combine, prioritize history, remove duplicates by text
                    val combined = (history + keywords).asSequence()
                        .distinctBy { it.text }
                        .take(MAX_SUGGESTIONS)
                        .toList()
                    _uiState.update { it.copy(suggestions = combined) }
                }
        }
    }

    private fun syncAllCoupons(force: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            couponRepository.syncAllCoupons(force)
            _uiState.update { it.copy(isSyncing = false) }
        }
    }

    fun refreshCoupons() {
        syncAllCoupons(force = true)
    }

    private fun observeMetadata() {
        viewModelScope.launch {
            couponRepository.getMetadataFlow().collectLatest { metadata ->
                _uiState.update {
                    it.copy(
                        statDeals = metadata.totalCoupon,
                        statUpdatedTimestamp = metadata.lastFetchTime,
                        statFetchedTimestamp = metadata.localFetchTime,
                    )
                }
            }
        }
    }

    private fun observeFilteredCount() {
        viewModelScope.launch {
            combine(
                _queryTrigger,
                uiState.map { it.filter }.distinctUntilChanged(),
            ) { query, filter ->
                query to filter
            }.flatMapLatest { (query, filter) ->
                couponRepository.getFilteredCountFlow(query, filter)
            }.collectLatest { count ->
                _uiState.update {
                    val isFiltered = _queryTrigger.value.isNotBlank() ||
                        (it.filter.categories.isNotEmpty()) ||
                        (it.filter.language != CourseLanguage.ALL) ||
                        (it.filter.sortBy != SortOption.NEWEST) ||
                        (it.filter.minRating > 0.0)

                    it.copy(
                        matchingDeals = count,
                        isEmptyState = (count == 0) && isFiltered,
                    )
                }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            couponRepository.getShowLocalFetchTime().collectLatest { show ->
                _uiState.update { it.copy(showLocalFetchTime = show) }
            }
        }
    }

    fun toggleTimestampDisplay() {
        viewModelScope.launch {
            couponRepository.saveShowLocalFetchTime(!_uiState.value.showLocalFetchTime)
        }
    }

    fun resetFilters() {
        _queryTrigger.value = ""
        _uiState.update {
            it.copy(
                query = "",
                filter = FilterData(),
            )
        }
    }

    fun checkIfInternetAvailable() {
        val available = networkUtil.isInternetAvailable()
        _uiState.update { it.copy(isInternetAvailable = available) }
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onSearchSubmitted(query: String) {
        _queryTrigger.value = query
        viewModelScope.launch {
            searchRepository.saveSearchQuery(query)
        }
    }

    fun updateFilter(filter: FilterData) {
        _uiState.update { it.copy(filter = filter) }
    }

    companion object {
        private const val DEBOUNCE_MS = 300L
        private const val FILTER_DEBOUNCE_MS = 1000L
        private const val MIN_QUERY_LENGTH = 2
        private const val MAX_SUGGESTIONS = 10
    }
}
