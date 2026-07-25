package com.thanh0x.coursedeals.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.thanh0x.coursedeals.MainActivity
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.data.model.TokenResponseData
import com.thanh0x.coursedeals.databinding.ActivityLoginBinding
import com.thanh0x.coursedeals.util.NetWorkResult
import com.thanh0x.coursedeals.util.ValidationHelper
import com.thanh0x.coursedeals.domain.logic.fingerprint.BiometricPromptUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnLogin.setOnClickListener {
            if (loginViewModel.isInternetAvailable()) {
                loginWithCurrentInputData()
            } else {
                showLoginDialog(
                    resources.getString(R.string.no_internet_title),
                    resources.getString(R.string.no_internet_message)
                )
            }
        }
        loginViewModel.isLoginByFingerprintEnable.observe(this) {
            binding.btnLoginWithFingerprint.isVisible = it ?: false
        }
        loginViewModel.loginResponseResult.observe(this) { networkResult ->
            handleNetworkResult(networkResult)
        }
        loginViewModel.decryptionResult.observe(this) {
            Log.d("LOAD DECRYPTED TOKEN", it)
            if (it.isNotEmpty()) {
                loginViewModel.requestJwtTokenFromFingerprint(it)
            } else {
                Log.e("LOAD DECRYPTED TOKEN", "error")
            }
        }
        loginViewModel.newAccessTokenFromFingerprint.observe(this) {
            if (it.isNotEmpty()) {
                lifecycleScope.launch {
                    loginViewModel.saveAccessToken(it)
                }
                navigateToMainScreen()
            } else {
                Log.e("NEW ACCESS TOKEN", "error")
            }
        }
        val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            binding.btnLoginWithFingerprint.setOnClickListener {
                if (loginViewModel.isInternetAvailable()) {
                    showBiometricPromptForDecryption()
                } else {
                    showLoginDialog(
                        resources.getString(R.string.no_internet_title),
                        resources.getString(R.string.no_internet_message)
                    )
                }
            }
        } else {
            binding.btnLoginWithFingerprint.isVisible = false
        }
    }

    private fun loginWithCurrentInputData() {
        val email = binding.edtLoginEmail.text.toString()
        val password = binding.edtLoginPassword.text.toString()
        if (ValidationHelper.isEmailPasswordEmpty(email, password)) {
            toastMessage(R.string.empty_email_password_warning_text)
        } else if (!ValidationHelper.isEmailAddressValidFormat(email)) {
            toastMessage(R.string.invalid_email_format_warning_text)
        } else {
            loginViewModel.login(email, password)
        }
    }

    private fun handleNetworkResult(networkResult: NetWorkResult<TokenResponseData>) {
        if (networkResult is NetWorkResult.Success) {
            hideLoadingLoginProgressbar()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else if (networkResult is NetWorkResult.Loading) {
            showLoadingLoginProgressbar()
        } else if (networkResult is NetWorkResult.Error) {
            hideLoadingLoginProgressbar()
            showLoginDialog(
                resources.getString(R.string.login_error_title), networkResult.message.toString()
            )
        }
    }

    private fun toastMessage(@StringRes resId: Int) {
        Toast.makeText(
            this, resources.getString(resId), Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginDialog(title: String, message: String) {
        val loginDialog = MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        ).setTitle(title).setMessage(message)
            .setPositiveButton(R.string.ok_text_button) { dialog, _ ->
                dialog.dismiss()
            }.create()
        loginDialog.show()
    }

    private fun hideLoadingLoginProgressbar() {
        binding.pbLogin.visibility = View.GONE
    }

    private fun showLoadingLoginProgressbar() {
        binding.pbLogin.visibility = View.VISIBLE
    }

    private fun showBiometricPromptForDecryption() {
        lifecycleScope.launch {
            loginViewModel.generateDecryptCipher()?.let { cipher ->
                val biometricPrompt =
                    BiometricPromptUtils.createBiometricPrompt(this@LoginActivity, {
                        loginViewModel.decryptFingerprintTokenFromStorage(it)
                    }) {}
                val promptInfo = BiometricPromptUtils.createPromptInfo(this@LoginActivity)
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
        }
    }

    private fun navigateToMainScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
