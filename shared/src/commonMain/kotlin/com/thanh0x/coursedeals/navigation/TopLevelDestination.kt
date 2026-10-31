package com.thanh0x.coursedeals.navigation

import com.thanh0x.coursedeals.core.ui.resources.DrawableResource
import com.thanh0x.coursedeals.core.ui.resources.Res
import com.thanh0x.coursedeals.core.ui.resources.StringResource

enum class TopLevelDestination(
    val route: String,
    val iconRes: DrawableResource,
    val selectedIconRes: DrawableResource,
    val titleRes: StringResource,
) {
    HOME(
        route = "home",
        iconRes = Res.drawable.ic_nav_home_outlined,
        selectedIconRes = Res.drawable.ic_nav_home_filled,
        titleRes = Res.string.string_title_home_menu,
    ),
    COURSE(
        route = "course",
        iconRes = Res.drawable.ic_nav_course_outlined,
        selectedIconRes = Res.drawable.ic_nav_course_filled,
        titleRes = Res.string.title_course_menu,
    ),
    PROFILE(
        route = "profile",
        iconRes = Res.drawable.ic_nav_profile_outlined,
        selectedIconRes = Res.drawable.ic_nav_profile_filled,
        titleRes = Res.string.title_profile_menu,
    ),
}
