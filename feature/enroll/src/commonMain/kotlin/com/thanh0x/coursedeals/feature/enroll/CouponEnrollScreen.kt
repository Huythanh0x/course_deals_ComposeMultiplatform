package com.thanh0x.coursedeals.feature.enroll

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thanh0x.coursedeals.core.ui.resources.Res
import com.thanh0x.coursedeals.core.ui.resources.stringResource
import com.thanh0x.coursedeals.core.ui.web.PlatformWebView

@Composable
fun CouponEnrollRoute(
    couponUrl: String?,
    isLoading: Boolean,
    isError: Boolean,
    errorMessage: String? = null,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit = {},
    onPageStarted: () -> Unit = {},
    onPageFinished: () -> Unit = {},
    onPageError: () -> Unit = {},
    onWebViewCreated: (Any) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    CouponEnrollScreen(
        couponUrl = couponUrl,
        isLoading = isLoading,
        isError = isError,
        errorMessage = errorMessage,
        onBackClick = onBackClick,
        onRetryClick = onRetryClick,
        onPageStarted = onPageStarted,
        onPageFinished = onPageFinished,
        onPageError = onPageError,
        onWebViewCreated = onWebViewCreated,
        modifier = modifier,
    )
}

@Composable
fun CouponEnrollScreen(
    couponUrl: String?,
    isLoading: Boolean,
    isError: Boolean,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onPageStarted: () -> Unit,
    onPageFinished: () -> Unit,
    onPageError: () -> Unit,
    onWebViewCreated: (Any) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            EnrollTopBar(
                title = stringResource(Res.string.text_button_enroll_now),
                onBackClick = onBackClick,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                isError || couponUrl.isNullOrBlank() -> {
                    val message = errorMessage
                        ?: stringResource(Res.string.error_fetching_null_coupon)
                    EnrollErrorState(
                        errorMessage = message,
                        onRetryClick = onRetryClick,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                else -> {
                    EnrollWebView(
                        url = couponUrl,
                        modifier = Modifier.fillMaxSize(),
                        onPageStarted = onPageStarted,
                        onPageFinished = onPageFinished,
                        onPageError = onPageError,
                        onWebViewCreated = onWebViewCreated,
                    )
                }
            }

            if (isLoading && !isError && !couponUrl.isNullOrBlank()) {
                EnrollLoadingState(
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.cancel_button_text),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = modifier,
    )
}

@Composable
fun EnrollLoadingState(
    modifier: Modifier = Modifier,
) {
    LinearProgressIndicator(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    )
}

@Suppress("UnusedParameter")
@Composable
fun EnrollWebView(
    url: String,
    modifier: Modifier = Modifier,
    onPageStarted: () -> Unit = {},
    onPageFinished: () -> Unit = {},
    onPageError: () -> Unit = {},
    onWebViewCreated: (Any) -> Unit = {},
) {
    PlatformWebView(
        url = url,
        modifier = modifier,
        onPageStarted = onPageStarted,
        onPageFinished = onPageFinished,
        onPageError = onPageError,
    )
}

@Composable
fun EnrollErrorState(
    errorMessage: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetryClick) {
                Text(text = stringResource(Res.string.retry_button_text))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EnrollTopBarPreview() {
    EnrollTopBar(
        title = "Enroll Now",
        onBackClick = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun EnrollLoadingStatePreview() {
    EnrollLoadingState()
}

@Preview(showBackground = true)
@Composable
private fun EnrollErrorStatePreview() {
    EnrollErrorState(
        errorMessage = "Unexpected error, try again !!!",
        onRetryClick = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun CouponEnrollScreenPreview() {
    CouponEnrollScreen(
        couponUrl = "https://udemy.com/example",
        isLoading = false,
        isError = false,
        errorMessage = null,
        onBackClick = {},
        onRetryClick = {},
        onPageStarted = {},
        onPageFinished = {},
        onPageError = {},
        onWebViewCreated = {},
    )
}
