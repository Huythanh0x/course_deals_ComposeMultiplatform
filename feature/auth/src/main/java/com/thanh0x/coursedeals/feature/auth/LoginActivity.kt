package com.thanh0x.coursedeals.feature.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.thanh0x.coursedeals.core.ui.BaseActivity
import com.thanh0x.coursedeals.core.ui.BiometricPromptUtils
import com.thanh0x.coursedeals.core.ui.DialogType
import com.thanh0x.coursedeals.core.ui.R as CoreR
import com.thanh0x.coursedeals.core.ui.UiEvent
import com.thanh0x.coursedeals.feature.auth.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class LoginActivity : BaseActivity() {
    lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    private val credentialManager by lazy { CredentialManager.create(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnEmailSubmit.setOnClickListener {
            onEmailSubmitClicked()
        }

        binding.btnToggleAuthMode.setOnClickListener {
            loginViewModel.toggleAuthMode()
        }

        binding.btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

        binding.btnFingerprint.setOnClickListener {
            authenticateWithFingerprint()
        }

        binding.btnAccessAsGuest.setOnClickListener {
            navigateToMainScreen()
        }
    }

    private fun onEmailSubmitClicked() {
        when (loginViewModel.uiState.value.authMode) {
            AuthMode.LOGIN -> {
                val email = binding.etEmail.text?.toString().orEmpty()
                val password = binding.etPassword.text?.toString().orEmpty()
                loginViewModel.login(email, password)
            }
            AuthMode.REGISTER -> {
                val email = binding.etEmail.text?.toString().orEmpty()
                val password = binding.etPassword.text?.toString().orEmpty()
                val fullName = binding.etFullName.text?.toString().orEmpty()
                loginViewModel.register(email, password, fullName)
            }
            AuthMode.VERIFY_OTP -> {
                val otp = binding.etOtp.text?.toString().orEmpty()
                loginViewModel.verifyOtp(otp)
            }
        }
    }

    private fun setupObservers() {
        collectFlow(loginViewModel.uiState) { state ->
            handleUiState(state)
        }

        collectFlow(loginViewModel.uiEvent) { event ->
            handleUiEvent(event)
        }
    }

    private fun handleUiState(state: LoginUiState) {
        if (state.isLoading) {
            showLoading()
        } else {
            hideLoading()
        }

        state.error?.let {
            showAlertDialog(
                title = getString(CoreR.string.login_error_title),
                message = it,
                type = DialogType.ERROR,
            )
            loginViewModel.clearError()
        }

        binding.tvInfoMessage.isVisible = !state.infoMessage.isNullOrBlank()
        binding.tvInfoMessage.text = state.infoMessage.orEmpty()

        when (state.authMode) {
            AuthMode.LOGIN -> {
                binding.tilFullName.isVisible = false
                binding.tilEmail.isVisible = true
                binding.tilPassword.isVisible = true
                binding.tilOtp.isVisible = false
                binding.btnEmailSubmit.text = getString(CoreR.string.auth_btn_sign_in)
                binding.btnToggleAuthMode.text = getString(CoreR.string.auth_toggle_to_register)
            }
            AuthMode.REGISTER -> {
                binding.tilFullName.isVisible = true
                binding.tilEmail.isVisible = true
                binding.tilPassword.isVisible = true
                binding.tilOtp.isVisible = false
                binding.btnEmailSubmit.text = getString(CoreR.string.auth_btn_create_account)
                binding.btnToggleAuthMode.text = getString(CoreR.string.auth_toggle_to_login)
            }
            AuthMode.VERIFY_OTP -> {
                binding.tilFullName.isVisible = false
                binding.tilEmail.isVisible = false
                binding.tilPassword.isVisible = false
                binding.tilOtp.isVisible = true
                binding.btnEmailSubmit.text = getString(CoreR.string.auth_btn_verify_otp)
                binding.btnToggleAuthMode.text = getString(CoreR.string.auth_toggle_back_to_login)
            }
        }

        binding.btnFingerprint.isEnabled = state.isFingerprintEnabled
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

        val serverClientId = getString(R.string.google_web_client_id)
        if (serverClientId.startsWith("YOUR_GOOGLE_WEB_CLIENT_ID")) {
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
            return
        }

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
                // User cancelled the prompt - no action required
            } catch (e: NoCredentialException) {
                Timber.w("No Google credential found on device, offering dev mock login")
                showAlertDialog(
                    title = getString(CoreR.string.login_dev_mode_title),
                    message = "No Google account is configured on this device. Would you like to proceed with Dev Mock Login?",
                    type = DialogType.CONFIRMATION,
                    positiveButtonText = getString(CoreR.string.login_dev_mode_positive),
                    negativeButtonText = getString(CoreR.string.cancel_button_text),
                    onDismiss = {
                        loginViewModel.googleLogin("mock-google-token")
                    },
                )
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
