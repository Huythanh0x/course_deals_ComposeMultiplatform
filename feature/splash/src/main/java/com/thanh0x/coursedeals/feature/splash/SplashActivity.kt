package com.thanh0x.coursedeals.feature.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.thanh0x.coursedeals.core.ui.R as CoreR
import com.thanh0x.coursedeals.feature.splash.R
import com.thanh0x.coursedeals.feature.splash.databinding.ActivitySplashBinding
import com.thanh0x.coursedeals.core.ui.BaseActivity
import com.thanh0x.coursedeals.core.common.Constant
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Long.max
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    lateinit var binding: ActivitySplashBinding
    private val startedTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        lifecycleScope.launch {
            val waitingTime = max(
                Constant.SPLASH_LOADING_TIME_MS - (System.currentTimeMillis() - startedTime), 0L
            )
            delay(waitingTime.milliseconds)
            navigateToHomeScreen()
        }
    }

    private fun navigateToHomeScreen() {
        val intent = Intent().apply {
            setClassName(this@SplashActivity.packageName, "com.thanh0x.coursedeals.feature.auth.LoginActivity")
        }
        startActivity(intent)
        finish()
    }
}
