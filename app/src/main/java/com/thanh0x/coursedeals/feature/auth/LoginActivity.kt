package com.thanh0x.coursedeals.feature.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.thanh0x.coursedeals.R as CoreR
import com.thanh0x.coursedeals.core.ui.BaseActivity
import com.thanh0x.coursedeals.core.ui.BiometricPromptUtils
import com.thanh0x.coursedeals.core.ui.DialogType
import com.thanh0x.coursedeals.core.ui.UiEvent
import com.thanh0x.coursedeals.core.ui.theme.CourseDealsTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

class LoginActivity : BaseActivity() {
    private val loginViewModel: LoginViewModel by inject()
    private val credentialManager by lazy { CredentialManager.create(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupObservers()

        if (intent.getStringExtra("AUTH_MODE") == "VERIFY_OTP") {
            loginViewModel.setAuthMode(AuthMode.VERIFY_OTP)
        }

        setContent {
            CourseDealsTheme {
                val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()
                LoginScreen(
                    uiState = uiState,
                    onLoginClick = { email, password ->
                        loginViewModel.login(email, password)
                    },
                    onRegisterClick = { email, password, fullName ->
                        loginViewModel.register(email, password, fullName)
                    },
                    onOtpClick = { otp ->
                        loginViewModel.verifyOtp(otp)
                    },
                    onToggleAuthMode = {
                        loginViewModel.toggleAuthMode()
                    },
                    onGoogleSignInClick = {
                        signInWithGoogle()
                    },
                    onFingerprintClick = {
                        authenticateWithFingerprint()
                    },
                    onGuestModeClick = {
                        navigateToMainScreen()
                    },
                )
            }
        }
    }

    private fun setupObservers() {
        collectFlow(loginViewModel.uiState) { state ->
            handleStateSideEffects(state)
        }

        collectFlow(loginViewModel.uiEvent) { event ->
            handleUiEvent(event)
        }
    }

    private fun handleStateSideEffects(state: LoginUiState) {
        state.error?.let {
            val message = it.ifBlank {
                getString(CoreR.string.login_error_google_failed)
            }
            showAlertDialog(
                title = getString(CoreR.string.login_error_title),
                message = message,
                type = DialogType.ERROR,
            )
            loginViewModel.clearError()
        }
    }

    override fun handleUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.Navigate -> {
                if (event.destination == "Main") {
                    navigateToMainScreen()
                }
            }
            else -> super.handleUiEvent(event)
        }
    }

    private fun signInWithGoogle() {
        if (!loginViewModel.isInternetAvailable()) {
            showAlertDialog(
                title = resources.getString(CoreR.string.no_internet_title),
                message = resources.getString(CoreR.string.no_internet_message),
                type = DialogType.WARNING,
            )
            return
        }

        val serverClientId = "YOUR_GOOGLE_WEB_CLIENT_ID"
        if (serverClientId.startsWith("YOUR_GOOGLE_WEB_CLIENT_ID")) {
            showDevMockDialog()
            return
        }

        executeGoogleSignIn(serverClientId)
    }

    private fun showDevMockDialog() {
        showAlertDialog(
            title = getString(CoreR.string.login_dev_mode_title),
            message = getString(CoreR.string.login_dev_mode_message),
            type = DialogType.CONFIRMATION,
            positiveButtonText = getString(CoreR.string.login_dev_mode_positive),
            negativeButtonText = getString(CoreR.string.cancel_button_text),
            onDismiss = {
                loginViewModel.googleLogin("mock-google-token")
            },
        )
    }

    private fun executeGoogleSignIn(serverClientId: String) {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity
                )
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    loginViewModel.googleLogin(googleIdTokenCredential.idToken)
                } else {
                    showAlertDialog(
                        title = getString(CoreR.string.login_error_title),
                        message = getString(CoreR.string.login_error_unexpected_credential),
                        type = DialogType.ERROR,
                    )
                }
            } catch (e: GetCredentialCancellationException) {
                Timber.d(e, "User cancelled Google Sign-In prompt")
            } catch (e: NoCredentialException) {
                Timber.w(e, "No Google credential found on device, offering dev mock login")
                showDevMockDialog()
            } catch (e: GetCredentialException) {
                Timber.e(e, "Google Sign-In failed")
                showAlertDialog(
                    title = getString(CoreR.string.login_error_title),
                    message = e.localizedMessage ?: getString(CoreR.string.login_error_google_failed),
                    type = DialogType.ERROR,
                )
            }
        }
    }

    private fun authenticateWithFingerprint() {
        val biometricPrompt = BiometricPromptUtils.createBiometricPrompt(
            activity = this,
            processSuccess = {
                loginViewModel.onBiometricUnlockSuccess()
            }
        )
        val promptInfo = BiometricPromptUtils.createPromptInfo(
            title = getString(CoreR.string.prompt_info_title),
            description = getString(CoreR.string.prompt_info_description),
            negativeButtonText = getString(CoreR.string.cancel_button_text)
        )
        biometricPrompt.authenticate(promptInfo)
    }

    private fun navigateToMainScreen() {
        val intent = Intent().apply {
            setClassName(this@LoginActivity.packageName, "com.thanh0x.coursedeals.MainActivity")
        }
        startActivity(intent)
        finish()
    }
}
