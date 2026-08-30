package com.thanh0x.coursedeals.core.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.thanh0x.coursedeals.core.ui.customview.LoadingDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

abstract class BaseActivity : AppCompatActivity() {

    private var loadingDialog: LoadingDialog? = null
    private var loadingStartTime: Long = 0L

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

    fun showLoading(message: String? = null) {
        if (isFinishing || isDestroyed) return
        if (loadingDialog == null) {
            loadingStartTime = System.currentTimeMillis()
            loadingDialog = LoadingDialog.newInstance(message)
            loadingDialog?.show(supportFragmentManager, "loading")
        }
    }

    fun hideLoading() {
        if (isFinishing || isDestroyed) return
        lifecycleScope.launch {
            val elapsedTime = System.currentTimeMillis() - loadingStartTime
            val remainingTime = max(500L - elapsedTime, 0L)
            if (remainingTime > 0) {
                delay(remainingTime.milliseconds)
            }
            loadingDialog?.dismiss()
            loadingDialog = null
        }
    }

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
