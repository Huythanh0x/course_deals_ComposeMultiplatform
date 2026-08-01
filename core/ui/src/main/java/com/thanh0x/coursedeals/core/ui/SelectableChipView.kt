package com.thanh0x.coursedeals.core.ui

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.chip.Chip

class SelectableChipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.chipStyle
) : Chip(context, attrs, defStyleAttr) {

    private var onChipClickListener: ((Boolean) -> Unit)? = null

    init {
        isClickable = true
        isCheckable = true
        setOnCheckedChangeListener { _, isChecked ->
            onChipClickListener?.invoke(isChecked)
        }
    }

    fun setOnChipClickListener(listener: (Boolean) -> Unit) {
        onChipClickListener = listener
    }

    fun setText(text: String) {
        this.text = text
    }

    fun getCustomText(): String = this.text.toString()

    fun setChipHeight(height: Int) {
        this.chipMinHeight = height.toFloat()
    }

    fun setFullWidth(fullWidth: Boolean) {
        if (fullWidth) {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }
}
