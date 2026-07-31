package com.thanh0x.coursedeals.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.databinding.DialogReportBinding
import com.thanh0x.coursedeals.databinding.ItemReportReasonBinding
import com.thanh0x.coursedeals.ui.base.BaseBottomSheetDialog

class ReportBottomSheetDialog(
    private val onReportSubmitted: (String, String?) -> Unit,
) : BaseBottomSheetDialog() {

    private var _binding
    : DialogReportBinding? = null
    private val binding get() = _binding!!

    private val reasons = listOf(
        R.string.report_reason_not_free,
        R.string.report_reason_no_data,
        R.string.report_reason_expired,
        R.string.report_reason_other
    )

    private var selectedReason: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
            val itemBinding = ItemReportReasonBinding.inflate(layoutInflater, binding.cgReasons, false)
            itemBinding.chip.text = reasonStr

            itemBinding.chip.setOnClickListener {
                clearGroupSelection()
                selectedReason = reasonStr
                itemBinding.cvCheck.isVisible = true
                itemBinding.chip.isChecked = true

                binding.tilOtherReason.isVisible = reasonResId == R.string.report_reason_other
                if (binding.tilOtherReason.isVisible) {
                    binding.tietOtherReason.requestFocus()
                }
            }
            binding.cgReasons.addView(itemBinding.root)
        }
    }

    private fun clearGroupSelection() {
        for (i in 0 until binding.cgReasons.childCount) {
            val container = binding.cgReasons.getChildAt(i) as ViewGroup
            val chip = container.findViewById<Chip>(R.id.chip)
            val check = container.findViewById<View>(R.id.cvCheck)
            chip.isChecked = false
            check.isVisible = false
        }
    }

    private fun validateAndSubmit() {
        if (selectedReason == null) {
            // Shake or show error if needed, but for now just wait for selection
            return
        }

        val otherDetails = if (binding.tilOtherReason.isVisible) {
            binding.tietOtherReason.text.toString().trim()
        } else null

        if (binding.tilOtherReason.isVisible && otherDetails.isNullOrEmpty()) {
            binding.tilOtherReason.error = getString(R.string.report_other_hint)
            return
        }

        onReportSubmitted(selectedReason!!, otherDetails)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ReportBottomSheetDialog"
    }
}
