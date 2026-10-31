package com.thanh0x.coursedeals.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.thanh0x.coursedeals.core.ui.resources.Res
import com.thanh0x.coursedeals.core.ui.resources.painterResource
import com.thanh0x.coursedeals.core.ui.resources.stringResource
import com.thanh0x.coursedeals.core.ui.util.MapperToView
import com.thanh0x.coursedeals.domain.coupons.Coupon
import com.thanh0x.coursedeals.domain.coupons.CourseLanguage
import com.thanh0x.coursedeals.domain.coupons.SearchSuggestion
import com.thanh0x.coursedeals.domain.coupons.SortOption
import com.thanh0x.coursedeals.feature.home.HomeUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    pagingItems: LazyPagingItems<Coupon>,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onFilterClick: () -> Unit,
    onSubmitDealClick: () -> Unit,
    onClearFiltersClick: () -> Unit,
    onToggleTimestampClick: () -> Unit,
    onRefresh: () -> Unit,
    onCouponClick: (Coupon) -> Unit,
    modifier: Modifier = Modifier,
) {
    val mapper = remember { MapperToView() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var isSearchFocused by remember { mutableStateOf(false) }

    val isFiltered = uiState.query.isNotBlank() ||
        uiState.filter.categories.isNotEmpty() ||
        uiState.filter.language != CourseLanguage.ALL ||
        uiState.filter.sortBy != SortOption.NEWEST ||
        uiState.filter.minRating > 0.0

    val hasActiveFilters = uiState.filter.categories.isNotEmpty() ||
        uiState.filter.language != CourseLanguage.ALL ||
        uiState.filter.sortBy != SortOption.NEWEST ||
        uiState.filter.minRating > 0.0

    val isRefreshing = pagingItems.loadState.refresh is LoadState.Loading
    val isAppendLoading = pagingItems.loadState.append is LoadState.Loading
    val isPrependLoading = pagingItems.loadState.prepend is LoadState.Loading

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                focusManager.clearFocus()
                keyboardController?.hide()
                isSearchFocused = false
            },
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Linear progress indicator at the very top (Prepend loading)
            if (isPrependLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Top Search Bar + Filter Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Search TextField inside rounded pill
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = {
                        onQueryChange(it)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .onFocusChanged { focusState ->
                            isSearchFocused = focusState.isFocused
                        },
                    placeholder = {
                        Text(
                            text = stringResource(Res.string.search_here_hint_search),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    trailingIcon = {
                        if (uiState.query.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    onQueryChange("")
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            onSearchSubmit(uiState.query)
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            isSearchFocused = false
                        },
                    ),
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Filter Button with Badge
                Box {
                    FilledTonalIconButton(
                        onClick = onFilterClick,
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(Res.string.filter_title),
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    if (hasActiveFilters) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.TopEnd)
                                .padding(top = 4.dp, end = 4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error),
                        )
                    }
                }
            }

            // Stat deals row + action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Left: Deals count + Updated/Fetched timestamp
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val dealsText = if (isFiltered) {
                        stringResource(Res.string.stat_matching_deals, uiState.matchingDeals, uiState.statDeals)
                    } else {
                        stringResource(Res.string.stat_deals, uiState.statDeals)
                    }

                    Text(
                        text = dealsText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    if (!isFiltered) {
                        Spacer(modifier = Modifier.width(8.dp))

                        val timestamp = if (uiState.showLocalFetchTime) {
                            uiState.statFetchedTimestamp
                        } else {
                            uiState.statUpdatedTimestamp
                        }

                        val labelRes = if (uiState.showLocalFetchTime) {
                            Res.string.stat_fetched
                        } else {
                            Res.string.stat_updated
                        }

                        Text(
                            text = stringResource(labelRes, mapper.mapTimeAgo(timestamp)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable(onClick = onToggleTimestampClick),
                        )
                    }
                }

                // Right: Clear Filters or Submit Deal button
                if (isFiltered) {
                    FilledTonalButton(
                        onClick = onClearFiltersClick,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(Res.string.btn_clear),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else {
                    FilledTonalButton(
                        onClick = onSubmitDealClick,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_logo_mark),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(Res.string.submit_deal),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            // Content Grid with Pull to Refresh
            PullToRefreshBox(
                isRefreshing = uiState.isSyncing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                if (uiState.isEmptyState || (pagingItems.itemCount == 0 && !isRefreshing)) {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .padding(8.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(Res.string.empty_state_message),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        FilledTonalButton(onClick = onClearFiltersClick) {
                            Text(stringResource(Res.string.btn_reset_filters))
                        }
                    }
                } else {
                    // Course grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 6.dp, end = 6.dp, bottom = 16.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(
                            count = pagingItems.itemCount,
                            key = pagingItems.itemKey { it.courseId },
                        ) { index ->
                            val item = pagingItems[index]
                            if (item != null) {
                                CouponCourseCard(
                                    coupon = item,
                                    onClick = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        isSearchFocused = false
                                        onCouponClick(item)
                                    },
                                )
                            }
                        }

                        // Bottom loading indicator when appending next page
                        if (isAppendLoading) {
                            item(span = { GridItemSpan(2) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Linear Progress Indicator at the bottom (Append loading)
            if (isAppendLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        // Dropdown suggestions popup anchored below search bar
        if (isSearchFocused && uiState.suggestions.isNotEmpty()) {
            SearchSuggestionsDropdown(
                suggestions = uiState.suggestions,
                onSuggestionClick = { suggestion ->
                    onQueryChange(suggestion.text)
                    onSearchSubmit(suggestion.text)
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    isSearchFocused = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 72.dp, top = 68.dp),
            )
        }

        // Full Screen Loading (first refresh)
        if (isRefreshing && pagingItems.itemCount == 0) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
