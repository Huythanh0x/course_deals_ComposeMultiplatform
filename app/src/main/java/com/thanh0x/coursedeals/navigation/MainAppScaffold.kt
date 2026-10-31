package com.thanh0x.coursedeals.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thanh0x.coursedeals.core.ui.DialogType
import com.thanh0x.coursedeals.domain.coupons.Coupon

@Composable
fun MainAppScaffold(
    onCouponClick: (Coupon) -> Unit,
    onNavigateToLogin: () -> Unit,
    showAlertDialog: (String, String, DialogType) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            MainBottomNavBar(
                destinations = TopLevelDestination.entries,
                currentDestinationRoute = currentRoute,
                onNavigateToDestination = { destination ->
                    navigateToTopLevelDestination(navController, destination)
                },
            )
        },
    ) { innerPadding ->
        MainNavHost(
            navController = navController,
            onCouponClick = onCouponClick,
            onNavigateToLogin = onNavigateToLogin,
            showAlertDialog = showAlertDialog,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

private fun navigateToTopLevelDestination(
    navController: NavHostController,
    destination: TopLevelDestination,
) {
    navController.navigate(destination.route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
