package com.thanh0x.coursedeals.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thanh0x.coursedeals.domain.model.AppResult
import com.thanh0x.coursedeals.domain.usecase.remote_coupon.FetchCouponDetailUseCase
import com.thanh0x.coursedeals.util.NetworkUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CouponDetailViewModel @Inject constructor(
    private val fetchCouponDetailUseCase: FetchCouponDetailUseCase,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val _uiState = MutableStateFlow(CouponDetailUiState())
    val uiState = _uiState.asStateFlow()
    
    fun fetchCouponDetail(courseId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = fetchCouponDetailUseCase(courseId)
            _uiState.update { it.copy(isLoading = false) }
            
            when (result) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(coupon = result.data) }
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
