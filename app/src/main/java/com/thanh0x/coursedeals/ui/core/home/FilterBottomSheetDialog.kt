package com.thanh0x.coursedeals.ui.core.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.databinding.DialogFilterBinding
import com.thanh0x.coursedeals.databinding.ItemCategoryPickerBinding
import com.thanh0x.coursedeals.domain.model.CourseCategory
import com.thanh0x.coursedeals.domain.model.CourseLanguage
import com.thanh0x.coursedeals.domain.model.FilterData
import com.thanh0x.coursedeals.domain.model.SortOption
import com.thanh0x.coursedeals.ui.base.BaseBottomSheetDialog

class FilterBottomSheetDialog(
    private val initialFilter: FilterData,
    private val onFilterApplied: (FilterData) -> Unit,
) : BaseBottomSheetDialog() {

    private var _binding: DialogFilterBinding? = null
    private val binding get() = _binding!!

    private val selectedCategories = mutableSetOf<CourseCategory>()
    private var selectedLanguage: CourseLanguage = CourseLanguage.ALL
    private var selectedSort: SortOption = SortOption.NEWEST

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedCategories.addAll(initialFilter.categories)
        selectedLanguage = initialFilter.language
        selectedSort = initialFilter.sortBy

        setupCategoryGroup()
        setupLanguageGroup()
        setupSortGroup()

        binding.btnReset.setOnClickListener {
            resetFilters()
        }

        binding.btnApply.setOnClickListener {
            onFilterApplied(FilterData(selectedCategories.toList(), selectedLanguage, selectedSort))
            dismiss()
        }
    }

    private fun setupCategoryGroup() {
        CourseCategory.entries.forEach { category ->
            val itemBinding = ItemCategoryPickerBinding.inflate(layoutInflater, binding.cgCategories, false)
            itemBinding.chip.text = getString(category.displayResId)

            val isSelected = selectedCategories.contains(category)
            itemBinding.cvCheck.isVisible = isSelected
            itemBinding.chip.isChecked = isSelected

            itemBinding.chip.setOnClickListener {
                if (selectedCategories.contains(category)) {
                    selectedCategories.remove(category)
                    itemBinding.cvCheck.isVisible = false
                    itemBinding.chip.isChecked = false
                } else {
                    selectedCategories.add(category)
                    itemBinding.cvCheck.isVisible = true
                    itemBinding.chip.isChecked = true
                }
            }
            binding.cgCategories.addView(itemBinding.root)
        }
    }

    private fun setupLanguageGroup() {
        CourseLanguage.entries.forEach { language ->
            val itemBinding = ItemCategoryPickerBinding.inflate(layoutInflater, binding.cgLanguages, false)
            itemBinding.chip.text = getString(language.displayResId)

            val isSelected = selectedLanguage == language
            itemBinding.cvCheck.isVisible = isSelected
            itemBinding.chip.isChecked = isSelected

            itemBinding.chip.setOnClickListener {
                clearGroupSelection(binding.cgLanguages)
                selectedLanguage = language
                itemBinding.cvCheck.isVisible = true
                itemBinding.chip.isChecked = true
            }
            binding.cgLanguages.addView(itemBinding.root)
        }
    }

    private fun setupSortGroup() {
        SortOption.entries.forEach { option ->
            val itemBinding = ItemCategoryPickerBinding.inflate(layoutInflater, binding.cgSort, false)
            itemBinding.chip.text = getString(option.displayResId)

            val isSelected = selectedSort == option
            itemBinding.cvCheck.isVisible = isSelected
            itemBinding.chip.isChecked = isSelected

            itemBinding.chip.setOnClickListener {
                clearGroupSelection(binding.cgSort)
                selectedSort = option
                itemBinding.cvCheck.isVisible = true
                itemBinding.chip.isChecked = true
            }
            binding.cgSort.addView(itemBinding.root)
        }
    }

    private fun clearGroupSelection(group: ViewGroup) {
        for (i in 0 until group.childCount) {
            val container = group.getChildAt(i) as ViewGroup
            val chip = container.findViewById<Chip>(R.id.chip)
            val check = container.findViewById<View>(R.id.cvCheck)
            chip.isChecked = false
            check.isVisible = false
        }
    }

    private fun resetFilters() {
        selectedCategories.clear()
        selectedLanguage = CourseLanguage.ALL
        selectedSort = SortOption.NEWEST

        clearGroupSelection(binding.cgCategories)
        clearGroupSelection(binding.cgLanguages)
        clearGroupSelection(binding.cgSort)

        // Select defaults
        updateChipSelection(binding.cgLanguages, CourseLanguage.ALL.displayResId)
        updateChipSelection(binding.cgSort, SortOption.NEWEST.displayResId)
    }

    private fun updateChipSelection(group: ViewGroup, displayResId: Int) {
        val text = getString(displayResId)
        for (i in 0 until group.childCount) {
            val container = group.getChildAt(i) as ViewGroup
            val chip = container.findViewById<Chip>(R.id.chip)
            val check = container.findViewById<View>(R.id.cvCheck)
            if (chip.text == text) {
                chip.isChecked = true
                check.isVisible = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "FilterBottomSheetDialog"
    }
}
