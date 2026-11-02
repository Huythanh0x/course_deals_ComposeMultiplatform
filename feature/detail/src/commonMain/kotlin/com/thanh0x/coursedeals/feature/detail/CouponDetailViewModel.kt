package com.thanh0x.coursedeals.feature.detail

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.core.ui.ViewModel
import com.thanh0x.coursedeals.core.ui.util.NetworkUtil
import com.thanh0x.coursedeals.domain.coupons.usecase.FetchCouponDetailUseCase
import com.thanh0x.coursedeals.domain.coupons.usecase.ReportCourseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CouponDetailViewModel(
    private val fetchCouponDetailUseCase: FetchCouponDetailUseCase,
    private val reportCourseUseCase: ReportCourseUseCase,
    private val networkUtil: NetworkUtil,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CouponDetailUiState())
    val uiState = _uiState.asStateFlow()

    @Suppress("UnusedParameter")
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

    fun reportCourse(courseId: Int, reason: String, details: String? = null) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isReporting = true,
                    reportSuccess = false,
                    reportError = null,
                )
            }
            when (val result = reportCourseUseCase(courseId, reason, details)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isReporting = false,
                            reportSuccess = true,
                            reportError = null,
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isReporting = false,
                            reportSuccess = false,
                            reportError = result.message,
                        )
                    }
                }
                is AppResult.Loading -> { }
            }
        }
    }

    fun clearReportState() {
        _uiState.update {
            it.copy(
                isReporting = false,
                reportSuccess = false,
                reportError = null,
            )
        }
    }
}
