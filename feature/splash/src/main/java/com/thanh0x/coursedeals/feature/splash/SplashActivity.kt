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
import com.thanh0x.coursedeals.domain.user.usecase.GetLocalTokenUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Long.max
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    lateinit var binding: ActivitySplashBinding
    private val startedTime = System.currentTimeMillis()

    @Inject
    lateinit var getLocalTokenUseCase: GetLocalTokenUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        lifecycleScope.launch {
            val token = getLocalTokenUseCase()
            val waitingTime = max(
                Constant.SPLASH_LOADING_TIME_MS - (System.currentTimeMillis() - startedTime), 0L
            )
            delay(waitingTime.milliseconds)
            if (!token.isNullOrBlank()) {
                navigateToMainScreen()
            } else {
                navigateToLoginScreen()
            }
        }
    }

    private fun navigateToMainScreen() {
        val intent = Intent().apply {
            setClassName(this@SplashActivity.packageName, "com.thanh0x.coursedeals.MainActivity")
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToLoginScreen() {
        val intent = Intent().apply {
            setClassName(this@SplashActivity.packageName, "com.thanh0x.coursedeals.feature.auth.LoginActivity")
        }
        startActivity(intent)
        finish()
    }
}
