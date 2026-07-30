package com.thanh0x.coursedeals.ui.base

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
                flow.collect { action(it) }
            }
        }
    }

    fun <T> collectLatestFlow(flow: Flow<T>, action: suspend (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collectLatest { action(it) }
            }
        }
    }

    fun showLoading(message: String? = null) {
        (activity as? BaseActivity)?.showLoading(message)
    }

    fun hideLoading() {
        (activity as? BaseActivity)?.hideLoading()
    }

    fun showAlertDialog(title: String, message: String, cause: String? = null, onPositiveClick: (() -> Unit)? = null) {
        (activity as? BaseActivity)?.showAlertDialog(title, message, cause, onPositiveClick)
    }

    open fun handleUiEvent(event: UiEvent) {
        (activity as? BaseActivity)?.handleUiEvent(event)
    }
}
