package com.thanh0x.coursedeals.ui.core.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thanh0x.coursedeals.domain.model.Coupon
import com.thanh0x.coursedeals.domain.repository.CouponRepository
import com.thanh0x.coursedeals.ui.base.UiEvent
import com.thanh0x.coursedeals.util.NetworkUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val couponRepository: CouponRepository,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val items: Flow<PagingData<Coupon>> = uiState
        .map { it.query }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            couponRepository.getCouponsPager(query)
        }
        .cachedIn(viewModelScope)

    init {
        observeMetadata()
        observeSettings()
        syncAllCoupons()
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
                        statFetchedTimestamp = metadata.localFetchTime
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

    fun checkIfInternetAvailable() {
        val available = networkUtil.isInternetAvailable()
        _uiState.update { it.copy(isInternetAvailable = available) }
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }
}
