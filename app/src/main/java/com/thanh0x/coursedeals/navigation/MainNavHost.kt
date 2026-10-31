package com.thanh0x.coursedeals.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.thanh0x.coursedeals.core.ui.DialogType
import com.thanh0x.coursedeals.domain.coupons.Coupon
import com.thanh0x.coursedeals.feature.course.CourseRoute
import com.thanh0x.coursedeals.feature.course.CourseViewModel
import com.thanh0x.coursedeals.feature.home.HomeViewModel
import com.thanh0x.coursedeals.feature.home.ui.HomeRoute
import com.thanh0x.coursedeals.feature.profile.ProfileRoute
import com.thanh0x.coursedeals.feature.profile.ProfileViewModel

@Composable
fun MainNavHost(
    navController: NavHostController,
    onCouponClick: (Coupon) -> Unit,
    onNavigateToLogin: () -> Unit,
    showAlertDialog: (String, String, DialogType) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.HOME.route,
        modifier = modifier,
    ) {
        composable(TopLevelDestination.HOME.route) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            HomeRoute(
                viewModel = homeViewModel,
                onCouponClick = onCouponClick,
                showAlertDialog = showAlertDialog,
            )
        }
        composable(TopLevelDestination.COURSE.route) {
            val courseViewModel: CourseViewModel = hiltViewModel()
            CourseRoute(
                viewModel = courseViewModel,
            )
        }
        composable(TopLevelDestination.PROFILE.route) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            ProfileRoute(
                viewModel = profileViewModel,
                onNavigateToLogin = onNavigateToLogin,
                showAlertDialog = showAlertDialog,
            )
        }
    }
}
