package com.thanh0x.coursedeals.core.ui

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

enum class DialogType(
    @DrawableRes val iconRes: Int,
    @ColorRes val iconTintRes: Int,
    @ColorRes val positiveButtonColorRes: Int,
) {
    INFO(
        iconRes = R.drawable.ic_logo_mark,
        iconTintRes = R.color.m3_primary,
        positiveButtonColorRes = R.color.m3_primary,
    ),
    WARNING(
        iconRes = R.drawable.ic_flag_filled,
        iconTintRes = R.color.warning_yellow,
        positiveButtonColorRes = R.color.warning_yellow_dark,
    ),
    ERROR(
        iconRes = R.drawable.ic_close,
        iconTintRes = R.color.m3_error,
        positiveButtonColorRes = R.color.m3_error,
    ),
    CONFIRMATION(
        iconRes = R.drawable.ic_check,
        iconTintRes = R.color.m3_primary,
        positiveButtonColorRes = R.color.m3_primary,
    ),
}
