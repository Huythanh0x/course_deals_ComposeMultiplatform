package com.thanh0x.coursedeals.feature.course

import com.thanh0x.coursedeals.core.ui.util.NetworkUtil
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CourseViewModelTest {

    private lateinit var fakeNetworkUtil: FakeNetworkUtil
    private lateinit var viewModel: CourseViewModel

    @BeforeTest
    fun setUp() {
        fakeNetworkUtil = FakeNetworkUtil()
        viewModel = CourseViewModel(fakeNetworkUtil)
    }

    @Test
    fun checkIfInternetAvailable_whenAvailable_updatesStateToTrue() {
        fakeNetworkUtil.isAvailable = true

        viewModel.checkIfInternetAvailable()

        assertTrue(viewModel.uiState.value.isInternetAvailable)
    }

    @Test
    fun checkIfInternetAvailable_whenNotAvailable_updatesStateToFalse() {
        fakeNetworkUtil.isAvailable = false

        viewModel.checkIfInternetAvailable()

        assertFalse(viewModel.uiState.value.isInternetAvailable)
    }

    private class FakeNetworkUtil : NetworkUtil(null) {
        var isAvailable: Boolean = true
        override fun isInternetAvailable(): Boolean = isAvailable
    }
}
