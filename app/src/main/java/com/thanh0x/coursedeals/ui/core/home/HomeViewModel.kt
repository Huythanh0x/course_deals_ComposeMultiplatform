package com.thanh0x.coursedeals.ui.core.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thanh0x.coursedeals.domain.model.Coupon
import com.thanh0x.coursedeals.domain.repository.CouponRepository
import com.thanh0x.coursedeals.ui.base.UiEvent
import com.thanh0x.coursedeals.util.Constant
import com.thanh0x.coursedeals.util.NetworkUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val couponRepository: CouponRepository,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val items: Flow<PagingData<Coupon>> = Pager(
        config = PagingConfig(pageSize = Constant.ITEMS_PER_PAGE, enablePlaceholders = false),
        pagingSourceFactory = { couponRepository.getRemotePagingCouponSource() }
    ).flow.cachedIn(viewModelScope)

    fun checkIfInternetAvailable() {
        val available = networkUtil.isInternetAvailable()
        _uiState.update { it.copy(isInternetAvailable = available) }
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }
}
