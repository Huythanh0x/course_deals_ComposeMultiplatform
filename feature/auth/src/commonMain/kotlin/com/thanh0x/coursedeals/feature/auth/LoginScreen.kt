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

@Suppress("LongParameterList")
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onLoginClick: (email: String, password: String) -> Unit,
    onRegisterClick: (email: String, password: String, fullName: String) -> Unit,
    onOtpClick: (otp: String) -> Unit,
    onToggleAuthMode: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onFingerprintClick: () -> Unit,
    onGuestModeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                onLoginClick = onLoginClick,
                onRegisterClick = onRegisterClick,
                onOtpClick = onOtpClick,
                onToggleAuthMode = onToggleAuthMode,
                onGoogleSignInClick = onGoogleSignInClick,
                onFingerprintClick = onFingerprintClick,
                onGuestModeClick = onGuestModeClick,
            )
        } else {
            LoginPortraitContent(
                uiState = uiState,
                onLoginClick = onLoginClick,
                onRegisterClick = onRegisterClick,
                onOtpClick = onOtpClick,
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
    }
}

@Suppress("LongParameterList")
@Composable
private fun LoginPortraitContent(
    uiState: LoginUiState,
    onLoginClick: (email: String, password: String) -> Unit,
    onRegisterClick: (email: String, password: String, fullName: String) -> Unit,
    onOtpClick: (otp: String) -> Unit,
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
            authMode = uiState.authMode,
            onSubmitLogin = onLoginClick,
            onSubmitRegister = onRegisterClick,
            onSubmitOtp = onOtpClick,
            onToggleAuthMode = onToggleAuthMode,
        )
        Spacer(modifier = Modifier.height(12.dp))
        SocialAuthSection(
            isFingerprintEnabled = uiState.isFingerprintEnabled,
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
    onLoginClick: (email: String, password: String) -> Unit,
    onRegisterClick: (email: String, password: String, fullName: String) -> Unit,
    onOtpClick: (otp: String) -> Unit,
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
                authMode = uiState.authMode,
                onSubmitLogin = onLoginClick,
                onSubmitRegister = onRegisterClick,
                onSubmitOtp = onOtpClick,
                onToggleAuthMode = onToggleAuthMode,
            )
            Spacer(modifier = Modifier.height(12.dp))
            SocialAuthSection(
                isFingerprintEnabled = uiState.isFingerprintEnabled,
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
