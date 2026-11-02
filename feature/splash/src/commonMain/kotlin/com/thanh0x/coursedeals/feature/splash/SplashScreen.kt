package com.thanh0x.coursedeals.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thanh0x.coursedeals.core.ui.resources.Res
import com.thanh0x.coursedeals.core.ui.resources.painterResource
import com.thanh0x.coursedeals.core.ui.resources.stringResource
import com.thanh0x.coursedeals.core.ui.theme.CourseDealsTheme
import com.thanh0x.coursedeals.core.ui.util.PlatformActions

private const val FORCE_UPDATE_TITLE_TEXT = "Update Required"
private const val FORCE_UPDATE_MESSAGE_TEXT =
    "A new version of Course Deals is required to continue. " +
        "Please update the app to get the latest deals and improvements."
private const val FORCE_UPDATE_BUTTON_TEXT = "Update Now"

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    uiState: SplashUiState = SplashUiState(),
    onUpdateClick: (String) -> Unit = { PlatformActions.openUrl(it) },
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("splash_screen"),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            SplashBranding(
                modifier = Modifier.align(Alignment.Center),
            )
            if (uiState.isForceUpdateRequired) {
                ForceUpdateModalOverlay(
                    onUpdateClick = { onUpdateClick(uiState.forceUpdateUrl) },
                )
            } else if (uiState.isLoading) {
                SplashLoadingIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun ForceUpdateModalOverlay(
    onUpdateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("force_update_modal"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_logo_mark),
                    contentDescription = stringResource(Res.string.app_name),
                    modifier = Modifier.size(56.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = FORCE_UPDATE_TITLE_TEXT,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("force_update_title"),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = FORCE_UPDATE_MESSAGE_TEXT,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("force_update_message"),
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onUpdateClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("force_update_button"),
                ) {
                    Text(
                        text = FORCE_UPDATE_BUTTON_TEXT,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SplashBranding(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_logo_mark),
            contentDescription = stringResource(Res.string.app_name),
            modifier = Modifier.size(64.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SplashLoadingIndicator(
    modifier: Modifier = Modifier,
) {
    CircularProgressIndicator(
        modifier = modifier.size(48.dp),
        color = MaterialTheme.colorScheme.primary,
        strokeWidth = 4.dp,
    )
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    CourseDealsTheme {
        SplashScreen()
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenForceUpdatePreview() {
    CourseDealsTheme {
        SplashScreen(
            uiState = SplashUiState(
                isLoading = false,
                isForceUpdateRequired = true,
            ),
        )
    }
}
