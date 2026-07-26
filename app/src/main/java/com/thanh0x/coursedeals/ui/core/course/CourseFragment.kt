package com.thanh0x.coursedeals.ui.core.course

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.databinding.FragmentCourseBinding
import com.thanh0x.coursedeals.ui.base.BaseFragment
import com.thanh0x.coursedeals.util.Constant
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CourseFragment : BaseFragment() {
    private var _binding: FragmentCourseBinding? = null
    val binding get() = _binding!!
    private val courseViewModel: CourseViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseBinding.inflate(layoutInflater, container, false)
        showLoading(getString(R.string.loading_your_courses_page_text))
        courseViewModel.isInternetAvailable.observe(viewLifecycleOwner) {
            if (it) {
                configureWebViewSettings()
                binding.wvMyCourses.webViewClient = getCustomWebViewClient()
                binding.wvMyCourses.loadUrl(Constant.MY_COURSE_URL)
            } else {
                showNotInternetDialog()
            }
        }
        courseViewModel.checkIfInternetAvailable()
        return binding.root
    }

    override fun onDestroyView() {
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
                if (_binding != null) {
                    hideLoading()
                    binding.wvMyCourses.visibility = View.VISIBLE
                }
                super.onPageStarted(view, url, favicon)
            }
        }
    }

    private fun showNotInternetDialog() {
        showAlertDialog(
            getString(R.string.no_internet_title),
            getString(R.string.no_internet_message)
        ) {
            courseViewModel.checkIfInternetAvailable()
        }
    }
}
