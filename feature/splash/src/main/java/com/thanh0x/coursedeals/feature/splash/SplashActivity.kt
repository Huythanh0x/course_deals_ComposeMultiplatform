package com.thanh0x.coursedeals.feature.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.thanh0x.coursedeals.core.common.Constant
import com.thanh0x.coursedeals.core.ui.BaseActivity
import com.thanh0x.coursedeals.core.ui.theme.CourseDealsTheme
import com.thanh0x.coursedeals.domain.user.usecase.GetLocalTokenUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.lang.Long.max
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    private val startedTime = System.currentTimeMillis()

    private val getLocalTokenUseCase: GetLocalTokenUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CourseDealsTheme {
                SplashScreen()
            }
        }
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
