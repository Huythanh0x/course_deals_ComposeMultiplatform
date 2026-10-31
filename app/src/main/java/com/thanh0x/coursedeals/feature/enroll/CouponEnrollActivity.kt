package com.thanh0x.coursedeals.feature.enroll

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.thanh0x.coursedeals.R as CoreR
import com.thanh0x.coursedeals.core.common.BundleKey
import com.thanh0x.coursedeals.core.ui.BaseActivity
import com.thanh0x.coursedeals.core.ui.DialogType
import com.thanh0x.coursedeals.core.ui.theme.CourseDealsTheme

class CouponEnrollActivity : BaseActivity() {

    private var webView: WebView? = null
    private var isLoading by mutableStateOf(false)
    private var isError by mutableStateOf(false)
    private var errorMessage by mutableStateOf<String?>(null)
    private var couponUrl: String? = null

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (webView?.canGoBack() == true) {
                webView?.goBack()
            } else {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        couponUrl = intent.extras?.getString(BundleKey.TO_ENROLL_ACTIVITY)
            ?: intent.dataString

        if (couponUrl.isNullOrBlank()) {
            isError = true
            errorMessage = getString(CoreR.string.error_fetching_null_coupon)
            showAlertDialog(
                title = getString(CoreR.string.coupon_detail_error_title),
                message = getString(CoreR.string.error_fetching_null_coupon),
                type = DialogType.ERROR,
            ) {
                finish()
            }
        }

        setContent {
            CourseDealsTheme {
                CouponEnrollRoute(
                    couponUrl = couponUrl,
                    isLoading = isLoading,
                    isError = isError,
                    errorMessage = errorMessage,
                    onBackClick = { finish() },
                    onRetryClick = { retryLoading() },
                    onPageStarted = {
                        isLoading = true
                        isError = false
                    },
                    onPageFinished = {
                        isLoading = false
                    },
                    onPageError = {
                        isLoading = false
                        isError = true
                    },
                    onWebViewCreated = { wv ->
                        webView = wv as? WebView
                    },
                )
            }
        }
    }

    private fun retryLoading() {
        val currentUrl = couponUrl
        if (!currentUrl.isNullOrBlank()) {
            isError = false
            isLoading = true
            webView?.loadUrl(currentUrl)
        }
    }

    override fun onDestroy() {
        webView?.apply {
            stopLoading()
            webViewClient = android.webkit.WebViewClient()
        }
        webView = null
        super.onDestroy()
    }
}
