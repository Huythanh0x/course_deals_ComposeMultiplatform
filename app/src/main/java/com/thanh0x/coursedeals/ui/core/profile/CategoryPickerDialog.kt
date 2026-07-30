package com.thanh0x.coursedeals.ui.core.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thanh0x.coursedeals.databinding.DialogCategoryPickerBinding
import com.thanh0x.coursedeals.databinding.ItemCategoryPickerBinding

class CategoryPickerDialog(
    private val initialSelected: List<String>,
    private val onCategoriesSelected: (List<String>) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogCategoryPickerBinding? = null
    private val binding get() = _binding!!

    private val categories = listOf(
        "Development", "Design", "Business", "IT & Software", "Marketing",
        "Personal Development", "Photography", "Music", "Health & Fitness", "Finance"
    )

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

        categories.forEach { category ->
            val itemBinding = ItemCategoryPickerBinding.inflate(layoutInflater, binding.cgCategories, false)
            itemBinding.chip.text = category
            
            // Sync UI state
            val isSelected = selectedItems.contains(category)
            itemBinding.cvCheck.isVisible = isSelected
            itemBinding.chip.isChecked = isSelected

            itemBinding.chip.setOnClickListener {
                if (selectedItems.contains(category)) {
                    selectedItems.remove(category)
                    itemBinding.cvCheck.isVisible = false
                    itemBinding.chip.isChecked = false
                } else {
                    selectedItems.add(category)
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
