package com.thanh0x.coursedeals.shared

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.thanh0x.coursedeals.core.common.AppVersion
import com.thanh0x.coursedeals.core.network.di.coreNetworkModule
import com.thanh0x.coursedeals.core.ui.DialogType
import com.thanh0x.coursedeals.core.ui.UiEvent
import com.thanh0x.coursedeals.core.ui.di.coreUiModule
import com.thanh0x.coursedeals.core.ui.koinViewModel
import com.thanh0x.coursedeals.core.ui.theme.CourseDealsTheme
import com.thanh0x.coursedeals.core.ui.util.PlatformActions
import com.thanh0x.coursedeals.data.coupons.di.couponDataModule
import com.thanh0x.coursedeals.data.user.di.userDataModule
import com.thanh0x.coursedeals.domain.coupons.di.couponDomainModule
import com.thanh0x.coursedeals.domain.user.FeatureFlagManager
import com.thanh0x.coursedeals.domain.user.UserProfileRepository
import com.thanh0x.coursedeals.domain.user.di.userDomainModule
import com.thanh0x.coursedeals.domain.user.model.AppConfig
import com.thanh0x.coursedeals.feature.auth.AuthMode
import com.thanh0x.coursedeals.feature.auth.LoginScreen
import com.thanh0x.coursedeals.feature.auth.LoginViewModel
import com.thanh0x.coursedeals.feature.auth.OtpVerificationScreen
import com.thanh0x.coursedeals.feature.auth.di.authModule
import com.thanh0x.coursedeals.feature.course.CourseViewModel
import com.thanh0x.coursedeals.feature.course.di.courseModule
import com.thanh0x.coursedeals.feature.detail.CouponDetailRoute
import com.thanh0x.coursedeals.feature.detail.CouponDetailViewModel
import com.thanh0x.coursedeals.feature.detail.di.detailModule
import com.thanh0x.coursedeals.feature.enroll.CouponEnrollRoute
import com.thanh0x.coursedeals.feature.home.HomeViewModel
import com.thanh0x.coursedeals.feature.home.di.homeModule
import com.thanh0x.coursedeals.feature.profile.ProfileViewModel
import com.thanh0x.coursedeals.feature.profile.di.profileModule
import com.thanh0x.coursedeals.feature.splash.SplashNavigationTarget
import com.thanh0x.coursedeals.feature.splash.SplashScreen
import com.thanh0x.coursedeals.feature.splash.SplashUiState
import com.thanh0x.coursedeals.feature.splash.SplashViewModel
import com.thanh0x.coursedeals.feature.splash.di.splashModule
import com.thanh0x.coursedeals.navigation.MainAppScaffold
import com.thanh0x.coursedeals.navigation.TopLevelDestination
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
    splashModule,
)

fun initSharedKoin() {
    if (KoinPlatformTools.defaultContext().getOrNull() == null) {
        startKoin {
            modules(sharedKoinModules)
        }
    }
}

sealed interface AppScreen {
    data object Splash : AppScreen
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
fun App(
    initialScreen: AppScreen = AppScreen.Login,
    initialRoute: String = "home",
    initialAuthMode: AuthMode = AuthMode.LOGIN,
    initialPendingEmail: String = "",
    initialShowTermsDialog: Boolean = false,
) {
    remember {
        initSharedKoin()
        true
    }

    var darkThemeOverride by remember { mutableStateOf<Boolean?>(null) }
    var currentScreen by remember { mutableStateOf<AppScreen>(initialScreen) }
    var dialogState by remember { mutableStateOf<DialogState?>(null) }
    var sessionEpoch by remember { mutableIntStateOf(0) }

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

    val featureFlagManager = remember {
        KoinPlatformTools.defaultContext().getOrNull()?.getOrNull<FeatureFlagManager>()
    }
    val appConfig = featureFlagManager?.configFlow?.collectAsStateWithLifecycle()?.value ?: AppConfig()
    val isGlobalForceUpdate = AppVersion.currentVersionCode() < appConfig.minSupportedVersionCode

    CourseDealsTheme(
        darkTheme = darkThemeOverride ?: false,
        dynamicColor = false,
    ) {
        if (isGlobalForceUpdate && currentScreen !is AppScreen.Splash) {
            SplashScreen(
                uiState = SplashUiState(
                    isLoading = false,
                    isForceUpdateRequired = true,
                    forceUpdateUrl = appConfig.forceUpdateUrl,
                ),
            )
            return@CourseDealsTheme
        }

        val isInMainSession = currentScreen is AppScreen.Main ||
            currentScreen is AppScreen.Detail ||
            currentScreen is AppScreen.Enroll

        if (isInMainSession) {
            var currentMainRoute by rememberSaveable(sessionEpoch) {
                mutableStateOf(if (sessionEpoch == 0) initialRoute else TopLevelDestination.HOME.route)
            }
            val homeViewModel: HomeViewModel = koinViewModel(key = "home_vm_$sessionEpoch")
            val courseViewModel: CourseViewModel = koinViewModel(key = "course_vm_$sessionEpoch")
            val profileViewModel: ProfileViewModel = koinViewModel(key = "profile_vm_$sessionEpoch")
            val homePagingItems = homeViewModel.items.collectAsLazyPagingItems()
            val homeGridState = remember(sessionEpoch) { LazyGridState() }

            DisposableEffect(homeViewModel, courseViewModel, profileViewModel) {
                onDispose {
                    homeViewModel.onCleared()
                    courseViewModel.onCleared()
                    profileViewModel.onCleared()
                }
            }

            when (val screen = currentScreen) {
                is AppScreen.Main -> {
                    MainAppScaffold(
                        initialRoute = initialRoute,
                        currentRouteOverride = currentMainRoute,
                        onRouteChange = { route ->
                            currentMainRoute = route
                        },
                        homeViewModel = homeViewModel,
                        courseViewModel = courseViewModel,
                        profileViewModel = profileViewModel,
                        homePagingItems = homePagingItems,
                        homeGridState = homeGridState,
                        onCouponClick = { coupon ->
                            currentScreen = AppScreen.Detail(coupon.courseId)
                        },
                        onNavigateToLogin = {
                            sessionEpoch++
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

                else -> {}
            }
        } else {
            when (currentScreen) {
                is AppScreen.Splash -> {
                    val splashViewModel: SplashViewModel = koinViewModel(key = "splash_vm")
                    val splashUiState by splashViewModel.uiState.collectAsStateWithLifecycle()
                    LaunchedEffect(splashUiState.isForceUpdateRequired, splashUiState.navigationTarget) {
                        if (!splashUiState.isForceUpdateRequired) {
                            when (splashUiState.navigationTarget) {
                                SplashNavigationTarget.MAIN -> currentScreen = AppScreen.Main
                                SplashNavigationTarget.LOGIN -> currentScreen = AppScreen.Login
                                null -> {}
                            }
                        }
                    }
                    SplashScreen(uiState = splashUiState)
                }

                is AppScreen.Login -> {
                    val loginViewModel: LoginViewModel = koinViewModel(key = "login_vm_$sessionEpoch")
                    val loginUiState by loginViewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(Unit) {
                        if (initialAuthMode == AuthMode.VERIFY_OTP) {
                            loginViewModel.setOtpVerificationState(
                                email = initialPendingEmail,
                                showTerms = initialShowTermsDialog,
                            )
                        }
                    }

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
                            loginViewModel.clearError()
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

                else -> {}
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
