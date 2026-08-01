package com.thanh0x.coursedeals.ui.base

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialog : BottomSheetDialogFragment() {

    override fun onStart() {
        super.onStart()
        setupBottomSheetBehavior()
    }

    fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val view = dialog?.currentFocus ?: view
        view?.let {
            imm?.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun setupBottomSheetBehavior() {
        (dialog as? BottomSheetDialog)?.let { bottomSheetDialog ->
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(
                com.google.android.material.R.id.design_bottom_sheet,
            )
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
                behavior.maxHeight = (resources.displayMetrics.heightPixels * HEIGHT_MULTIPLIER).toInt()
            }
        }
    }

    companion object {
        private const val HEIGHT_MULTIPLIER = 0.9
    }
}
