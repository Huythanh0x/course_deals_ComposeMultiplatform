package com.thanh0x.coursedeals.core.ui

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton

class FragmentDialogOverlay(private val overlayRoot: View) {

    private val ivIcon: ImageView = overlayRoot.findViewById(R.id.ivOverlayIcon)
    private val tvTitle: TextView = overlayRoot.findViewById(R.id.tvOverlayTitle)
    private val tvMessage: TextView = overlayRoot.findViewById(R.id.tvOverlayMessage)
    private val btnPositive: MaterialButton = overlayRoot.findViewById(R.id.btnOverlayPositive)
    private val btnNegative: MaterialButton = overlayRoot.findViewById(R.id.btnOverlayNegative)

    fun show(
        type: DialogType = DialogType.INFO,
        title: String,
        message: String,
        positiveButtonText: String? = null,
        negativeButtonText: String? = null,
        onNegativeClick: (() -> Unit)? = null,
        onPositiveClick: (() -> Unit)? = null,
    ) {
        val context = overlayRoot.context

        ivIcon.setImageResource(type.iconRes)
        val iconColor = ContextCompat.getColor(context, type.iconTintRes)
        ivIcon.imageTintList = ColorStateList.valueOf(iconColor)

        tvTitle.text = title
        tvMessage.text = message

        val positiveText = positiveButtonText ?: context.getString(R.string.ok_text_button)
        btnPositive.text = positiveText
        val positiveColor = ContextCompat.getColor(context, type.positiveButtonColorRes)
        if (type == DialogType.WARNING || type == DialogType.ERROR) {
            btnPositive.setTextColor(positiveColor)
        }
        btnPositive.setOnClickListener {
            hide()
            onPositiveClick?.invoke()
        }

        if (negativeButtonText != null) {
            btnNegative.isVisible = true
            btnNegative.text = negativeButtonText
            btnNegative.setOnClickListener {
                hide()
                onNegativeClick?.invoke()
            }
        } else {
            btnNegative.isVisible = false
        }

        overlayRoot.isVisible = true
    }

    fun hide() {
        overlayRoot.isVisible = false
    }

    val isVisible: Boolean
        get() = overlayRoot.isVisible
}
