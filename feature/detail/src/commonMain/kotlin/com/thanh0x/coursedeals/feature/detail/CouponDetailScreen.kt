package com.thanh0x.coursedeals.feature.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thanh0x.coursedeals.core.common.platformCurrentTimeMillis
import com.thanh0x.coursedeals.core.ui.util.MapperToView
import com.thanh0x.coursedeals.domain.coupons.Coupon

@Composable
fun CouponDetailRoute(
    viewModel: CouponDetailViewModel,
    onBackClick: () -> Unit,
    onEnrollClick: (String?) -> Unit,
    onShareClick: (title: String, url: String) -> Unit,
    onReportSubmitted: (reason: String, otherDetails: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CouponDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onEnrollClick = onEnrollClick,
        onShareClick = onShareClick,
        onRefreshCache = { uiState.coupon?.courseId?.let { viewModel.fetchCouponDetail(it, true) } },
        onReportSubmitted = onReportSubmitted,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouponDetailScreen(
    uiState: CouponDetailUiState,
    onBackClick: () -> Unit,
    onEnrollClick: (String?) -> Unit,
    onShareClick: (title: String, url: String) -> Unit,
    onRefreshCache: () -> Unit,
    onReportSubmitted: (reason: String, otherDetails: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showReportSheet by remember { mutableStateOf(false) }
    var isBookmarked by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (uiState.coupon != null) {
                EnrollFloatingBottomBar(
                    onEnrollClick = { onEnrollClick(uiState.coupon.couponUrl) },
                    onReportClick = { showReportSheet = true },
                )
            }
        },
    ) { innerPadding ->
        CouponDetailContent(
            uiState = uiState,
            isBookmarked = isBookmarked,
            onBackClick = onBackClick,
            onBookmarkToggle = { isBookmarked = !isBookmarked },
            onShareClick = {
                uiState.coupon?.let { onShareClick(it.title ?: "", it.couponUrl ?: "") }
            },
            onRefreshCache = onRefreshCache,
            modifier = Modifier.padding(innerPadding),
        )
    }

    if (showReportSheet) {
        ReportDealBottomSheet(
            onDismissRequest = { showReportSheet = false },
            onSubmitReport = { reason, otherDetails ->
                showReportSheet = false
                onReportSubmitted(reason, otherDetails)
            },
        )
    }
}

@Composable
private fun CouponDetailContent(
    uiState: CouponDetailUiState,
    isBookmarked: Boolean,
    onBackClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onShareClick: () -> Unit,
    onRefreshCache: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coupon = uiState.coupon
    if (coupon == null) {
        if (uiState.isLoading) {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        return
    }

    val mapper = remember { MapperToView() }
    val timeLeftText = mapper.mapTimeLeft(coupon.expiredTime)
    val cachedAtText = remember(uiState.cachedAt) {
        uiState.cachedAt?.let { cachedMillis ->
            val diffMinutes = ((platformCurrentTimeMillis() - cachedMillis) / 60_000L).coerceAtLeast(0L)
            when {
                diffMinutes < 1L -> "Just now"
                diffMinutes < 60L -> "${diffMinutes}m ago"
                else -> "${diffMinutes / 60L}h ago"
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailHeroHeader(
            previewImage = coupon.previewImage,
            title = coupon.title ?: "",
            timeLeftText = timeLeftText,
            isBookmarked = isBookmarked,
            onBackClick = onBackClick,
            onBookmarkToggle = onBookmarkToggle,
            onShareClick = onShareClick,
        )
        CourseTitleSection(coupon = coupon)
        CourseRatingAndMetricsSection(coupon = coupon)
        CouponPricingSection(
            coupon = coupon,
            isFromCache = uiState.isFromCache,
            cachedAtText = cachedAtText,
            onRefreshCache = onRefreshCache,
        )
        CourseDescriptionSection(coupon = coupon)
    }
}
