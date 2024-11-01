package com.batdaulaptrinh.freeudemycoupons.ui.core.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.batdaulaptrinh.freeudemycoupons.data.model.Coupon
import com.batdaulaptrinh.freeudemycoupons.data.repository.CouponRepositoryImpl
import com.batdaulaptrinh.freeudemycoupons.util.Constant
import com.batdaulaptrinh.freeudemycoupons.util.NetworkUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val couponRepositoryImpl: CouponRepositoryImpl,
    private val networkUtil: NetworkUtil
) : ViewModel() {
    val items: Flow<PagingData<Coupon>> = Pager(
        config = PagingConfig(pageSize = Constant.ITEMS_PER_PAGE, enablePlaceholders = false),
        pagingSourceFactory = { couponRepositoryImpl.getRemotePagingCouponSource() }
    ).flow.cachedIn(viewModelScope)

    val isInternetAvailable = MutableLiveData<Boolean>()

    fun checkIfInternetAvailable() {
        isInternetAvailable.postValue(networkUtil.isInternetAvailable())
    }
}
