package com.thanh0x.coursedeals.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import com.google.android.material.chip.Chip
import com.thanh0x.coursedeals.core.ui.R as CoreR
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.databinding.DialogReportBinding
import com.thanh0x.coursedeals.core.ui.BaseBottomSheetDialog
import com.thanh0x.coursedeals.core.ui.SelectableChipView

class ReportBottomSheetDialog() : BaseBottomSheetDialog() {

    private var _binding: DialogReportBinding? = null
    private val binding get() = _binding!!

    private val reasons = listOf(
        CoreR.string.report_reason_not_free,
        CoreR.string.report_reason_no_data,
        CoreR.string.report_reason_expired,
        CoreR.string.report_reason_other,
    )

    private var selectedReason: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupReasonGroup()

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSubmit.setOnClickListener {
            validateAndSubmit()
        }

        binding.tietOtherReason.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validateAndSubmit()
                true
            } else {
                false
            }
        }
    }

    private fun setupReasonGroup() {
        reasons.forEach { reasonResId ->
            val reasonStr = getString(reasonResId)
            val chipView = SelectableChipView(requireContext()).apply {
                setText(reasonStr)
                setChipHeight(resources.getDimensionPixelSize(CoreR.dimen.spacing_48))
                setFullWidth(true)

                setOnChipClickListener {
                    clearGroupSelection()
                    selectedReason = reasonStr
                    isChecked = true

                    binding.tilOtherReason.isVisible = reasonResId == CoreR.string.report_reason_other
                    if (binding.tilOtherReason.isVisible) {
                        binding.tietOtherReason.requestFocus()
                    }
                }
            }
            binding.llReasons.addView(chipView)
        }
    }

    private fun clearGroupSelection() {
        for (i in 0 until binding.llReasons.childCount) {
            val chipView = binding.llReasons.getChildAt(i) as? SelectableChipView
            chipView?.isChecked = false
        }
    }

    private fun validateAndSubmit() {
        binding.tietOtherReason.clearFocus()
        hideKeyboard()
        if (selectedReason == null) {
            return
        }

        val otherDetails = if (binding.tilOtherReason.isVisible) {
            binding.tietOtherReason.text.toString().trim()
        } else {
            null
        }

        if (binding.tilOtherReason.isVisible && otherDetails.isNullOrEmpty()) {
            binding.tilOtherReason.error = getString(CoreR.string.report_other_hint)
            return
        }

        setFragmentResult(
            REQUEST_KEY,
            bundleOf(
                EXTRA_REASON to selectedReason,
                EXTRA_OTHER_DETAILS to otherDetails,
            ),
        )
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ReportBottomSheetDialog"
        const val REQUEST_KEY = "ReportRequestKey"
        const val EXTRA_REASON = "ExtraReason"
        const val EXTRA_OTHER_DETAILS = "ExtraOtherDetails"
    }
}
