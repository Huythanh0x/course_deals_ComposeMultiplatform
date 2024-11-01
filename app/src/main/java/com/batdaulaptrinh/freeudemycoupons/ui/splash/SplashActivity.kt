package com.batdaulaptrinh.freeudemycoupons.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.batdaulaptrinh.freeudemycoupons.MainActivity
import com.batdaulaptrinh.freeudemycoupons.R
import com.batdaulaptrinh.freeudemycoupons.databinding.ActivitySplashBinding
import com.batdaulaptrinh.freeudemycoupons.util.Constant
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Long.max

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    lateinit var binding: ActivitySplashBinding
    private val startedTime = System.currentTimeMillis()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        lifecycleScope.launch {
            val waitingTime = max(
                Constant.SPLASH_LOADING_TIME_MS - (System.currentTimeMillis() - startedTime), 0L
            )
            delay(waitingTime)
            navigateToHomeScreen()
        }
    }

    private fun navigateToHomeScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
