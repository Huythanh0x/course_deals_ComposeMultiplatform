package com.thanh0x.coursedeals.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.thanh0x.coursedeals.core.ui.DialogType
import com.thanh0x.coursedeals.core.ui.koinViewModel
import com.thanh0x.coursedeals.domain.coupons.Coupon
import com.thanh0x.coursedeals.feature.course.CourseRoute
import com.thanh0x.coursedeals.feature.course.CourseViewModel
import com.thanh0x.coursedeals.feature.home.HomeViewModel
import com.thanh0x.coursedeals.feature.home.ui.HomeRoute
import com.thanh0x.coursedeals.feature.profile.ProfileRoute
import com.thanh0x.coursedeals.feature.profile.ProfileViewModel

@Composable
fun MainAppScaffold(
    onCouponClick: (Coupon) -> Unit,
    onNavigateToLogin: () -> Unit,
    showAlertDialog: (String, String, DialogType) -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentRoute by rememberSaveable { mutableStateOf(TopLevelDestination.HOME.route) }
    val homeViewModel: HomeViewModel = koinViewModel(key = "home_vm")
    val courseViewModel: CourseViewModel = koinViewModel(key = "course_vm")
    val profileViewModel: ProfileViewModel = koinViewModel(key = "profile_vm")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            MainBottomNavBar(
                destinations = TopLevelDestination.entries,
                currentDestinationRoute = currentRoute,
                onNavigateToDestination = { destination ->
                    currentRoute = destination.route
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (currentRoute) {
                TopLevelDestination.HOME.route -> {
                    HomeRoute(
                        viewModel = homeViewModel,
                        onCouponClick = onCouponClick,
                        showAlertDialog = showAlertDialog,
                    )
                }
                TopLevelDestination.COURSE.route -> {
                    LaunchedEffect(Unit) {
                        courseViewModel.checkIfInternetAvailable()
                    }
                    CourseRoute(
                        viewModel = courseViewModel,
                    )
                }
                TopLevelDestination.PROFILE.route -> {
                    ProfileRoute(
                        viewModel = profileViewModel,
                        onNavigateToLogin = onNavigateToLogin,
                        showAlertDialog = showAlertDialog,
                    )
                }
            }
        }
    }
}
