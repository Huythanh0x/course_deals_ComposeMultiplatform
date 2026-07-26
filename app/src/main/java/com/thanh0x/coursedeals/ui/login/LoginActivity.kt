package com.thanh0x.coursedeals.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.thanh0x.coursedeals.MainActivity
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.data.model.TokenResponseData
import com.thanh0x.coursedeals.databinding.ActivityLoginBinding
import com.thanh0x.coursedeals.util.NetWorkResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
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

        loginViewModel.loginResponseResult.observe(this) { networkResult ->
            handleNetworkResult(networkResult)
        }
    }

    private fun handleSocialLogin(provider: String) {
        if (loginViewModel.isInternetAvailable()) {
            Toast.makeText(this, "Continue with $provider", Toast.LENGTH_SHORT).show()
            // Placeholder: Navigate to main for demo purposes if needed, 
            // or trigger actual social auth flow.
            navigateToMainScreen()
        } else {
            showLoginDialog(
                resources.getString(R.string.no_internet_title),
                resources.getString(R.string.no_internet_message)
            )
        }
    }

    private fun handleNetworkResult(networkResult: NetWorkResult<TokenResponseData>) {
        if (networkResult is NetWorkResult.Success) {
            hideLoadingLoginProgressbar()
            navigateToMainScreen()
        } else if (networkResult is NetWorkResult.Loading) {
            showLoadingLoginProgressbar()
        } else if (networkResult is NetWorkResult.Error) {
            hideLoadingLoginProgressbar()
            showLoginDialog(
                resources.getString(R.string.login_error_title), networkResult.message.toString()
            )
        }
    }

    private fun showLoginDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        ).setTitle(title).setMessage(message)
            .setPositiveButton(R.string.ok_text_button) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun hideLoadingLoginProgressbar() {
        binding.pbLogin.visibility = View.GONE
    }

    private fun showLoadingLoginProgressbar() {
        binding.pbLogin.visibility = View.VISIBLE
    }

    private fun navigateToMainScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
