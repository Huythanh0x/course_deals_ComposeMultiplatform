package com.thanh0x.coursedeals.feature.detail

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.core.ui.ViewModel
import com.thanh0x.coursedeals.domain.coupons.Coupon
import com.thanh0x.coursedeals.domain.coupons.usecase.FetchCouponDetailUseCase
import com.thanh0x.coursedeals.core.ui.util.NetworkUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CouponDetailViewModel(
    private val fetchCouponDetailUseCase: FetchCouponDetailUseCase,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val _uiState = MutableStateFlow(CouponDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun fetchCouponDetail(courseId: Int, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = fetchCouponDetailUseCase(courseId)
            _uiState.update { it.copy(isLoading = false) }

            when (result) {
                is AppResult.Success -> {
                    val coupon = result.data
                    val cachedAt = coupon.cachedAt
                    _uiState.update {
                        it.copy(
                            coupon = coupon,
                            isFromCache = cachedAt != null,
                            cachedAt = cachedAt,
                            error = null,
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                is AppResult.Loading -> { }
            }
        }
    }

    fun checkIfInternetAvailable() {
        val available = networkUtil.isInternetAvailable()
        _uiState.update { it.copy(isInternetAvailable = available) }
    }
}
