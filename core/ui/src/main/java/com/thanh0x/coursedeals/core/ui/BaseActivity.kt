package com.thanh0x.coursedeals.core.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.thanh0x.coursedeals.core.ui.customview.LoadingDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds
import timber.log.Timber

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
            is UiEvent.ShowAlertDialog -> showAlertDialog(
                title = event.title,
                message = event.message,
                type = event.type,
            )
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

    fun hideLoading(immediate: Boolean = false) {
        if (isFinishing || isDestroyed) return
        if (immediate) {
            loadingDialog?.dismissAllowingStateLoss()
            loadingDialog = null
            return
        }
        lifecycleScope.launch {
            val elapsedTime = System.currentTimeMillis() - loadingStartTime
            val remainingTime = max(500L - elapsedTime, 0L)
            if (remainingTime > 0) {
                delay(remainingTime.milliseconds)
            }
            loadingDialog?.dismissAllowingStateLoss()
            loadingDialog = null
        }
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun showAlertDialog(
        title: String,
        message: String,
        type: DialogType = DialogType.INFO,
        cause: String? = null,
        positiveButtonText: String? = null,
        negativeButtonText: String? = null,
        onNegativeClick: (() -> Unit)? = null,
        onDismiss: (() -> Unit)? = null,
    ) {
        if (isFinishing || isDestroyed) return
        val finalMessage = if (cause != null) "$message\n\nCause: $cause" else message
        val headerView = LayoutInflater.from(this).inflate(R.layout.layout_dialog_header, null)
        val tvTitle = headerView.findViewById<TextView>(R.id.tvDialogTitle)
        val ivLogo = headerView.findViewById<ImageView>(R.id.ivDialogLogo)

        tvTitle.text = title
        ivLogo.setImageResource(type.iconRes)
        val iconColor = ContextCompat.getColor(this, type.iconTintRes)
        ivLogo.imageTintList = ColorStateList.valueOf(iconColor)

        if (type == DialogType.ERROR) {
            Timber.tag(this.javaClass.simpleName)
                .e("showAlertDialog: title=$title, message=$message, cause=$cause")
        }

        val positiveText = positiveButtonText ?: getString(R.string.ok_text_button)
        val builder = MaterialAlertDialogBuilder(
            this,
            R.style.ThemeOverlay_CourseDeal_MaterialAlertDialog_Centered,
        ).setCustomTitle(headerView)
            .setMessage(finalMessage)
            .setPositiveButton(positiveText) { d, _ ->
                d.dismiss()
                onDismiss?.invoke()
            }

        if (negativeButtonText != null) {
            builder.setNegativeButton(negativeButtonText) { d, _ ->
                d.dismiss()
                onNegativeClick?.invoke()
            }
        }

        val dialog = builder.show()

        val positiveColor = ContextCompat.getColor(this, type.positiveButtonColorRes)
        dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)?.setTextColor(positiveColor)
    }
}
