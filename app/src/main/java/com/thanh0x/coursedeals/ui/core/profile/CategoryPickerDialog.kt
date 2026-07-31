package com.thanh0x.coursedeals.ui.core.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.thanh0x.coursedeals.databinding.DialogCategoryPickerBinding
import com.thanh0x.coursedeals.databinding.ItemCategoryPickerBinding
import com.thanh0x.coursedeals.domain.model.CourseCategory
import com.thanh0x.coursedeals.ui.base.BaseBottomSheetDialog

class CategoryPickerDialog(
    private val initialSelected: List<String>,
    private val onCategoriesSelected: (List<String>) -> Unit,
) : BaseBottomSheetDialog() {

    private var _binding: DialogCategoryPickerBinding? = null
    private val binding get() = _binding!!

    private val selectedItems = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCategoryPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            onCategoriesSelected(selectedItems.toList())
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CategoryPickerDialog"
    }
}
