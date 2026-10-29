package com.thanh0x.coursedeals.feature.course

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import com.thanh0x.coursedeals.core.ui.R
import com.thanh0x.coursedeals.core.ui.DialogType
import com.thanh0x.coursedeals.core.ui.AppDialogFragment
import com.thanh0x.coursedeals.feature.course.databinding.FragmentCourseBinding
import com.thanh0x.coursedeals.core.ui.BaseFragment
import com.thanh0x.coursedeals.core.common.Constant
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CourseFragment : BaseFragment() {
    private var _binding: FragmentCourseBinding? = null
    val binding get() = _binding!!
    private val courseViewModel: CourseViewModel by viewModels()
    private var activeFragmentDialog: AppDialogFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showLoading()
        setupObservers()
        courseViewModel.checkIfInternetAvailable()
    }

    private fun setupObservers() {
        collectFlow(courseViewModel.uiState) { state ->
            handleUiState(state)
        }
    }

    private fun handleUiState(state: CourseUiState) {
        if (state.isInternetAvailable) {
            configureWebViewSettings()
            binding.wvMyCourses.webViewClient = getCustomWebViewClient()
            binding.wvMyCourses.loadUrl(Constant.MY_COURSE_URL)
        } else {
            hideLoading(immediate = true)
            showNotInternetDialog()
        }
    }

    override fun onDestroyView() {
        hideLoading(immediate = true)
        _binding?.wvMyCourses?.apply {
            stopLoading()
            webViewClient = android.webkit.WebViewClient()
        }
        activeFragmentDialog?.dismissAllowingStateLoss()
        activeFragmentDialog = null
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun configureWebViewSettings() {
        binding.wvMyCourses.settings.apply {
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
                hideLoading()
                _binding?.wvMyCourses?.visibility = View.VISIBLE
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                hideLoading()
                _binding?.wvMyCourses?.visibility = View.VISIBLE
                super.onPageFinished(view, url)
            }

            override fun onReceivedError(
                view: WebView?,
                request: android.webkit.WebResourceRequest?,
                error: android.webkit.WebResourceError?
            ) {
                hideLoading()
                super.onReceivedError(view, request, error)
            }
        }
    }

    private fun showNotInternetDialog() {
        activeFragmentDialog = showFragmentDialog(
            type = DialogType.WARNING,
            title = getString(R.string.course_internet_required_title),
            message = getString(R.string.course_internet_required_message),
            positiveButtonText = getString(R.string.retry_button_text),
            onPositiveClick = {
                courseViewModel.checkIfInternetAvailable()
            },
        )
    }
}
