package com.thanh0x.coursedeals

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import com.thanh0x.coursedeals.core.common.BundleKey
import com.thanh0x.coursedeals.core.logging.CommonTracingInitializer
import com.thanh0x.coursedeals.core.logging.StartupTimeMonitor
import com.thanh0x.coursedeals.core.ui.BaseActivity
import com.thanh0x.coursedeals.core.ui.theme.CourseDealsTheme
import com.thanh0x.coursedeals.navigation.MainAppScaffold

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)
        setContent {
            LaunchedEffect(Unit) {
                CommonTracingInitializer.setCurrentRoute("main")
                StartupTimeMonitor.onFirstFrameRendered()
            }
            CourseDealsTheme {
                MainAppScaffold(
                    onCouponClick = { coupon -> navigateToDetail(coupon.courseId) },
                    onNavigateToLogin = { navigateToLogin() },
                    showAlertDialog = { title, message, type ->
                        showAlertDialog(title = title, message = message, type = type)
                    },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data
        if (data != null && data.scheme == "coursedeals" && data.host == "courses") {
            val courseId = data.lastPathSegment?.toIntOrNull()
            if (courseId != null) {
                navigateToDetail(courseId)
                return
            }
        }
        val extraCourseId = intent?.getIntExtra(BundleKey.TO_DETAIL_ACTIVITY, -1)?.takeIf { it > 0 }
        if (extraCourseId != null) {
            navigateToDetail(extraCourseId)
        }
    }

    private fun navigateToDetail(courseId: Int) {
        val detailIntent = Intent().apply {
            setClassName(packageName, "com.thanh0x.coursedeals.feature.detail.CouponDetailActivity")
            putExtra(BundleKey.TO_DETAIL_ACTIVITY, courseId)
        }
        startActivity(detailIntent)
    }

    private fun navigateToLogin() {
        val intent = Intent().apply {
            setClassName(packageName, "com.thanh0x.coursedeals.feature.auth.LoginActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
