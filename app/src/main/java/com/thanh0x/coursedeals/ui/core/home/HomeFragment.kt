package com.thanh0x.coursedeals.ui.core.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.paging.LoadState
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.databinding.FragmentHomeBinding
import com.thanh0x.coursedeals.domain.model.FilterData
import com.thanh0x.coursedeals.ui.base.BaseFragment
import com.thanh0x.coursedeals.ui.detail.CouponDetailActivity
import com.thanh0x.coursedeals.util.BundleKey
import com.thanh0x.coursedeals.util.MapperToView
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : BaseFragment() {
    private var _binding: FragmentHomeBinding? = null
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var couponCoursePagingAdapter: CouponCoursePagingViewAdapter
    private val binding get() = _binding!!

    private var currentFilter = FilterData()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        setupListeners()
        setupObservers()
        homeViewModel.checkIfInternetAvailable()
    }

    private fun setupAdapter() {
        couponCoursePagingAdapter = CouponCoursePagingViewAdapter() { clickedCoupon ->
            val detailIntent = Intent(requireContext(), CouponDetailActivity::class.java)
            detailIntent.putExtra(BundleKey.TO_DETAIL_ACTIVITY, clickedCoupon.courseId)
            startActivity(detailIntent)
        }
        binding.rvCouponCourse.adapter = couponCoursePagingAdapter
    }

    private fun setupListeners() {
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
                getString(R.string.no_internet_message)
            )
        }

        val mapper = MapperToView(requireContext())
        binding.tvStatDeals.text = getString(R.string.stat_deals, state.statDeals)

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
            mapper.mapTimeAgo(timestamp)
        )

        binding.srlHome.isRefreshing = state.isSyncing
    }

    private fun observePagingData() {
        collectFlow(homeViewModel.items) { pagingData ->
            couponCoursePagingAdapter.submitData(pagingData)
        }
    }

    private fun observePagingLoadState() {
        collectFlow(couponCoursePagingAdapter.loadStateFlow) { loadState ->
            val isRefreshing = loadState.refresh is LoadState.Loading
            binding.pbHome.isVisible = isRefreshing && couponCoursePagingAdapter.itemCount == 0

            binding.lpiLoadPreviousPage.isVisible = loadState.source.prepend is LoadState.Loading
            binding.lpiLoadNextPage.isVisible = loadState.source.append is LoadState.Loading

            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error
                ?: loadState.refresh as? LoadState.Error

            errorState?.let {
                showAlertDialog(
                    getString(R.string.fetch_error_title),
                    it.error.localizedMessage ?: getString(R.string.error_fetching_null_coupon)
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val queryTextChangeListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            Timber.d("SUBMITTED TEXT: $query")
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
        val dialog = SubmitDealBottomSheet { url ->
            showAlertDialog(
                getString(R.string.submit_deal_title),
                getString(R.string.submit_success_msg)
            )
        }
        dialog.show(childFragmentManager, SubmitDealBottomSheet.TAG)
    }

    private fun applyFilters() {
        homeViewModel.updateFilter(currentFilter)
    }
}
