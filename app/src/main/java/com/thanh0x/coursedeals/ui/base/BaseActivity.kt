package com.thanh0x.coursedeals.ui.base

import android.content.res.ColorStateList
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.ui.customview.LoadingDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Long.max
import kotlin.time.Duration.Companion.milliseconds

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
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
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

    fun showAlertDialog(
        title: String,
        message: String,
        cause: String? = null,
        onPositiveClick: (() -> Unit)? = null
    ) {
        if (isFinishing || isDestroyed) return
        val headerView = LayoutInflater.from(this).inflate(R.layout.layout_dialog_header, null)
        val tvTitle = headerView.findViewById<TextView>(R.id.tvDialogTitle)
        val ivLogo = headerView.findViewById<ImageView>(R.id.ivDialogLogo)

        tvTitle.text = title

        // Use red tint for error-related titles
        val isError = title.contains("Error", ignoreCase = true) ||
                title.contains("Failed", ignoreCase = true) ||
                title.contains("No Internet", ignoreCase = true)

        if (isError) {
            Timber.e(this.javaClass.name, "showAlertDialog: title = $title, message = $cause")
            val typedValue = TypedValue()
            val attrId = resources.getIdentifier("colorError", "attr", packageName)
            if (attrId != 0) {
                theme.resolveAttribute(attrId, typedValue, true)
                ivLogo.imageTintList = ColorStateList.valueOf(typedValue.data)
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
