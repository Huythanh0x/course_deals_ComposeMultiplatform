package com.thanh0x.coursedeals.ui.core.course

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.thanh0x.coursedeals.util.NetworkUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(private val networkUtil: NetworkUtil) : ViewModel() {
    val isInternetAvailable = MutableLiveData<Boolean>()
    fun checkIfInternetAvailable() {
        isInternetAvailable.postValue(networkUtil.isInternetAvailable())
    }
}