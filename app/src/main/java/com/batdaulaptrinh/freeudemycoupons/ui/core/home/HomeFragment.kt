package com.batdaulaptrinh.freeudemycoupons.ui.core.home

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
import com.batdaulaptrinh.freeudemycoupons.R
import com.batdaulaptrinh.freeudemycoupons.databinding.FragmentHomeBinding
import com.batdaulaptrinh.freeudemycoupons.ui.detail.CouponDetailActivity
import com.batdaulaptrinh.freeudemycoupons.util.BundleKey
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var couponCoursePagingAdapter: CouponCoursePagingViewAdapter
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.svCouponCourse.setOnQueryTextListener(queryTextChangeListener)
        couponCoursePagingAdapter = CouponCoursePagingViewAdapter() { clickedCoupon ->
            val detailIntent = Intent(requireContext(), CouponDetailActivity::class.java)
            detailIntent.putExtra(BundleKey.TO_DETAIL_ACTIVITY, clickedCoupon.courseId)
            startActivity(detailIntent)
        }
        binding.rvCouponCourse.adapter = couponCoursePagingAdapter
        observeInitialLoading()
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
                couponCoursePagingAdapter.loadStateFlow.collect {
                    binding.lpiLoadPreviousPage.isVisible = it.source.prepend is LoadState.Loading
                    binding.lpiLoadNextPage.isVisible = it.source.append is LoadState.Loading
                }
            }
        }
    }

    private fun observeInitialLoading() {
        lifecycleScope.launch {
            couponCoursePagingAdapter.loadStateFlow.collect {
                if (it.prepend is LoadState.NotLoading && it.prepend.endOfPaginationReached) {
                    binding.pbHome.visibility = View.GONE
                }
                if (it.append is LoadState.NotLoading && it.append.endOfPaginationReached) {
                    binding.pbHome.isVisible = couponCoursePagingAdapter.itemCount < 1
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
