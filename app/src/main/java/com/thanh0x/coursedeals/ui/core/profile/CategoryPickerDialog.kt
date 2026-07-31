package com.thanh0x.coursedeals.ui.core.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import com.thanh0x.coursedeals.databinding.DialogCategoryPickerBinding
import com.thanh0x.coursedeals.databinding.ItemCategoryPickerBinding
import com.thanh0x.coursedeals.domain.model.CourseCategory
import com.thanh0x.coursedeals.ui.base.BaseBottomSheetDialog

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
            val itemBinding = ItemCategoryPickerBinding.inflate(layoutInflater, binding.cgCategories, false)
            itemBinding.chip.text = categoryStr

            val isSelected = selectedItems.contains(categoryStr)
            itemBinding.cvCheck.isVisible = isSelected
            itemBinding.chip.isChecked = isSelected

            itemBinding.chip.setOnClickListener {
                if (selectedItems.contains(categoryStr)) {
                    selectedItems.remove(categoryStr)
                    itemBinding.cvCheck.isVisible = false
                    itemBinding.chip.isChecked = false
                } else {
                    selectedItems.add(categoryStr)
                    itemBinding.cvCheck.isVisible = true
                    itemBinding.chip.isChecked = true
                }
            }
            binding.cgCategories.addView(itemBinding.root)
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
