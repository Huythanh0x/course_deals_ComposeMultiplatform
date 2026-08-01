package com.thanh0x.coursedeals.ui.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Checkable
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.thanh0x.coursedeals.R

class SelectableChipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr), Checkable {

    private val innerChip: Chip
    private val innerCheckBadge: MaterialCardView

    private var checked: Boolean = false

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_selectable_chip, this, true)
        innerChip = findViewById(R.id.innerChip)
        innerCheckBadge = findViewById(R.id.innerCheckBadge)

        context.obtainStyledAttributes(attrs, R.styleable.SelectableChipView).apply {
            val text = getString(R.styleable.SelectableChipView_chipText)
            val height = getDimensionPixelSize(R.styleable.SelectableChipView_chipHeight, -1)
            val isCheckedInitial = getBoolean(R.styleable.SelectableChipView_android_checked, false)

            setText(text)
            if (height != -1) {
                setChipHeight(height)
            }
            isChecked = isCheckedInitial
            recycle()
        }
    }

    fun setText(text: String?) {
        innerChip.text = text
    }

    fun getText(): String = innerChip.text.toString()

    fun setChipHeight(height: Int) {
        val params = innerChip.layoutParams
        params.height = height
        innerChip.layoutParams = params
    }

    fun setFullWidth(isFullWidth: Boolean) {
        val currentParams = layoutParams ?: LayoutParams(
            if (isFullWidth) LayoutParams.MATCH_PARENT else LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
        )

        if (isFullWidth) {
            currentParams.width = LayoutParams.MATCH_PARENT
            layoutParams = currentParams

            val chipParams = innerChip.layoutParams as LayoutParams
            chipParams.width = 0
            chipParams.startToStart = LayoutParams.PARENT_ID
            chipParams.endToEnd = LayoutParams.PARENT_ID
            innerChip.layoutParams = chipParams
        } else {
            currentParams.width = LayoutParams.WRAP_CONTENT
            layoutParams = currentParams

            val chipParams = innerChip.layoutParams as LayoutParams
            chipParams.width = LayoutParams.WRAP_CONTENT
            chipParams.startToStart = LayoutParams.PARENT_ID
            chipParams.endToEnd = LayoutParams.UNSET
            innerChip.layoutParams = chipParams
        }
    }

    override fun setChecked(checked: Boolean) {
        this.checked = checked
        innerChip.isChecked = checked
        innerCheckBadge.visibility = if (checked) VISIBLE else INVISIBLE
    }

    override fun isChecked(): Boolean = checked

    override fun toggle() {
        setChecked(!checked)
    }

    fun setOnChipClickListener(listener: (Boolean) -> Unit) {
        innerChip.setOnClickListener {
            toggle()
            listener(checked)
        }
    }
}
