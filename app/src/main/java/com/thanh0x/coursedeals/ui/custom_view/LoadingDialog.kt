package com.thanh0x.coursedeals.ui.custom_view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.databinding.DialogLoadingBinding
import androidx.core.graphics.drawable.toDrawable

class LoadingDialog : DialogFragment() {

    private var _binding: DialogLoadingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getString(ARG_MESSAGE)?.let { message ->
            binding.tvLoadingMessage.text = message
            binding.tvLoadingMessage.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_MESSAGE = "arg_message"

        fun newInstance(message: String? = null): LoadingDialog {
            val fragment = LoadingDialog()
            val args = Bundle()
            if (message != null) {
                args.putString(ARG_MESSAGE, message)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
