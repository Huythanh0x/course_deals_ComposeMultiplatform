package com.thanh0x.coursedeals.core.ui

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AppDialogFragment : DialogFragment() {

    private var onPositiveClick: (() -> Unit)? = null
    private var onNegativeClick: (() -> Unit)? = null
    private var onDismissAction: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = arguments ?: Bundle()
        val typeName = args.getString(ARG_TYPE, DialogType.INFO.name)
        val type = runCatching { DialogType.valueOf(typeName) }.getOrDefault(DialogType.INFO)
        val title = args.getString(ARG_TITLE, "")
        val message = args.getString(ARG_MESSAGE, "")
        val positiveButtonText = args.getString(ARG_POSITIVE_TEXT)
        val negativeButtonText = args.getString(ARG_NEGATIVE_TEXT)

        val context = requireContext()
        val headerView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_header, null)
        val tvTitle = headerView.findViewById<TextView>(R.id.tvDialogTitle)
        val ivLogo = headerView.findViewById<ImageView>(R.id.ivDialogLogo)

        tvTitle.text = title
        ivLogo.setImageResource(type.iconRes)
        val iconColor = ContextCompat.getColor(context, type.iconTintRes)
        ivLogo.imageTintList = ColorStateList.valueOf(iconColor)

        val positiveText = positiveButtonText ?: getString(R.string.ok_text_button)
        val builder = MaterialAlertDialogBuilder(
            context,
            R.style.ThemeOverlay_CourseDeal_MaterialAlertDialog_Centered,
        ).setCustomTitle(headerView)
            .setMessage(message)
            .setPositiveButton(positiveText) { dialogInterface, _ ->
                dialogInterface.dismiss()
                onPositiveClick?.invoke()
                onDismissAction?.invoke()
            }

        if (negativeButtonText != null) {
            builder.setNegativeButton(negativeButtonText) { dialogInterface, _ ->
                dialogInterface.dismiss()
                onNegativeClick?.invoke()
            }
        }

        val alertDialog = builder.create()
        alertDialog.setOnShowListener {
            val positiveColor = ContextCompat.getColor(context, type.positiveButtonColorRes)
            alertDialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)?.setTextColor(positiveColor)
        }
        return alertDialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            // Non-modal: allow touches outside dialog bounds to pass directly to underlying views (e.g. BottomNavigation)
            addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        onDismissAction?.invoke()
    }

    fun setListeners(
        onPositiveClick: (() -> Unit)? = null,
        onNegativeClick: (() -> Unit)? = null,
        onDismiss: (() -> Unit)? = null,
    ) {
        this.onPositiveClick = onPositiveClick
        this.onNegativeClick = onNegativeClick
        this.onDismissAction = onDismiss
    }

    companion object {
        private const val ARG_TYPE = "arg_type"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_MESSAGE = "arg_message"
        private const val ARG_POSITIVE_TEXT = "arg_positive_text"
        private const val ARG_NEGATIVE_TEXT = "arg_negative_text"

        fun newInstance(
            title: String,
            message: String,
            type: DialogType = DialogType.INFO,
            positiveButtonText: String? = null,
            negativeButtonText: String? = null,
        ): AppDialogFragment {
            return AppDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TYPE, type.name)
                    putString(ARG_TITLE, title)
                    putString(ARG_MESSAGE, message)
                    putString(ARG_POSITIVE_TEXT, positiveButtonText)
                    putString(ARG_NEGATIVE_TEXT, negativeButtonText)
                }
            }
        }
    }
}
