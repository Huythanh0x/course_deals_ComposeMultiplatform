package com.thanh0x.coursedeals.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.thanh0x.coursedeals.MainActivity
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.databinding.ActivityLoginBinding
import com.thanh0x.coursedeals.domain.model.AppResult
import com.thanh0x.coursedeals.domain.model.TokenData
import com.thanh0x.coursedeals.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity() {
    lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGoogle.setOnClickListener {
            handleSocialLogin("Google")
        }

        binding.btnApple.setOnClickListener {
            handleSocialLogin("Apple")
        }

        binding.btnFingerprint.setOnClickListener {
            handleSocialLogin("Fingerprint")
        }

        loginViewModel.loginResponseResult.observe(this) { result ->
            handleLoginResult(result)
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

    private fun handleLoginResult(result: AppResult<TokenData>) {
        when (result) {
            is AppResult.Success -> {
                hideLoading()
                navigateToMainScreen()
            }
            is AppResult.Loading -> {
                showLoading()
            }
            is AppResult.Error -> {
                hideLoading()
                showAlertDialog(
                    resources.getString(R.string.login_error_title),
                    result.message
                )
            }
        }
    }

    private fun navigateToMainScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
