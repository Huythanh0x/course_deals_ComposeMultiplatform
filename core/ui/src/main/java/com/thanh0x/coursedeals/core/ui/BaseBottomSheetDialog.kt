package com.thanh0x.coursedeals.core.ui

import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialog : BottomSheetDialogFragment() {
    override fun onStart() {
        super.onStart()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        expandToFullHeight()
    }

    fun setFullWidth() {
        dialog?.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    /**
     * Expands the sheet to its full content height while anchoring it firmly to the bottom
     * of the screen in both portrait and landscape.
     *
     * In Material Components, [BottomSheetBehavior.isFitToContents] = true anchors the sheet
     * to the bottom of the parent layout (`parentHeight - childHeight`), ensuring that the
     * sheet is always anchored at the bottom edge rather than floating in mid-air. Setting
     * [BottomSheetBehavior.state] = STATE_EXPANDED and [BottomSheetBehavior.skipCollapsed] = true
     * ensures the entire content is shown immediately without getting stuck in a partial peek state.
     */
    protected fun expandToFullHeight() {
        val bottomSheet = (dialog as? BottomSheetDialog)
            ?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            ?: return

        BottomSheetBehavior.from(bottomSheet).apply {
            skipCollapsed = true
            isFitToContents = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}
