package com.thanh0x.coursedeals.shared

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thanh0x.coursedeals.core.network.di.coreNetworkModule
import com.thanh0x.coursedeals.core.ui.DialogType
import com.thanh0x.coursedeals.core.ui.UiEvent
import com.thanh0x.coursedeals.core.ui.di.coreUiModule
import com.thanh0x.coursedeals.core.ui.koinViewModel
import com.thanh0x.coursedeals.core.ui.theme.CourseDealsTheme
import com.thanh0x.coursedeals. core.ui.util.PlatformActions
import com.thanh0x.coursedeals.data.coupons.di.couponDataModule
import com.thanh0x.coursedeals.data.user.di.userDataModule
import com.thanh0x.coursedeals.domain.coupons.di.couponDomainModule
import com.thanh0x.coursedeals.domain.user.UserProfileRepository
import com.thanh0x.coursedeals.domain.user.di.userDomainModule
import com.thanh0x.coursedeals.feature.auth.AuthMode
import com.thanh0x.coursedeals.feature.auth.LoginScreen
import com.thanh0x.coursedeals.feature.auth.LoginViewModel
import com.thanh0x.coursedeals.feature.auth.OtpVerificationScreen
import com.thanh0x.coursedeals.feature.auth.di.authModule
import com.thanh0x.coursedeals.feature.course.di.courseModule
import com.thanh0x.coursedeals.feature.detail.CouponDetailRoute
import com.thanh0x.coursedeals.feature.detail.CouponDetailViewModel
import com.thanh0x.coursedeals.feature.detail.di.detailModule
import com.thanh0x.coursedeals.feature.enroll.CouponEnrollRoute
import com.thanh0x.coursedeals.feature.home.di.homeModule
import com.thanh0x.coursedeals.feature.profile.di.profileModule
import com.thanh0x.coursedeals.navigation.MainAppScaffold
import kotlinx.coroutines.flow.collectLatest
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools

val sharedKoinModules = listOf(
    coreNetworkModule,
    coreUiModule,
    couponDataModule,
    userDataModule,
    couponDomainModule,
    userDomainModule,
    homeModule,
    courseModule,
    profileModule,
    detailModule,
    authModule,
)

fun initSharedKoin() {
    if (KoinPlatformTools.defaultContext().getOrNull() == null) {
        startKoin {
            modules(sharedKoinModules)
        }
    }
}

sealed interface AppScreen {
    data object Main : AppScreen
    data object Login : AppScreen
    data class Detail(val courseId: Int) : AppScreen
    data class Enroll(val couponUrl: String?, val previousCourseId: Int?) : AppScreen
}

private data class DialogState(
    val title: String,
    val message: String,
    val type: DialogType,
)

@Composable
fun App() {
    remember {
        initSharedKoin()
        true
    }

    var darkThemeOverride by remember { mutableStateOf<Boolean?>(null) }
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Main) }
    var dialogState by remember { mutableStateOf<DialogState?>(null) }

    LaunchedEffect(Unit) {
        PlatformActions.onApplyDarkMode = { enabled ->
            darkThemeOverride = enabled
        }
        PlatformActions.onShowToast = { message ->
            dialogState = DialogState(
                title = "Notice",
                message = message,
                type = DialogType.INFO,
            )
        }
        val repo = KoinPlatformTools.defaultContext().getOrNull()?.getOrNull<UserProfileRepository>()
        val savedDarkMode = repo?.getEnableDarkMode()
        if (savedDarkMode != null && darkThemeOverride == null) {
            darkThemeOverride = savedDarkMode
        }
    }

    val showAlertDialog: (String, String, DialogType) -> Unit = { title, message, type ->
        dialogState = DialogState(title = title, message = message, type = type)
    }

    CourseDealsTheme(
        darkTheme = darkThemeOverride ?: false,
        dynamicColor = false,
    ) {
        when (val screen = currentScreen) {
            is AppScreen.Main -> {
                MainAppScaffold(
                    onCouponClick = { coupon ->
                        val courseId = coupon.courseId
                        if (courseId != null) {
                            currentScreen = AppScreen.Detail(courseId)
                        }
                    },
                    onNavigateToLogin = {
                        currentScreen = AppScreen.Login
                    },
                    showAlertDialog = showAlertDialog,
                )
            }

            is AppScreen.Detail -> {
                val detailViewModel: CouponDetailViewModel = koinViewModel(
                    key = "detail_${screen.courseId}",
                )
                val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(screen.courseId) {
                    detailViewModel.checkIfInternetAvailable()
                    detailViewModel.fetchCouponDetail(screen.courseId)
                }
                LaunchedEffect(detailUiState.error, detailUiState.coupon) {
                    val err = detailUiState.error
                    if (!err.isNullOrBlank() && detailUiState.coupon == null) {
                        showAlertDialog("Error", err, DialogType.ERROR)
                        currentScreen = AppScreen.Main
                    }
                }
                CouponDetailRoute(
                    viewModel = detailViewModel,
                    onBackClick = {
                        currentScreen = AppScreen.Main
                    },
                    onEnrollClick = { couponUrl ->
                        currentScreen = AppScreen.Enroll(
                            couponUrl = couponUrl,
                            previousCourseId = screen.courseId,
                        )
                    },
                    onShareClick = { title, url ->
                        PlatformActions.shareLink(title, url)
                    },
                    onReportSubmitted = { _, _ ->
                        showAlertDialog(
                            "Report Submitted",
                            "Thank you for reporting this deal. Our team will verify it shortly.",
                            DialogType.INFO,
                        )
                    },
                )
            }

            is AppScreen.Enroll -> {
                var isLoading by remember(screen.couponUrl) { mutableStateOf(true) }
                var isError by remember(screen.couponUrl) { mutableStateOf(false) }
                CouponEnrollRoute(
                    couponUrl = screen.couponUrl,
                    isLoading = isLoading,
                    isError = isError,
                    onBackClick = {
                        val prevId = screen.previousCourseId
                        currentScreen = if (prevId != null) {
                            AppScreen.Detail(prevId)
                        } else {
                            AppScreen.Main
                        }
                    },
                    onRetryClick = {
                        isError = false
                        isLoading = true
                    },
                    onPageStarted = {
                        isLoading = true
                    },
                    onPageFinished = {
                        isLoading = false
                    },
                    onPageError = {
                        isLoading = false
                        isError = true
                    },
                )
            }

            is AppScreen.Login -> {
                val loginViewModel: LoginViewModel = koinViewModel(key = "login_vm")
                val loginUiState by loginViewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(loginViewModel) {
                    loginViewModel.uiEvent.collectLatest { event ->
                        when (event) {
                            is UiEvent.Navigate -> {
                                currentScreen = AppScreen.Main
                            }
                            is UiEvent.ShowToast -> {
                                showAlertDialog("Authentication", event.message, DialogType.INFO)
                            }
                            else -> {}
                        }
                    }
                }

                LaunchedEffect(loginUiState.error) {
                    val err = loginUiState.error
                    if (!err.isNullOrBlank()) {
                        showAlertDialog("Authentication Error", err, DialogType.ERROR)
                    }
                }

                when (loginUiState.authMode) {
                    AuthMode.VERIFY_OTP -> {
                        OtpVerificationScreen(
                            uiState = loginUiState,
                            onOtpClick = { otp ->
                                loginViewModel.verifyOtp(otp)
                            },
                            onBackToEmailClick = {
                                loginViewModel.backToLogin()
                            },
                            onResendOtpClick = { email ->
                                loginViewModel.sendOtp(email)
                            },
                            onAcceptTermsClick = {
                                loginViewModel.acceptTerms()
                            },
                            onDeclineTermsClick = {
                                loginViewModel.declineTerms()
                            },
                        )
                    }
                    else -> {
                        LoginScreen(
                            uiState = loginUiState,
                            onSendOtpClick = { email ->
                                loginViewModel.sendOtp(email)
                            },
                            onOtpClick = { otp ->
                                loginViewModel.verifyOtp(otp)
                            },
                            onBackToEmailClick = {
                                loginViewModel.backToLogin()
                            },
                            onAcceptTermsClick = {
                                loginViewModel.acceptTerms()
                            },
                            onDeclineTermsClick = {
                                loginViewModel.declineTerms()
                            },
                            onLoginClick = { email, password ->
                                loginViewModel.login(email, password)
                            },
                            onRegisterClick = { email, password, fullName ->
                                loginViewModel.register(email, password, fullName)
                            },
                            onToggleAuthMode = {
                                loginViewModel.toggleAuthMode()
                            },
                            onGoogleSignInClick = {
                                loginViewModel.googleLogin("mock-google-token")
                            },
                            onFingerprintClick = {
                                loginViewModel.onBiometricUnlockSuccess()
                            },
                            onGuestModeClick = {
                                currentScreen = AppScreen.Main
                            },
                        )
                    }
                }
            }
        }

        val currentDialog = dialogState
        if (currentDialog != null) {
            AlertDialog(
                onDismissRequest = { dialogState = null },
                title = { Text(text = currentDialog.title) },
                text = { Text(text = currentDialog.message) },
                confirmButton = {
                    TextButton(onClick = { dialogState = null }) {
                        Text(text = "OK")
                    }
                },
            )
        }
    }
}
