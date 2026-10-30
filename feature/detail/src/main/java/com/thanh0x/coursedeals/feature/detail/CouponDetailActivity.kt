package com.thanh0x.coursedeals.feature.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.thanh0x.coursedeals.core.common.BundleKey
import com.thanh0x.coursedeals.core.ui.BaseActivity
import com.thanh0x.coursedeals.core.ui.R as CoreR
import com.thanh0x.coursedeals.core.ui.theme.CourseDealsTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CouponDetailActivity : BaseActivity() {
    private val couponDetailViewModel: CouponDetailViewModel by viewModels()

    @Inject
    lateinit var networkUtil: com.thanh0x.coursedeals.core.ui.util.NetworkUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val courseId = intent.extras?.getInt(BundleKey.TO_DETAIL_ACTIVITY)
        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CourseDealsTheme {
                    CouponDetailRoute(
                        viewModel = couponDetailViewModel,
                        onBackClick = { finish() },
                        onEnrollClick = { couponUrl -> navigateToEnroll(couponUrl) },
                        onShareClick = { title, url -> shareLink(this@CouponDetailActivity, title, url) },
                        onReportSubmitted = { _, _ ->
                            showAlertDialog(
                                getString(CoreR.string.action_report_title),
                                getString(CoreR.string.report_submit_success),
                            )
                        },
                    )
                }
            }
        }
        setContentView(composeView)

        if (courseId != null) {
            setupObservers()
            couponDetailViewModel.fetchCouponDetail(courseId)
        } else {
            showFetchingErrorDialog("Not getting courseID")
        }
    }

    private fun setupObservers() {
        collectFlow(couponDetailViewModel.uiState) { state ->
            handleStateErrors(state)
        }
    }

    private fun handleStateErrors(state: CouponDetailUiState) {
        state.error?.let {
            if (state.coupon == null) {
                if (!networkUtil.isInternetAvailable()) {
                    showNoInternetDialog()
                } else {
                    showFetchingErrorDialog(state.error)
                }
            }
        }
    }

    private fun navigateToEnroll(couponUrl: String?) {
        val enrollIntent = Intent().apply {
            setClassName(this@CouponDetailActivity.packageName, "com.thanh0x.coursedeals.feature.enroll.CouponEnrollActivity")
            putExtra(BundleKey.TO_ENROLL_ACTIVITY, couponUrl)
        }
        startActivity(enrollIntent)
    }

    private fun showFetchingErrorDialog(cause: String) {
        Timber.e("showFetchingErrorDialog: $cause")
        showAlertDialog(
            title = getString(CoreR.string.coupon_detail_error_title),
            message = getString(CoreR.string.error_fetching_null_coupon),
            type = com.thanh0x.coursedeals.core.ui.DialogType.ERROR,
            cause = cause,
            onDismiss = { finish() },
        )
    }

    private fun showNoInternetDialog() {
        showAlertDialog(
            title = getString(CoreR.string.no_internet_title),
            message = getString(CoreR.string.no_internet_message),
            type = com.thanh0x.coursedeals.core.ui.DialogType.WARNING,
            positiveButtonText = getString(CoreR.string.retry_button_text),
            onDismiss = {
                couponDetailViewModel.checkIfInternetAvailable()
            },
        )
    }

    private fun shareLink(context: Context, title: String, url: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, url)
        context.startActivity(Intent.createChooser(intent, title))
    }
}
