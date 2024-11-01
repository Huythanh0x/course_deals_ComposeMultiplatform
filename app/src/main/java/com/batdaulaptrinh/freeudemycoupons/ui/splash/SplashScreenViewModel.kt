package com.batdaulaptrinh.freeudemycoupons.ui.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batdaulaptrinh.freeudemycoupons.domain.usecase.authentication.jwt.CheckIfTokenExpiredUseCase
import com.batdaulaptrinh.freeudemycoupons.domain.usecase.remote_coupon.LoadJwtTokenUseCase
import com.batdaulaptrinh.freeudemycoupons.util.NetworkUtil
import com.batdaulaptrinh.freeudemycoupons.util.SplashScreenFlag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    val loadJwtTokenUseCase: LoadJwtTokenUseCase,
    val checkIfTokenExpiredUseCase: CheckIfTokenExpiredUseCase,
    val networkUtil: NetworkUtil
) :
    ViewModel() {
    val splashScreenFlag = MutableLiveData<SplashScreenFlag>()
    val isInternetAvailable = MutableLiveData<Boolean>()

    fun checkIfInternetAvailable() {
        isInternetAvailable.postValue(networkUtil.isInternetAvailable())
    }

    fun checkJwtToken() {
        viewModelScope.launch(Dispatchers.IO) {
            val token = loadJwtTokenUseCase()
            if (token.isNullOrEmpty()) {
                splashScreenFlag.postValue(SplashScreenFlag.EMPTY_TOKEN)
            } else if (!checkIfTokenExpiredUseCase().isSuccessful) {
                splashScreenFlag.postValue(SplashScreenFlag.EXPIRED_TOKEN)
            } else {
                splashScreenFlag.postValue(SplashScreenFlag.VALID_TOKEN)
            }
        }
    }
}