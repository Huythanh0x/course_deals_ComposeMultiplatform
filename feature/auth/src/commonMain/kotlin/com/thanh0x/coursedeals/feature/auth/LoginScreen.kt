package com.thanh0x.coursedeals.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.thanh0x.coursedeals.core.ui.resources.Res
import com.thanh0x.coursedeals.core.ui.resources.stringResource

@Suppress("LongParameterList")
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onGoogleSignInClick: () -> Unit,
    onFingerprintClick: () -> Unit,
    onGuestModeClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSendOtpClick: (email: String) -> Unit = {},
    onOtpClick: (otp: String) -> Unit = {},
    onBackToEmailClick: () -> Unit = {},
    onAcceptTermsClick: () -> Unit = {},
    onDeclineTermsClick: () -> Unit = {},
    onLoginClick: (email: String, password: String) -> Unit = { email, _ -> onSendOtpClick(email) },
    onRegisterClick: (email: String, password: String, fullName: String) -> Unit = { email, _, _ -> onSendOtpClick(email) },
    onToggleAuthMode: () -> Unit = onBackToEmailClick,
) {
    val handleSendOtp: (String) -> Unit = { email ->
        onSendOtpClick(email)
        onLoginClick(email, "")
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .systemBarsPadding(),
    ) {
        val isLandscape = maxWidth > maxHeight
        if (isLandscape) {
            LoginLandscapeContent(
                uiState = uiState,
                onSendOtpClick = handleSendOtp,
                onSubmitOtp = onOtpClick,
                onBackToEmailClick = onBackToEmailClick,
                onToggleAuthMode = onToggleAuthMode,
                onGoogleSignInClick = onGoogleSignInClick,
                onFingerprintClick = onFingerprintClick,
                onGuestModeClick = onGuestModeClick,
            )
        } else {
            LoginPortraitContent(
                uiState = uiState,
                onSendOtpClick = handleSendOtp,
                onSubmitOtp = onOtpClick,
                onBackToEmailClick = onBackToEmailClick,
                onToggleAuthMode = onToggleAuthMode,
                onGoogleSignInClick = onGoogleSignInClick,
                onFingerprintClick = onFingerprintClick,
                onGuestModeClick = onGuestModeClick,
            )
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
            )
        }

        if (uiState.showTermsDialog) {
            TermsAndPrivacyDialog(
                onAccept = onAcceptTermsClick,
                onDecline = onDeclineTermsClick,
            )
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun LoginPortraitContent(
    uiState: LoginUiState,
    onSendOtpClick: (email: String) -> Unit,
    onSubmitOtp: (otp: String) -> Unit,
    onBackToEmailClick: () -> Unit,
    onToggleAuthMode: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onFingerprintClick: () -> Unit,
    onGuestModeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        LoginHeader()
        Spacer(modifier = Modifier.height(20.dp))
        LoginInfoMessage(message = uiState.infoMessage)
        LoginFormSection(
            authMode = AuthMode.LOGIN,
            pendingEmail = uiState.pendingEmail,
            onSendOtpClick = onSendOtpClick,
            onSubmitOtp = onSubmitOtp,
            onBackToEmailClick = onBackToEmailClick,
            onToggleAuthMode = onToggleAuthMode,
        )
        Spacer(modifier = Modifier.height(12.dp))
        SocialAuthSection(
            isFingerprintEnabled = uiState.isFingerprintEnabled,
            isFingerprintFeatureEnabled = uiState.isFingerprintFeatureEnabled,
            onGoogleSignInClick = onGoogleSignInClick,
            onFingerprintClick = onFingerprintClick,
        )
        Spacer(modifier = Modifier.height(4.dp))
        GuestModeButton(onClick = onGuestModeClick)
    }
}

@Suppress("LongParameterList")
@Composable
private fun LoginLandscapeContent(
    uiState: LoginUiState,
    onSendOtpClick: (email: String) -> Unit,
    onSubmitOtp: (otp: String) -> Unit,
    onBackToEmailClick: () -> Unit,
    onToggleAuthMode: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onFingerprintClick: () -> Unit,
    onGuestModeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            LoginHeader()
        }
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(scrollState)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LoginInfoMessage(message = uiState.infoMessage)
            LoginFormSection(
                authMode = AuthMode.LOGIN,
                pendingEmail = uiState.pendingEmail,
                onSendOtpClick = onSendOtpClick,
                onSubmitOtp = onSubmitOtp,
                onBackToEmailClick = onBackToEmailClick,
                onToggleAuthMode = onToggleAuthMode,
            )
            Spacer(modifier = Modifier.height(12.dp))
            SocialAuthSection(
                isFingerprintEnabled = uiState.isFingerprintEnabled,
                isFingerprintFeatureEnabled = uiState.isFingerprintFeatureEnabled,
                onGoogleSignInClick = onGoogleSignInClick,
                onFingerprintClick = onFingerprintClick,
            )
            Spacer(modifier = Modifier.height(4.dp))
            GuestModeButton(onClick = onGuestModeClick)
        }
    }
}

@Composable
private fun LoginInfoMessage(
    message: String?,
    modifier: Modifier = Modifier,
) {
    if (!message.isNullOrBlank()) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        )
    }
}
