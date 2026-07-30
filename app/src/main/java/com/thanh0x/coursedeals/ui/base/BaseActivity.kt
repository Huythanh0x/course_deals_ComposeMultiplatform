package com.thanh0x.coursedeals.ui.base

import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.ui.custom_view.LoadingDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Long.max
import kotlin.time.Duration.Companion.milliseconds

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class BaseActivity : AppCompatActivity() {

    private var loadingDialog: LoadingDialog? = null
    private var loadingStartTime: Long = 0L

    fun <T> collectFlow(flow: Flow<T>, action: suspend (T) -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collect { action(it) }
            }
        }
    }

    fun <T> collectLatestFlow(flow: Flow<T>, action: suspend (T) -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collectLatest { action(it) }
            }
        }
    }

    open fun handleUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.ShowToast -> {
                android.widget.Toast.makeText(this, event.message, android.widget.Toast.LENGTH_SHORT).show()
            }
            is UiEvent.ShowErrorDialog -> {
                showAlertDialog(event.title, event.message)
            }
            is UiEvent.Navigate -> {
                // This will be overridden or expanded in specific activities
            }
        }
    }

    fun showLoading(message: String? = null) {
        if (loadingDialog == null) {
            loadingStartTime = System.currentTimeMillis()
            loadingDialog = LoadingDialog.newInstance(message)
            loadingDialog?.show(supportFragmentManager, "loading")
        }
    }

    fun hideLoading() {
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

    fun showAlertDialog(title: String, message: String, onPositiveClick: (() -> Unit)? = null) {
        val headerView = LayoutInflater.from(this).inflate(R.layout.layout_dialog_header, null)
        val tvTitle = headerView.findViewById<TextView>(R.id.tvDialogTitle)
        val ivLogo = headerView.findViewById<ImageView>(R.id.ivDialogLogo)
        
        tvTitle.text = title
        
        // Use red tint for error-related titles
        val isError = title.contains("Error", ignoreCase = true) || 
                     title.contains("Failed", ignoreCase = true) ||
                     title.contains("No Internet", ignoreCase = true)
        
        if (isError) {
            val typedValue = android.util.TypedValue()
            val attrId = resources.getIdentifier("colorError", "attr", packageName)
            if (attrId != 0) {
                theme.resolveAttribute(attrId, typedValue, true)
                ivLogo.imageTintList = android.content.res.ColorStateList.valueOf(typedValue.data)
            }
        }

        MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        ).setCustomTitle(headerView)
            .setMessage(message)
            .setPositiveButton(R.string.ok_text_button) { dialog, _ ->
                dialog.dismiss()
                onPositiveClick?.invoke()
            }
            .show()
    }
}
