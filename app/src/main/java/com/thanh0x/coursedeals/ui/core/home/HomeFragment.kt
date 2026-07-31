package com.thanh0x.coursedeals.ui.core.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.paging.LoadState
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.databinding.FragmentHomeBinding
import com.thanh0x.coursedeals.domain.model.CourseLanguage
import com.thanh0x.coursedeals.domain.model.FilterData
import com.thanh0x.coursedeals.domain.model.SearchSuggestion
import com.thanh0x.coursedeals.domain.model.SortOption
import com.thanh0x.coursedeals.domain.model.SuggestionType
import com.thanh0x.coursedeals.ui.base.BaseFragment
import com.thanh0x.coursedeals.ui.detail.CouponDetailActivity
import com.thanh0x.coursedeals.util.BundleKey
import com.thanh0x.coursedeals.util.MapperToView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment() {
    private var _binding: FragmentHomeBinding? = null
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var couponCoursePagingAdapter: CouponCoursePagingViewAdapter
    private lateinit var suggestionsAdapter: NoFilterArrayAdapter
    private val binding get() = _binding!!

    private var currentFilter = FilterData()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        setupListeners()
        setupObservers()
        setupSearchViewSuggestions()
        homeViewModel.checkIfInternetAvailable()
    }

    private fun setupSearchViewSuggestions() {
        val searchAutoComplete = binding.svCouponCourse.findViewById<android.widget.AutoCompleteTextView>(
            androidx.appcompat.R.id.search_src_text,
        )
        suggestionsAdapter = NoFilterArrayAdapter(
            requireContext(),
            R.layout.item_search_suggestion,
            android.R.id.text1,
            mutableListOf(),
        )
        searchAutoComplete.setAdapter(suggestionsAdapter)
        searchAutoComplete.threshold = 0
        searchAutoComplete.setDropDownBackgroundResource(R.drawable.bg_search_suggestion_popup)
        searchAutoComplete.dropDownVerticalOffset = DROPDOWN_VERTICAL_OFFSET
        searchAutoComplete.setOnItemClickListener { parent, _, position, _ ->
            val suggestion = parent.getItemAtPosition(position) as SearchSuggestion
            binding.svCouponCourse.setQuery(suggestion.text, true)
            clearSearchFocus()
        }

        // Show history when focused even if empty
        searchAutoComplete.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.svCouponCourse.query.isNullOrBlank()) {
                searchAutoComplete.showDropDown()
            }
        }
    }

    private fun setupAdapter() {
        couponCoursePagingAdapter = CouponCoursePagingViewAdapter { clickedCoupon ->
            clearSearchFocus()
            val detailIntent = Intent(requireContext(), CouponDetailActivity::class.java)
            detailIntent.putExtra(BundleKey.TO_DETAIL_ACTIVITY, clickedCoupon.courseId)
            startActivity(detailIntent)
        }
        binding.rvCouponCourse.adapter = couponCoursePagingAdapter
    }

    private fun setupListeners() {
        binding.clHomeRoot.setOnClickListener {
            clearSearchFocus()
        }
        binding.rvCouponCourse.addOnScrollListener(
            object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(
                    recyclerView: androidx.recyclerview.widget.RecyclerView,
                    newState: Int,
                ) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState != androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE) {
                        clearSearchFocus()
                    }
                }
            },
        )
        binding.svCouponCourse.setOnQueryTextListener(queryTextChangeListener)
        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }
        binding.btnSubmitDeal.setOnClickListener {
            showSubmitDealDialog()
        }
        binding.tvStatUpdated.setOnClickListener {
            homeViewModel.toggleTimestampDisplay()
        }
        binding.srlHome.setOnRefreshListener {
            homeViewModel.refreshCoupons()
        }
        binding.btnClearFilters.setOnClickListener {
            homeViewModel.resetFilters()
        }
        binding.btnResetFilters.setOnClickListener {
            homeViewModel.resetFilters()
        }
    }

    private fun setupObservers() {
        collectFlow(homeViewModel.uiState) { state ->
            handleUiState(state)
        }

        collectFlow(homeViewModel.uiEvent) { event ->
            handleUiEvent(event)
        }

        observePagingData()
        observePagingLoadState()
    }

    private fun handleUiState(state: HomeUiState) {
        if (!state.isInternetAvailable) {
            showAlertDialog(
                getString(R.string.fetch_error_title),
                getString(R.string.no_internet_message),
            )
        }

        // Sync local currentFilter and search view with state (for Reset logic)
        currentFilter = state.filter
        if (binding.svCouponCourse.query.toString() != state.query) {
            binding.svCouponCourse.setQuery(state.query, false)
        }

        val isFiltered = state.query.isNotBlank() ||
            (state.filter.categories.isNotEmpty()) ||
            (state.filter.language != CourseLanguage.ALL) ||
            (state.filter.sortBy != SortOption.NEWEST) ||
            (state.filter.minRating > 0.0)

        val mapper = MapperToView(requireContext())

        binding.tvStatDeals.text = if (isFiltered) {
            getString(R.string.stat_matching_deals, state.matchingDeals, state.statDeals)
        } else {
            getString(R.string.stat_deals, state.statDeals)
        }

        val timestamp = if (state.showLocalFetchTime) {
            state.statFetchedTimestamp
        } else {
            state.statUpdatedTimestamp
        }

        val labelResId = if (state.showLocalFetchTime) {
            R.string.stat_fetched
        } else {
            R.string.stat_updated
        }

        binding.tvStatUpdated.text = getString(
            labelResId,
            mapper.mapTimeAgo(timestamp),
        )

        binding.tvStatUpdated.isVisible = !isFiltered
        binding.btnClearFilters.isVisible = isFiltered
        binding.btnSubmitDeal.isVisible = !isFiltered

        binding.srlHome.isRefreshing = state.isSyncing
        binding.llEmptyState.isVisible = state.isEmptyState

        updateSuggestions(state.suggestions)
    }

    private fun updateSuggestions(suggestions: List<SearchSuggestion>) {
        val searchAutoComplete = binding.svCouponCourse.findViewById<android.widget.AutoCompleteTextView>(
            androidx.appcompat.R.id.search_src_text,
        )

        suggestionsAdapter.clear()
        suggestionsAdapter.addAll(suggestions)

        if (searchAutoComplete.hasFocus() && suggestions.isNotEmpty()) {
            searchAutoComplete.showDropDown()
        }
    }

    private fun observePagingData() {
        collectFlow(homeViewModel.items) { pagingData ->
            couponCoursePagingAdapter.submitData(pagingData)
        }
    }

    private fun observePagingLoadState() {
        collectFlow(couponCoursePagingAdapter.loadStateFlow) { loadState ->
            val isRefreshing = loadState.refresh is LoadState.Loading
            binding.pbHome.isVisible = isRefreshing && (couponCoursePagingAdapter.itemCount == 0)

            binding.lpiLoadPreviousPage.isVisible = loadState.source.prepend is LoadState.Loading
            binding.lpiLoadNextPage.isVisible = loadState.source.append is LoadState.Loading

            val errorState = (loadState.source.append as? LoadState.Error)
                ?: (loadState.source.prepend as? LoadState.Error)
                ?: (loadState.append as? LoadState.Error)
                ?: (loadState.prepend as? LoadState.Error)
                ?: (loadState.refresh as? LoadState.Error)

            errorState?.let {
                showAlertDialog(
                    getString(R.string.fetch_error_title),
                    it.error.localizedMessage ?: getString(R.string.error_fetching_null_coupon),
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class NoFilterArrayAdapter(
        context: Context,
        resource: Int,
        textViewResourceId: Int,
        objects: List<SearchSuggestion>,
    ) : ArrayAdapter<SearchSuggestion>(context, resource, textViewResourceId, objects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            val suggestion = getItem(position)
            val iconView = view.findViewById<ImageView>(R.id.ivSuggestionIcon)
            val textView = view.findViewById<TextView>(android.R.id.text1)

            suggestion?.let {
                textView.text = it.text
                val iconRes = when (it.type) {
                    SuggestionType.HISTORY -> R.drawable.ic_clock
                    SuggestionType.KEYWORD -> R.drawable.ic_search
                }
                iconView.setImageResource(iconRes)
            }
            return view
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val results = FilterResults()
                    val allItems = mutableListOf<SearchSuggestion>()
                    for (i in 0 until count) {
                        getItem(i)?.let { allItems.add(it) }
                    }
                    results.values = allItems
                    results.count = allItems.size
                    return results
                }

                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    notifyDataSetChanged()
                }

                override fun convertResultToString(resultValue: Any?): CharSequence {
                    return (resultValue as? SearchSuggestion)?.text ?: ""
                }
            }
        }
    }

    private val queryTextChangeListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            query?.let {
                homeViewModel.onSearchSubmitted(it)
                clearSearchFocus()
            }
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            homeViewModel.updateQuery(newText ?: "")
            return true
        }
    }

    private fun showFilterDialog() {
        val dialog = FilterBottomSheetDialog(currentFilter) { filterData ->
            currentFilter = filterData
            applyFilters()
        }
        dialog.show(childFragmentManager, FilterBottomSheetDialog.TAG)
    }

    private fun showSubmitDealDialog() {
        val dialog = SubmitDealBottomSheet { _ ->
            showAlertDialog(
                getString(R.string.submit_deal_title),
                getString(R.string.submit_success_msg),
            )
        }
        dialog.show(childFragmentManager, SubmitDealBottomSheet.TAG)
    }

    private fun applyFilters() {
        homeViewModel.updateFilter(currentFilter)
    }

    private fun clearSearchFocus() {
        binding.svCouponCourse.clearFocus()
        hideKeyboard()
    }

    companion object {
        private const val DROPDOWN_VERTICAL_OFFSET = 8
    }
}
