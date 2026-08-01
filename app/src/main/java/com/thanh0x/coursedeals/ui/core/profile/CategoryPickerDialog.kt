package com.thanh0x.coursedeals.ui.core.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.databinding.DialogCategoryPickerBinding
import com.thanh0x.coursedeals.domain.model.CourseCategory
import com.thanh0x.coursedeals.ui.base.BaseBottomSheetDialog
import com.thanh0x.coursedeals.ui.customview.SelectableChipView

class CategoryPickerDialog : BaseBottomSheetDialog() {

    private var _binding: DialogCategoryPickerBinding? = null
    private val binding get() = _binding!!

    private val selectedItems = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogCategoryPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val initialSelected = arguments?.getStringArrayList(ARG_INITIAL_SELECTED) ?: emptyList<String>()
        selectedItems.addAll(initialSelected)

        CourseCategory.entries.forEach { category ->
            val categoryStr = getString(category.displayResId)
            val chipView = SelectableChipView(requireContext()).apply {
                setText(categoryStr)
                setChipHeight(resources.getDimensionPixelSize(R.dimen.spacing_32))

                // Wrap content for horizontal ChipGroup flow
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )

                isChecked = selectedItems.contains(categoryStr)
                setOnChipClickListener { checked ->
                    if (checked) {
                        selectedItems.add(categoryStr)
                    } else {
                        selectedItems.remove(categoryStr)
                    }
                }
            }
            binding.cgCategories.addView(chipView)
        }

        binding.btnDone.setOnClickListener {
            setFragmentResult(REQUEST_KEY, bundleOf(EXTRA_SELECTED_CATEGORIES to ArrayList(selectedItems)))
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CategoryPickerDialog"
        const val REQUEST_KEY = "CategoryPickerRequestKey"
        const val EXTRA_SELECTED_CATEGORIES = "ExtraSelectedCategories"
        private const val ARG_INITIAL_SELECTED = "ArgInitialSelected"

        fun newInstance(selected: List<String>): CategoryPickerDialog {
            return CategoryPickerDialog().apply {
                arguments = bundleOf(ARG_INITIAL_SELECTED to ArrayList(selected))
            }
        }
    }
}
