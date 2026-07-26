package com.thanh0x.coursedeals.ui.enroll

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.databinding.ActivityCouponEnrollBinding
import com.thanh0x.coursedeals.ui.base.BaseActivity
import com.thanh0x.coursedeals.util.BundleKey

class CouponEnrollActivity : BaseActivity() {
    lateinit var binding: ActivityCouponEnrollBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCouponEnrollBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val couponUrl = intent.extras?.getString(BundleKey.TO_ENROLL_ACTIVITY)
        if (couponUrl != null) {
            configureWebViewSettings()
            binding.wvEnroll.webViewClient = getCustomWebViewClient()
            binding.wvEnroll.loadUrl(couponUrl)
        } else {
            showUrlOpeningErrorDialog()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun configureWebViewSettings() {
        binding.wvEnroll.settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            builtInZoomControls = true
            displayZoomControls = false
        }
    }

    private fun getCustomWebViewClient(): WebViewClient {
        return object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                showLoading(getString(R.string.loading_enroll_page_text))
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                hideLoading()
                binding.wvEnroll.visibility = android.view.View.VISIBLE
            }
        }
    }

    private fun showUrlOpeningErrorDialog() {
        showAlertDialog(
            getString(R.string.coupon_detail_error_title),
            getString(R.string.error_fetching_null_coupon)
        ) {
            finish()
        }
    }
}
