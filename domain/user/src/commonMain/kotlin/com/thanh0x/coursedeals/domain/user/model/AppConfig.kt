package com.thanh0x.coursedeals.domain.user.model

import com.thanh0x.coursedeals.core.common.AppVersion
import com.thanh0x.coursedeals.core.common.Constant

data class AppConfig(
    val minSupportedVersionCode: Int = AppVersion.DEFAULT_VERSION_CODE,
    val latestVersionCode: Int = AppVersion.DEFAULT_LATEST_VERSION_CODE,
    val forceUpdateUrl: String = Constant.DEFAULT_FORCE_UPDATE_URL,
    val featureFlags: Map<String, String> = DEFAULT_FEATURE_FLAGS,
) {
    companion object {
        const val FLAG_CLIENT_VERIFICATION_ENABLED = "clientVerificationEnabled"
        const val FLAG_COMMUNITY_REPORTS_ENABLED = "communityReportsEnabled"
        const val FLAG_PREFER_EXTERNAL_BROWSER_CHECKOUT = "preferExternalBrowserCheckout"
        const val FLAG_ENROLL_FLOW_REPORT_THRESHOLD = "enrollFlowReportThreshold"

        val DEFAULT_FEATURE_FLAGS: Map<String, String> = mapOf(
            FLAG_CLIENT_VERIFICATION_ENABLED to "false",
            FLAG_COMMUNITY_REPORTS_ENABLED to "true",
            FLAG_PREFER_EXTERNAL_BROWSER_CHECKOUT to "false",
            FLAG_ENROLL_FLOW_REPORT_THRESHOLD to "0.3",
        )
    }
}
