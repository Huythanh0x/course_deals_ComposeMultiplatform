package com.thanh0x.coursedeals.feature.home.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.thanh0x.coursedeals.core.ui.DialogType
import com.thanh0x.coursedeals.domain.coupons.Coupon
import com.thanh0x.coursedeals.feature.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onCouponClick: (Coupon) -> Unit,
    showAlertDialog: (String, String, DialogType) -> Unit = { _, _, _ -> },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagingItems = viewModel.items.collectAsLazyPagingItems()

    var showFilterSheet by remember { mutableStateOf(false) }
    var showSubmitDealSheet by remember { mutableStateOf(false) }

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
        modifier = modifier,
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
            onSubmit = { _ ->
                showAlertDialog(
                    "Submit a Deal",
                    "Deal submitted successfully! It will be reviewed soon.",
                    DialogType.INFO,
                )
            },
        )
    }
}
