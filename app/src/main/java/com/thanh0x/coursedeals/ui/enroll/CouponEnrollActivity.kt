package com.thanh0x.coursedeals.ui.enroll

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.databinding.ActivityCouponEnrollBinding
import com.thanh0x.coursedeals.ui.custom_view.LoadingDialog
import com.thanh0x.coursedeals.util.BundleKey
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CouponEnrollActivity : AppCompatActivity() {
    lateinit var binding: ActivityCouponEnrollBinding
    private var loadingDialog: LoadingDialog? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_coupon_enroll)
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
                showLoadingDialog()
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                hideLoadingDialog()
                showWebView()
            }
        }
    }

    private fun showUrlOpeningErrorDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(this.getString(R.string.coupon_detail_error_title))
            .setMessage(this.getString(R.string.error_fetching_null_coupon))
            .setNeutralButton(R.string.ok_text_button) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create().show()
    }

    fun showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog.newInstance(getString(R.string.loading_enroll_page_text))
            loadingDialog?.show(supportFragmentManager, "loading")
        }
    }

    fun hideLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    fun showWebView() {
        binding.wvEnroll.isVisible = true
    }

}