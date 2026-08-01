package com.thanh0x.coursedeals.core.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class BaseActivity : AppCompatActivity() {

    fun <T> collectFlow(flow: Flow<T>, action: suspend (T) -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collectLatest(action)
            }
        }
    }

    open fun handleUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.ShowToast -> showToast(event.message)
            is UiEvent.ShowAlertDialog -> showAlertDialog(event.title, event.message)
            else -> {}
        }
    }

    fun showLoading(message: String? = null) {}
    fun hideLoading() {}

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun showAlertDialog(
        title: String,
        message: String,
        cause: String? = null,
        onDismiss: (() -> Unit)? = null
    ) {
        val finalMessage = if (cause != null) "$message\n\nCause: $cause" else message
        android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(finalMessage)
            .setPositiveButton(android.R.string.ok) { _, _ -> onDismiss?.invoke() }
            .show()
    }
}
