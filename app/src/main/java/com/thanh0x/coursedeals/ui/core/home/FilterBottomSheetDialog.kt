package com.thanh0x.coursedeals.ui.core.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.databinding.DialogFilterBinding
import com.thanh0x.coursedeals.databinding.ItemCategoryPickerBinding
import com.thanh0x.coursedeals.domain.model.FilterData

class FilterBottomSheetDialog(
    private val initialFilter: FilterData,
    private val onFilterApplied: (FilterData) -> Unit,
) : BottomSheetDialogFragment() {

    private var _binding: DialogFilterBinding? = null
    private val binding get() = _binding!!

    private val categories = listOf(
        "Development", "Design", "Business", "IT & Software", "Marketing",
        "Personal Development", "Photography", "Music", "Health & Fitness", "Finance"
    )

    private val languages = listOf("All", "English", "Others")
    private val sortOptions = listOf("Rating", "Students", "Reviews", "Expiring Soon", "Newest")

    private val selectedCategories = mutableSetOf<String>()
    private var selectedLanguage: String? = null
    private var selectedSort: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedCategories.addAll(initialFilter.categories)
        selectedLanguage = initialFilter.language ?: "All"
        selectedSort = initialFilter.sortBy ?: "Newest"

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

    override fun onStart() {
        super.onStart()
        setupBottomSheetBehavior()
    }

    private fun setupBottomSheetBehavior() {
        (dialog as? BottomSheetDialog)?.let { bottomSheetDialog ->
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(
                com.google.android.material.R.id.design_bottom_sheet,
            )
            bottomSheet?.let {
                it.setBackgroundResource(android.R.color.transparent)
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true

                val layoutParams = it.layoutParams
                layoutParams.height = (resources.displayMetrics.heightPixels * HEIGHT_MULTIPLIER).toInt()
                it.layoutParams = layoutParams
            }
        }
    }

    private fun setupCategoryGroup() {
        categories.forEach { category ->
            val itemBinding = ItemCategoryPickerBinding.inflate(layoutInflater, binding.cgCategories, false)
            itemBinding.chip.text = category

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
        languages.forEach { language ->
            val itemBinding = ItemCategoryPickerBinding.inflate(layoutInflater, binding.cgLanguages, false)
            itemBinding.chip.text = language

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
        sortOptions.forEach { option ->
            val itemBinding = ItemCategoryPickerBinding.inflate(layoutInflater, binding.cgSort, false)
            itemBinding.chip.text = option

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
        selectedLanguage = "All"
        selectedSort = "Newest"

        clearGroupSelection(binding.cgCategories)
        clearGroupSelection(binding.cgLanguages)
        clearGroupSelection(binding.cgSort)

        // Select defaults
        updateChipSelection(binding.cgLanguages, "All")
        updateChipSelection(binding.cgSort, "Newest")
    }

    private fun updateChipSelection(group: ViewGroup, text: String) {
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
        private const val HEIGHT_MULTIPLIER = 0.9
    }
}
