package com.thanh0x.coursedeals.ui.core.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.databinding.FragmentHomeBinding
import com.thanh0x.coursedeals.ui.detail.CouponDetailActivity
import com.thanh0x.coursedeals.util.BundleKey
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var couponCoursePagingAdapter: CouponCoursePagingViewAdapter
    private val binding get() = _binding!!

    private var currentFilter = FilterData()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.svCouponCourse.setOnQueryTextListener(queryTextChangeListener)
        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }
        binding.btnSubmitDeal.setOnClickListener {
            showSubmitDealDialog()
        }
        couponCoursePagingAdapter = CouponCoursePagingViewAdapter() { clickedCoupon ->
            val detailIntent = Intent(requireContext(), CouponDetailActivity::class.java)
            detailIntent.putExtra(BundleKey.TO_DETAIL_ACTIVITY, clickedCoupon.courseId)
            startActivity(detailIntent)
        }
        binding.rvCouponCourse.adapter = couponCoursePagingAdapter
        setupStatLine()
        observeLoadingState()
        homeViewModel.isInternetAvailable.observe(viewLifecycleOwner) {
            if (it) {
                observeLoadingCourses()
            } else {
                showErrorFetchDialog(
                    resources.getString(R.string.fetch_error_title),
                    resources.getString(R.string.no_internet_message)
                )
            }
        }
        homeViewModel.checkIfInternetAvailable()
        return binding.root
    }

    private fun setupStatLine() {
        // Set sample values for now to avoid showing raw formatting tokens
        binding.tvStatDeals.text = getString(R.string.stat_deals, 128)
        binding.tvStatUpdated.text = getString(R.string.stat_updated, "2h")
    }

    private fun observeLoadingCourses() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                homeViewModel.items.collect {
                    couponCoursePagingAdapter.submitData(it)
                }
            }
        }
    }

    private fun observeLoadingState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                couponCoursePagingAdapter.loadStateFlow.collect { loadState ->
                    binding.pbHome.isVisible = loadState.refresh is LoadState.Loading
                    binding.lpiLoadPreviousPage.isVisible = loadState.source.prepend is LoadState.Loading
                    binding.lpiLoadNextPage.isVisible = loadState.source.append is LoadState.Loading

                    val errorState = loadState.source.append as? LoadState.Error
                        ?: loadState.source.prepend as? LoadState.Error
                        ?: loadState.append as? LoadState.Error
                        ?: loadState.prepend as? LoadState.Error
                        ?: loadState.refresh as? LoadState.Error

                    errorState?.let {
                        showErrorFetchDialog(
                            resources.getString(R.string.fetch_error_title),
                            it.error.localizedMessage ?: resources.getString(R.string.error_fetching_null_coupon)
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private val queryTextChangeListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            Log.d("SUBMITTED TEXT", query.toString())
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
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
            showErrorFetchDialog(
                getString(R.string.submit_deal_title),
                getString(R.string.submit_success_msg)
            )
        }
        dialog.show(childFragmentManager, SubmitDealBottomSheet.TAG)
    }

    private fun applyFilters() {
        // Log or show the applied filters for now
        Log.d("FILTER APPLIED", currentFilter.toString())
        // In a real implementation, you would update the ViewModel with these parameters
        // to trigger a new PagingData stream.
    }

    private fun showErrorFetchDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(
            requireActivity(),
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        ).setTitle(title).setMessage(message)
            .setPositiveButton(R.string.ok_text_button) { dialog, _ ->
                dialog.dismiss()
                homeViewModel.checkIfInternetAvailable()
            }
            .setCancelable(false)
            .create().show()
    }
}
