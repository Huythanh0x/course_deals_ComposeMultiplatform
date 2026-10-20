package com.thanh0x.coursedeals.core.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.chip.Chip

class SelectableChipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.chipStyle
) : Chip(context, attrs, defStyleAttr) {

    private var onChipClickListener: ((Boolean) -> Unit)? = null
    private var suppressChipClickListener = false

    init {
        isClickable = true
        isCheckable = true

        // Selected vs. unselected chips must be visually distinguishable: plain
        // background/no border when unselected, a distinct fill + visible border when
        // selected. These color selectors already key off android:state_checked.
        chipBackgroundColor = AppCompatResources.getColorStateList(context, R.color.cat_chip_bg_selector)
        chipStrokeColor = AppCompatResources.getColorStateList(context, R.color.cat_chip_stroke_selector)
        chipStrokeWidth = resources.getDimension(R.dimen.spacing_1)
        setTextColor(AppCompatResources.getColorStateList(context, R.color.cat_chip_text_selector))

        setOnCheckedChangeListener { _, isChecked ->
            if (!suppressChipClickListener) {
                onChipClickListener?.invoke(isChecked)
            }
        }
    }

    fun setOnChipClickListener(listener: (Boolean) -> Unit) {
        onChipClickListener = listener
    }

    /**
     * Sets [isChecked] without notifying the click listener — for callers driving
     * selection state programmatically (e.g. clearing sibling chips in a single-select
     * group). A real user tap always goes through Chip's own touch handling, never
     * through this method, so it's unaffected.
     */
    fun setCheckedSilently(checked: Boolean) {
        suppressChipClickListener = true
        isChecked = checked
        suppressChipClickListener = false
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
