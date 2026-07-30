package com.thanh0x.coursedeals.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.thanh0x.coursedeals.MainActivity
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.databinding.ActivityLoginBinding
import com.thanh0x.coursedeals.ui.base.BaseActivity
import com.thanh0x.coursedeals.ui.base.UiEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity() {
    lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnGoogle.setOnClickListener {
            handleSocialLogin("Google")
        }

        binding.btnApple.setOnClickListener {
            handleSocialLogin("Apple")
        }

        binding.btnFingerprint.setOnClickListener {
            handleSocialLogin("Fingerprint")
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
            showAlertDialog(getString(R.string.login_error_title), it)
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

    private fun handleSocialLogin(provider: String) {
        if (loginViewModel.isInternetAvailable()) {
            Toast.makeText(this, "Continue with $provider", Toast.LENGTH_SHORT).show()
            navigateToMainScreen()
        } else {
            showAlertDialog(
                resources.getString(R.string.no_internet_title),
                resources.getString(R.string.no_internet_message)
            )
        }
    }

    private fun navigateToMainScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
