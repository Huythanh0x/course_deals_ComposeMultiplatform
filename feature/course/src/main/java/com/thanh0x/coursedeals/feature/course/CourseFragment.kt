package com.thanh0x.coursedeals.feature.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import com.thanh0x.coursedeals.core.ui.AppDialogFragment
import com.thanh0x.coursedeals.core.ui.BaseFragment
import com.thanh0x.coursedeals.core.ui.DialogType
import com.thanh0x.coursedeals.core.ui.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CourseFragment : BaseFragment() {
    private val courseViewModel: CourseViewModel by viewModels()
    private var activeFragmentDialog: AppDialogFragment? = null
    private var webView: WebView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CourseRoute(
                    viewModel = courseViewModel,
                    onPageStarted = { hideLoading() },
                    onPageFinished = { hideLoading() },
                    onPageError = { hideLoading() },
                    onWebViewCreated = { wv -> webView = wv },
                )
            }
        }
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
        if (!state.isInternetAvailable) {
            hideLoading(immediate = true)
            showNotInternetDialog()
        }
    }

    override fun onDestroyView() {
        hideLoading(immediate = true)
        webView?.apply {
            stopLoading()
            webViewClient = android.webkit.WebViewClient()
        }
        webView = null
        activeFragmentDialog?.dismissAllowingStateLoss()
        activeFragmentDialog = null
        super.onDestroyView()
    }

    private fun showNotInternetDialog() {
        if (activeFragmentDialog?.dialog?.isShowing == true) return
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
