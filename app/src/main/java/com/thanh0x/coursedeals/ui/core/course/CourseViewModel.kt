package com.thanh0x.coursedeals.ui.core.course

import androidx.lifecycle.ViewModel
import com.thanh0x.coursedeals.util.NetworkUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(private val networkUtil: NetworkUtil) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CourseUiState())
    val uiState = _uiState.asStateFlow()
    
    fun checkIfInternetAvailable() {
        val available = networkUtil.isInternetAvailable()
        _uiState.update { it.copy(isInternetAvailable = available) }
    }
}
