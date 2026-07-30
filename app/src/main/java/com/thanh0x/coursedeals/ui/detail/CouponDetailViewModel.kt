package com.thanh0x.coursedeals.ui.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thanh0x.coursedeals.domain.model.AppResult
import com.thanh0x.coursedeals.domain.model.Coupon
import com.thanh0x.coursedeals.domain.usecase.remote_coupon.FetchCouponDetailUseCase
import com.thanh0x.coursedeals.util.NetworkUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CouponDetailViewModel @Inject constructor(
    private val fetchCouponDetailUseCase: FetchCouponDetailUseCase,
    private val networkUtil: NetworkUtil
) :
    ViewModel() {
    val isInternetAvailable = MutableLiveData<Boolean>()
    val couponDetail = MutableLiveData<AppResult<Coupon>>()
    
    fun fetchCouponDetail(courseId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            couponDetail.postValue(AppResult.Loading)
            val result = fetchCouponDetailUseCase(courseId)
            couponDetail.postValue(result)
        }
    }

    fun checkIfInternetAvailable() {
        isInternetAvailable.postValue(networkUtil.isInternetAvailable())
    }
}
