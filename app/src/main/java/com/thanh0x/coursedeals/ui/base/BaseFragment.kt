package com.thanh0x.coursedeals.ui.base

import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {
    fun showLoading(message: String? = null) {
        (activity as? BaseActivity)?.showLoading(message)
    }

    fun hideLoading() {
        (activity as? BaseActivity)?.hideLoading()
    }

    fun showAlertDialog(title: String, message: String, onPositiveClick: (() -> Unit)? = null) {
        (activity as? BaseActivity)?.showAlertDialog(title, message, onPositiveClick)
    }
}
