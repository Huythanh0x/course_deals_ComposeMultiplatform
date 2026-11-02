package com.thanh0x.coursedeals.feature.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thanh0x.coursedeals.BuildConfig
import com.thanh0x.coursedeals.core.common.AppVersion
import com.thanh0x.coursedeals.core.common.Constant
import com.thanh0x.coursedeals.core.ui.BaseActivity
import com.thanh0x.coursedeals.core.ui.theme.CourseDealsTheme
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import java.lang.Long.max
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    private var startedTime = System.currentTimeMillis()
    private var hasNavigated = false

    private val splashViewModel: SplashViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        applyVersionOverrideFromIntent(intent)
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!splashViewModel.uiState.value.isForceUpdateRequired) {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            },
        )

        splashViewModel.evaluateStartupGate()

        setContent {
            CourseDealsTheme {
                val uiState by splashViewModel.uiState.collectAsStateWithLifecycle()
                SplashScreen(uiState = uiState)
            }
        }

        collectFlow(splashViewModel.uiState) { uiState ->
            if (uiState.isForceUpdateRequired || hasNavigated) {
                return@collectFlow
            }
            val target = uiState.navigationTarget ?: return@collectFlow
            hasNavigated = true
            val waitingTime = max(
                Constant.SPLASH_LOADING_TIME_MS - (System.currentTimeMillis() - startedTime),
                0L,
            )
            delay(waitingTime.milliseconds)
            if (splashViewModel.uiState.value.isForceUpdateRequired) {
                hasNavigated = false
                return@collectFlow
            }
            when (target) {
                SplashNavigationTarget.MAIN -> navigateToMainScreen()
                SplashNavigationTarget.LOGIN -> navigateToLoginScreen()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        startedTime = System.currentTimeMillis()
        hasNavigated = false
        applyVersionOverrideFromIntent(intent)
        splashViewModel.evaluateStartupGate()
    }

    private fun applyVersionOverrideFromIntent(incomingIntent: Intent?) {
        if (!BuildConfig.DEBUG) return
        val intExtra = incomingIntent?.getIntExtra(EXTRA_OVERRIDE_VERSION_CODE, -1) ?: -1
        val parsedOverride = if (intExtra in 1..MAX_VALID_VERSION_CODE) {
            intExtra
        } else {
            incomingIntent?.getStringExtra(EXTRA_OVERRIDE_VERSION_CODE)
                ?.toIntOrNull()
                ?.takeIf { it in 1..MAX_VALID_VERSION_CODE }
        }
        AppVersion.overrideVersionCode = parsedOverride
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
            setClassName(
                this@SplashActivity.packageName,
                "com.thanh0x.coursedeals.feature.auth.LoginActivity",
            )
        }
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_OVERRIDE_VERSION_CODE = "override_version_code"
        private const val MAX_VALID_VERSION_CODE = 10_000
    }
}
