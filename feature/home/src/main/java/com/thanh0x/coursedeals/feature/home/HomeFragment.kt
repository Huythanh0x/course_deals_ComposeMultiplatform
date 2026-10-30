package com.thanh0x.coursedeals.feature.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.thanh0x.coursedeals.core.common.BundleKey
import com.thanh0x.coursedeals.core.ui.BaseFragment
import com.thanh0x.coursedeals.core.ui.DialogType
import com.thanh0x.coursedeals.core.ui.R as CoreR
import com.thanh0x.coursedeals.core.ui.theme.CourseDealsTheme
import com.thanh0x.coursedeals.domain.coupons.Coupon
import com.thanh0x.coursedeals.domain.coupons.FilterData
import com.thanh0x.coursedeals.feature.home.ui.FilterBottomSheetDialogContent
import com.thanh0x.coursedeals.feature.home.ui.HomeScreen
import com.thanh0x.coursedeals.feature.home.ui.SubmitDealBottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment() {

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CourseDealsTheme {
                    HomeRoute(
                        viewModel = homeViewModel,
                        onCouponClick = { coupon ->
                            navigateToDetail(coupon)
                        },
                        showAlertDialog = { title, message, type ->
                            showAlertDialog(
                                title = title,
                                message = message,
                                type = type,
                            )
                        },
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectFlow(homeViewModel.uiEvent) { event ->
            handleUiEvent(event)
        }
        homeViewModel.checkIfInternetAvailable()
    }

    private fun navigateToDetail(coupon: Coupon) {
        val detailIntent = Intent().apply {
            setClassName(requireContext().packageName, "com.thanh0x.coursedeals.feature.detail.CouponDetailActivity")
            putExtra(BundleKey.TO_DETAIL_ACTIVITY, coupon.courseId)
        }
        startActivity(detailIntent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeRoute(
    viewModel: HomeViewModel,
    onCouponClick: (Coupon) -> Unit,
    showAlertDialog: (String, String, DialogType) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagingItems = viewModel.items.collectAsLazyPagingItems()

    var showFilterSheet by remember { mutableStateOf(false) }
    var showSubmitDealSheet by remember { mutableStateOf(false) }

    // Observe error state
    val loadState = pagingItems.loadState
    val errorState = (loadState.source.append as? LoadState.Error)
        ?: (loadState.source.prepend as? LoadState.Error)
        ?: (loadState.append as? LoadState.Error)
        ?: (loadState.prepend as? LoadState.Error)
        ?: (loadState.refresh as? LoadState.Error)

    LaunchedEffect(errorState, pagingItems.itemCount) {
        if (errorState != null && pagingItems.itemCount == 0) {
            showAlertDialog(
                "Fetching Data Error",
                errorState.error.localizedMessage ?: "Unexpected error, try again !!!",
                DialogType.ERROR,
            )
        }
    }

    HomeScreen(
        uiState = uiState,
        pagingItems = pagingItems,
        onQueryChange = { query ->
            viewModel.updateQuery(query)
        },
        onSearchSubmit = { query ->
            viewModel.onSearchSubmitted(query)
        },
        onFilterClick = {
            showFilterSheet = true
        },
        onSubmitDealClick = {
            showSubmitDealSheet = true
        },
        onClearFiltersClick = {
            viewModel.resetFilters()
        },
        onToggleTimestampClick = {
            viewModel.toggleTimestampDisplay()
        },
        onRefresh = {
            viewModel.refreshCoupons()
            pagingItems.refresh()
        },
        onCouponClick = onCouponClick,
    )

    if (showFilterSheet) {
        FilterBottomSheetDialogContent(
            initialFilter = uiState.filter,
            onDismissRequest = {
                showFilterSheet = false
            },
            onApply = { newFilter ->
                viewModel.updateFilter(newFilter)
            },
        )
    }

    if (showSubmitDealSheet) {
        SubmitDealBottomSheetDialog(
            onDismissRequest = {
                showSubmitDealSheet = false
            },
            onSubmit = { url ->
                showAlertDialog(
                    "Submit a Deal",
                    "Deal submitted successfully! It will be reviewed soon.",
                    DialogType.INFO,
                )
            },
        )
    }
}
