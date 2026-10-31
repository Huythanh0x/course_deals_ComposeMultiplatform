package com.thanh0x.coursedeals.feature.course

import androidx.lifecycle.ViewModel
import com.thanh0x.coursedeals.core.ui.util.NetworkUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CourseViewModel(private val networkUtil: NetworkUtil) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseUiState())
    val uiState = _uiState.asStateFlow()

    fun checkIfInternetAvailable() {
        val available = networkUtil.isInternetAvailable()
        _uiState.update { it.copy(isInternetAvailable = available) }
    }
}
