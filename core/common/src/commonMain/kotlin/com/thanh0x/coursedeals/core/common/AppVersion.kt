package com.thanh0x.coursedeals.core.common

object AppVersion {
    const val DEFAULT_VERSION_CODE = 105
    const val DEFAULT_LATEST_VERSION_CODE = 106

    var overrideVersionCode: Int? = null

    fun currentVersionCode(): Int = overrideVersionCode ?: DEFAULT_VERSION_CODE
}
