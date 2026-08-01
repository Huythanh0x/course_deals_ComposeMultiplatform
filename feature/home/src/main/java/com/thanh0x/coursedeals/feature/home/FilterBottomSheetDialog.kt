package com.thanh0x.coursedeals.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.thanh0x.coursedeals.core.ui.R as CoreR
import com.thanh0x.coursedeals.feature.home.R
import com.thanh0x.coursedeals.feature.home.databinding.DialogFilterBinding
import com.thanh0x.coursedeals.domain.coupons.CourseCategory
import com.thanh0x.coursedeals.domain.coupons.CourseLanguage
import com.thanh0x.coursedeals.domain.coupons.FilterData
import com.thanh0x.coursedeals.domain.coupons.SortOption
import com.thanh0x.coursedeals.core.ui.BaseBottomSheetDialog
import com.thanh0x.coursedeals.core.ui.SelectableChipView
import com.thanh0x.coursedeals.core.ui.util.categoryResId
import com.thanh0x.coursedeals.core.ui.util.languageResId
import com.thanh0x.coursedeals.core.ui.util.sortResId

class FilterBottomSheetDialog() : BaseBottomSheetDialog() {

    private var _binding: DialogFilterBinding? = null
    private val binding get() = _binding!!

    private val selectedCategories = mutableSetOf<CourseCategory>()
    private var selectedLanguage: CourseLanguage = CourseLanguage.ALL
    private var selectedSort: SortOption = SortOption.NEWEST
    private var selectedMinRating: Double = 0.0

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

        val initialFilter = arguments?.getParcelable<FilterData>(ARG_INITIAL_FILTER) ?: FilterData()

        selectedCategories.addAll(initialFilter.categories)
        selectedLanguage = initialFilter.language
        selectedSort = initialFilter.sortBy
        selectedMinRating = initialFilter.minRating

        setupCategoryGroup()
        setupRatingSlider()
        setupLanguageGroup()
        setupSortGroup()

        binding.btnReset.setOnClickListener {
            resetFilters()
        }

        binding.btnApply.setOnClickListener {
            val result = FilterData(
                selectedCategories.toList(),
                selectedLanguage,
                selectedSort,
                selectedMinRating,
            )
            setFragmentResult(REQUEST_KEY, bundleOf(EXTRA_FILTER_DATA to result))
            dismiss()
        }
    }

    private fun setupRatingSlider() {
        binding.sliderRating.value = ratingToSlider(selectedMinRating)
        updateRatingLabel(selectedMinRating)

        binding.sliderRating.setLabelFormatter { value ->
            val rating = sliderToRating(value)
            if (rating > 0.0) {
                getString(CoreR.string.filter_rating_format, rating)
            } else {
                getString(CoreR.string.filter_rating_any)
            }
        }

        binding.sliderRating.addOnChangeListener { _, value, _ ->
            selectedMinRating = sliderToRating(value)
            updateRatingLabel(selectedMinRating)
        }
    }

    private fun sliderToRating(value: Float): Double {
        return if (value == 0.0f) {
            0.0
        } else {
            MIN_QUALIFIED_RATING + ((value - 1.0) / RATING_STEP_FACTOR)
        }
    }

    private fun ratingToSlider(rating: Double): Float {
        return if (rating <= 0.0) {
            0.0f
        } else {
            (((rating - MIN_QUALIFIED_RATING) * RATING_STEP_FACTOR) + 1.0)
                .toFloat()
                .coerceIn(0.0f, MAX_SLIDER_VALUE)
        }
    }

    private fun updateRatingLabel(rating: Double) {
        binding.tvCurrentRating.text = if (rating > 0.0) {
            getString(CoreR.string.filter_rating_format, rating)
        } else {
            getString(CoreR.string.filter_rating_any)
        }
    }

    private fun setupCategoryGroup() {
        CourseCategory.entries.forEach { category ->
            val chipView = SelectableChipView(requireContext()).apply {
                setText(getString(category.categoryResId))
                setChipHeight(resources.getDimensionPixelSize(CoreR.dimen.spacing_32))

                // Wrap content for horizontal ChipGroup flow
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )

                isChecked = selectedCategories.contains(category)
                setOnChipClickListener { checked ->
                    if (checked) {
                        selectedCategories.add(category)
                    } else {
                        selectedCategories.remove(category)
                    }
                }
            }
            binding.cgCategories.addView(chipView)
        }
    }

    private fun setupLanguageGroup() {
        CourseLanguage.entries.forEach { language ->
            val chipView = SelectableChipView(requireContext()).apply {
                setText(getString(language.languageResId))
                setChipHeight(resources.getDimensionPixelSize(CoreR.dimen.spacing_32))

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )

                isChecked = selectedLanguage == language
                setOnChipClickListener {
                    clearGroupSelection(binding.cgLanguages)
                    selectedLanguage = language
                    isChecked = true
                }
            }
            binding.cgLanguages.addView(chipView)
        }
    }

    private fun setupSortGroup() {
        SortOption.entries.forEach { option ->
            val chipView = SelectableChipView(requireContext()).apply {
                setText(getString(option.sortResId))
                setChipHeight(resources.getDimensionPixelSize(CoreR.dimen.spacing_32))

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )

                isChecked = selectedSort == option
                setOnChipClickListener {
                    clearGroupSelection(binding.cgSort)
                    selectedSort = option
                    isChecked = true
                }
            }
            binding.cgSort.addView(chipView)
        }
    }

    private fun clearGroupSelection(group: ViewGroup) {
        for (i in 0 until group.childCount) {
            val chipView = group.getChildAt(i) as? SelectableChipView
            chipView?.isChecked = false
        }
    }

    private fun resetFilters() {
        selectedCategories.clear()
        selectedLanguage = CourseLanguage.ALL
        selectedSort = SortOption.NEWEST
        selectedMinRating = 0.0

        clearGroupSelection(binding.cgCategories)
        clearGroupSelection(binding.cgLanguages)
        clearGroupSelection(binding.cgSort)

        // Select defaults
        updateChipSelection(binding.cgLanguages, CourseLanguage.ALL)
        updateChipSelection(binding.cgSort, SortOption.NEWEST)

        binding.sliderRating.value = ratingToSlider(0.0)
        updateRatingLabel(0.0)
    }

    private fun updateChipSelection(group: ViewGroup, option: Any) {
        val text = when (option) {
            is CourseLanguage -> getString(option.languageResId)
            is SortOption -> getString(option.sortResId)
            else -> ""
        }
        for (i in 0 until group.childCount) {
            val chipView = group.getChildAt(i) as? SelectableChipView
            if (chipView?.getText() == text) {
                chipView.isChecked = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "FilterBottomSheetDialog"
        const val REQUEST_KEY = "FilterRequestKey"
        const val EXTRA_FILTER_DATA = "ExtraFilterData"
        private const val ARG_INITIAL_FILTER = "ArgInitialFilter"

        private const val MIN_QUALIFIED_RATING = 4.0
        private const val RATING_STEP_FACTOR = 10.0
        private const val MAX_SLIDER_VALUE = 11.0f

        fun newInstance(filter: FilterData): FilterBottomSheetDialog {
            return FilterBottomSheetDialog().apply {
                arguments = bundleOf(ARG_INITIAL_FILTER to filter)
            }
        }
    }
}
