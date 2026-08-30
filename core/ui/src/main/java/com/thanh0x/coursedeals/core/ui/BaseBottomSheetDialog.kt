package com.thanh0x.coursedeals.core.ui

import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialog : BottomSheetDialogFragment() {
    fun setFullWidth() {
        dialog?.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    /**
     * Expands the sheet to fill the full available height as soon as it's shown, in both
     * portrait and landscape. Call this from `onStart()` in subclasses that need a
     * full-height sheet (rather than Material's default partial "peek" presentation).
     *
     * Material's [BottomSheetBehavior] normally sizes the expanded state to the sheet's
     * *content* height ([BottomSheetBehavior.isFitToContents] = true), capped at the
     * available parent height. That's fine in portrait, where the parent is usually taller
     * than the content, but in landscape the parent height shrinks enough that content-fit
     * sizing (or a height computed once as a fraction of `resources.displayMetrics`) can
     * leave the sheet short of the screen edge instead of filling it. Setting
     * `isFitToContents = false` makes the behavior measure and lay out the sheet against
     * the actual available parent height at layout time instead of its content size, so
     * this is correct for either orientation without any hardcoded pixel/fraction math.
     */
    protected fun expandToFullHeight() {
        val bottomSheet = (dialog as? BottomSheetDialog)
            ?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            ?: return

        BottomSheetBehavior.from(bottomSheet).apply {
            skipCollapsed = true
            isFitToContents = false
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}
