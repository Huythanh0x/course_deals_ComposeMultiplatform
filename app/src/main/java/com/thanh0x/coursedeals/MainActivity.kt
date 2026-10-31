package com.thanh0x.coursedeals

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import com.thanh0x.coursedeals.core.common.BundleKey
import com.thanh0x.coursedeals.core.ui.BaseActivity
import com.thanh0x.coursedeals.core.ui.theme.CourseDealsTheme
import com.thanh0x.coursedeals.domain.coupons.Coupon
import com.thanh0x.coursedeals.navigation.MainAppScaffold
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CourseDealsTheme {
                MainAppScaffold(
                    onCouponClick = { coupon -> navigateToDetail(coupon) },
                    onNavigateToLogin = { navigateToLogin() },
                    showAlertDialog = { title, message, type ->
                        showAlertDialog(title = title, message = message, type = type)
                    },
                )
            }
        }
    }

    private fun navigateToDetail(coupon: Coupon) {
        val detailIntent = Intent().apply {
            setClassName(packageName, "com.thanh0x.coursedeals.feature.detail.CouponDetailActivity")
            putExtra(BundleKey.TO_DETAIL_ACTIVITY, coupon.courseId)
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
