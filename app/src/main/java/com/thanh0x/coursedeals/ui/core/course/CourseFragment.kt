package com.thanh0x.coursedeals.ui.core.course

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.databinding.FragmentCourseBinding
import com.thanh0x.coursedeals.util.Constant
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CourseFragment : Fragment() {
    private var _binding: FragmentCourseBinding? = null
    val binding get() = _binding!!
    private val courseViewModel: CourseViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseBinding.inflate(layoutInflater, container, false)
        showLoadingDialog()
        hideWebView()
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
                    hideLoadingDialog()
                    showWebView()
                }
                super.onPageStarted(view, url, favicon)
            }
        }
    }

    private fun showNotInternetDialog() {
        MaterialAlertDialogBuilder(requireContext()).setTitle(this.getString(R.string.no_internet_title))
            .setMessage(this.getString(R.string.no_internet_message))
            .setNeutralButton(R.string.ok_text_button) { dialog, _ ->
                dialog.dismiss()
                courseViewModel.checkIfInternetAvailable()
            }.setCancelable(false).create().show()
    }

    fun showLoadingDialog() {
        binding.clEnrollContainer.isVisible = true
    }

    fun hideLoadingDialog() {
        binding.clEnrollContainer.isVisible = false
    }

    fun showWebView() {
        binding.wvMyCourses.isVisible = true
    }

    fun hideWebView() {
        binding.wvMyCourses.isVisible = false
    }
}
