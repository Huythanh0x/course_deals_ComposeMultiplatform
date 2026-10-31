package com.thanh0x.coursedeals.core.ui

import android.content.res.ColorStateList
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
import com.thanh0x.coursedeals.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

internal fun DialogType.toIconRes(): Int = R.drawable.ic_logo_mark

internal fun DialogType.toIconTintRes(): Int = when (this) {
    DialogType.INFO -> R.color.m3_primary
    DialogType.WARNING -> R.color.warning_yellow
    DialogType.ERROR -> R.color.m3_error
    DialogType.CONFIRMATION -> R.color.m3_primary
}

internal fun DialogType.toPositiveButtonColorRes(): Int = when (this) {
    DialogType.INFO -> R.color.m3_primary
    DialogType.WARNING -> R.color.warning_yellow_dark
    DialogType.ERROR -> R.color.m3_error
    DialogType.CONFIRMATION -> R.color.m3_primary
}

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
            is UiEvent.ShowAlertDialog -> showAlertDialog(
                title = event.title,
                message = event.message,
                type = event.type,
            )
            else -> {}
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
        ivLogo.setImageResource(type.toIconRes())
        val iconColor = ContextCompat.getColor(this, type.toIconTintRes())
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

        val positiveColor = ContextCompat.getColor(this, type.toPositiveButtonColorRes())
        dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)?.setTextColor(positiveColor)
    }
}
