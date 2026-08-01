package com.thanh0x.coursedeals.core.ui

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class BaseFragment : Fragment() {

    fun <T> collectFlow(flow: Flow<T>, action: suspend (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collectLatest(action)
            }
        }
    }

    open fun handleUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.ShowToast -> (requireActivity() as? BaseActivity)?.showToast(event.message)
            is UiEvent.ShowAlertDialog -> (requireActivity() as? BaseActivity)?.showAlertDialog(event.title, event.message)
            is UiEvent.HideKeyboard -> hideKeyboard()
            else -> {}
        }
    }

    fun showLoading(message: String? = null) {
        (requireActivity() as? BaseActivity)?.showLoading(message)
    }

    fun hideLoading() {
        (requireActivity() as? BaseActivity)?.hideLoading()
    }

    fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    fun showAlertDialog(
        title: String,
        message: String,
        cause: String? = null,
        onDismiss: (() -> Unit)? = null
    ) {
        (requireActivity() as? BaseActivity)?.showAlertDialog(title, message, cause, onDismiss)
    }
}
