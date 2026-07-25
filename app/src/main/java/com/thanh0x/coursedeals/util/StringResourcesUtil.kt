package com.thanh0x.coursedeals.util

import android.content.Context
import androidx.annotation.StringRes

object StringResourcesUtil {
    fun getStringResource(
        context: Context,
        @StringRes resId: Int,
        formatArgs: Any? = null
    ): String {
        return when (formatArgs) {
            null -> context.getString(resId)
            else -> context.getString(resId, formatArgs)
        }
    }
}
