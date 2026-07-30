package com.thanh0x.coursedeals.ui.core.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thanh0x.coursedeals.domain.model.Coupon
import com.thanh0x.coursedeals.domain.repository.CouponRepository
import com.thanh0x.coursedeals.util.Constant
import com.thanh0x.coursedeals.util.NetworkUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val couponRepository: CouponRepository,
    private val networkUtil: NetworkUtil
) : ViewModel() {
    val items: Flow<PagingData<Coupon>> = Pager(
        config = PagingConfig(pageSize = Constant.ITEMS_PER_PAGE, enablePlaceholders = false),
        pagingSourceFactory = { couponRepository.getRemotePagingCouponSource() }
    ).flow.cachedIn(viewModelScope)

    val isInternetAvailable = MutableLiveData<Boolean>()

    fun checkIfInternetAvailable() {
        isInternetAvailable.postValue(networkUtil.isInternetAvailable())
    }
}
