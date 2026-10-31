package com.thanh0x.coursedeals.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thanh0x.coursedeals.core.ui.resources.painterResource
import com.thanh0x.coursedeals.core.ui.resources.stringResource

@Composable
fun MainBottomNavBar(
    destinations: List<TopLevelDestination>,
    currentDestinationRoute: String?,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        destinations.forEach { destination ->
            val selected = currentDestinationRoute == destination.route
            val iconRes = if (selected) destination.selectedIconRes else destination.iconRes
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = stringResource(destination.titleRes),
                    )
                },
                label = { Text(text = stringResource(destination.titleRes)) },
            )
        }
    }
}
