package com.thanh0x.coursedeals.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.thanh0x.coursedeals.core.ui.R as CoreR

enum class TopLevelDestination(
    val route: String,
    @DrawableRes val iconRes: Int,
    @DrawableRes val selectedIconRes: Int,
    @StringRes val titleRes: Int,
) {
    HOME(
        route = "home",
        iconRes = CoreR.drawable.ic_nav_home_outlined,
        selectedIconRes = CoreR.drawable.ic_nav_home_filled,
        titleRes = CoreR.string.string_title_home_menu,
    ),
    COURSE(
        route = "course",
        iconRes = CoreR.drawable.ic_nav_course_outlined,
        selectedIconRes = CoreR.drawable.ic_nav_course_filled,
        titleRes = CoreR.string.title_course_menu,
    ),
    PROFILE(
        route = "profile",
        iconRes = CoreR.drawable.ic_nav_profile_outlined,
        selectedIconRes = CoreR.drawable.ic_nav_profile_filled,
        titleRes = CoreR.string.title_profile_menu,
    ),
}
