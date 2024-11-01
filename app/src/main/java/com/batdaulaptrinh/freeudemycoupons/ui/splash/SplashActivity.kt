package com.batdaulaptrinh.freeudemycoupons.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.batdaulaptrinh.freeudemycoupons.MainActivity
import com.batdaulaptrinh.freeudemycoupons.R
import com.batdaulaptrinh.freeudemycoupons.databinding.ActivitySplashBinding
import com.batdaulaptrinh.freeudemycoupons.ui.login.LoginActivity
import com.batdaulaptrinh.freeudemycoupons.util.Constant
import com.batdaulaptrinh.freeudemycoupons.util.SplashScreenFlag
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.lang.Long.max

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    lateinit var binding: ActivitySplashBinding
    private val splashScreenViewModel: SplashScreenViewModel by viewModels()
    private val startedTime = System.currentTimeMillis()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        splashScreenViewModel.splashScreenFlag.observe(this) {
            runBlocking {
                val waitingTime = max(
                    Constant.SPLASH_LOADING_TIME_MS - (System.currentTimeMillis() - startedTime), 0L
                )
                delay(waitingTime)
            }
            when (it) {
                SplashScreenFlag.EMPTY_TOKEN -> navigateToLoginScreen()
                SplashScreenFlag.EXPIRED_TOKEN -> navigateToLoginScreen()
                SplashScreenFlag.VALID_TOKEN -> navigateToHomeScreen()
                null -> {}
            }
        }
        splashScreenViewModel.isInternetAvailable.observe(this) {
            if (it == true) {
                splashScreenViewModel.checkJwtToken()
            } else {
                showNoInternetDialog()
            }
        }
        splashScreenViewModel.checkIfInternetAvailable()
    }

    private fun showNoInternetDialog() {
        MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        ).setTitle(R.string.no_internet_title).setMessage(R.string.no_internet_message)
            .setPositiveButton(R.string.ok_text_button) { dialog, _ ->
                dialog.dismiss()
                splashScreenViewModel.checkIfInternetAvailable()
            }
            .setCancelable(false)
            .create().show()

    }

    private fun navigateToHomeScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateToLoginScreen() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
