package com.thanh0x.coursedeals.feature.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.thanh0x.coursedeals.core.ui.BaseActivity
import com.thanh0x.coursedeals.core.ui.BiometricPromptUtils
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
        binding.btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

        binding.btnFingerprint.setOnClickListener {
            authenticateWithFingerprint()
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
            showAlertDialog(getString(CoreR.string.login_error_title), it)
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
                resources.getString(CoreR.string.no_internet_title),
                resources.getString(CoreR.string.no_internet_message)
            )
            return
        }

        val serverClientId = getString(CoreR.string.google_web_client_id)
        if (serverClientId.startsWith("YOUR_GOOGLE_WEB_CLIENT_ID")) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Google Sign-In (Dev Mode)")
                .setMessage("Google Web Client ID is not configured yet. Sign in with a Mock Google Developer account for local testing?")
                .setPositiveButton("Sign In") { _, _ ->
                    loginViewModel.googleLogin("mock-google-token")
                }
                .setNegativeButton(getString(CoreR.string.cancel_button_text), null)
                .show()
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
                        getString(CoreR.string.login_error_title),
                        "Unexpected credential received from Google."
                    )
                }
            } catch (e: GetCredentialCancellationException) {
                // User cancelled the prompt - no action required
            } catch (e: GetCredentialException) {
                Timber.e(e, "Google Sign-In failed")
                showAlertDialog(
                    getString(CoreR.string.login_error_title),
                    e.localizedMessage ?: "Failed to sign in with Google."
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
