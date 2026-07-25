package com.thanh0x.coursedeals.ui.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thanh0x.coursedeals.data.model.Coupon
import com.thanh0x.coursedeals.domain.usecase.remote_coupon.FetchCouponDetailUseCase
import com.thanh0x.coursedeals.util.NetWorkResult
import com.thanh0x.coursedeals.util.NetworkUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CouponDetailViewModel @Inject constructor(
    val fetchCouponDetailUseCase: FetchCouponDetailUseCase,
    private val networkUtil: NetworkUtil
) :
    ViewModel() {
    val isInternetAvailable = MutableLiveData<Boolean>()
    val couponDetail = MutableLiveData<NetWorkResult<Coupon>>()
    fun fetchCouponDetail(courseId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            couponDetail.postValue(NetWorkResult.Loading())
            fetchCouponDetailUseCase(courseId).let {
                couponDetail.postValue(it)
            }
        }
    }

    fun checkIfInternetAvailable() {
        isInternetAvailable.postValue(networkUtil.isInternetAvailable())
    }
}
