package com.thanh0x.coursedeals.ui.core.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.databinding.DialogSubmitDealBinding
import com.thanh0x.coursedeals.ui.base.BaseBottomSheetDialog

class SubmitDealBottomSheet() : BaseBottomSheetDialog() {

    private var _binding: DialogSubmitDealBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogSubmitDealBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tietUrl.post {
            binding.tietUrl.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.tietUrl, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSubmit.setOnClickListener {
            validateAndSubmit()
        }

        binding.tietUrl.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validateAndSubmit()
                true
            } else {
                false
            }
        }
    }

    private fun validateAndSubmit() {
        binding.tietUrl.clearFocus()
        hideKeyboard()
        val url = binding.tietUrl.text.toString().trim()
        if (url.isEmpty()) {
            binding.tilUrl.error = getString(R.string.url_error_invalid)
        } else if (!url.contains("udemy.com")) {
            binding.tilUrl.error = getString(R.string.url_error_invalid)
        } else {
            binding.tilUrl.error = null
            setFragmentResult(REQUEST_KEY, bundleOf(EXTRA_COUPON_URL to url))
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "SubmitDealBottomSheet"
        const val REQUEST_KEY = "SubmitDealRequestKey"
        const val EXTRA_COUPON_URL = "ExtraCouponUrl"
    }
}
