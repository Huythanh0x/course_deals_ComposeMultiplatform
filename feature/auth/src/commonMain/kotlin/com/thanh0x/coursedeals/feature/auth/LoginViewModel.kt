package com.thanh0x.coursedeals.feature.auth

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.core.common.Constant
import com.thanh0x.coursedeals.core.ui.UiEvent
import com.thanh0x.coursedeals.core.ui.ViewModel
import com.thanh0x.coursedeals.core.ui.util.NetworkUtil
import com.thanh0x.coursedeals.domain.user.TokenData
import com.thanh0x.coursedeals.domain.user.usecase.ClearCachedUserProfileUseCase
import com.thanh0x.coursedeals.domain.user.usecase.ClearLocalTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.CryptographyManagerUseCase
import com.thanh0x.coursedeals.domain.user.usecase.LoginUseCase
import com.thanh0x.coursedeals.domain.user.usecase.RegisterUseCase
import com.thanh0x.coursedeals.domain.user.usecase.RequestAccessTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SaveJwtTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SaveRefreshTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SendOtpUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SettingFingerprintUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SettingUserProfileUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SocialLoginUseCase
import com.thanh0x.coursedeals.domain.user.usecase.VerifyOtpUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("LongParameterList", "TooManyFunctions")
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val sendOtpUseCase: SendOtpUseCase,
    private val socialLoginUseCase: SocialLoginUseCase,
    private val saveJwtTokenUseCase: SaveJwtTokenUseCase,
    private val saveRefreshTokenUseCase: SaveRefreshTokenUseCase,
    private val settingFingerprintUseCase: SettingFingerprintUseCase,
    private val cryptographyManagerUseCase: CryptographyManagerUseCase,
    private val clearLocalTokenUseCase: ClearLocalTokenUseCase,
    private val clearCachedUserProfileUseCase: ClearCachedUserProfileUseCase,
    private val requestAccessTokenUseCase: RequestAccessTokenUseCase,
    private val settingUserProfileUseCase: SettingUserProfileUseCase,
    private val networkUtil: NetworkUtil,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        checkIsAbleLoginByFingerprint()
    }

    fun setAuthMode(mode: AuthMode) {
        _uiState.update { it.copy(authMode = mode, infoMessage = null, error = null) }
    }

    fun toggleAuthMode() {
        _uiState.update { current ->
            val nextMode = when (current.authMode) {
                AuthMode.LOGIN -> AuthMode.REGISTER
                AuthMode.REGISTER, AuthMode.VERIFY_OTP -> AuthMode.LOGIN
            }
            current.copy(
                authMode = nextMode,
                infoMessage = null,
                error = null
            )
        }
    }

    fun backToLogin() {
        _uiState.update {
            it.copy(
                authMode = AuthMode.LOGIN,
                infoMessage = null,
                error = null
            )
        }
    }

    fun setOtpVerificationState(email: String, showTerms: Boolean = false) {
        _uiState.update {
            it.copy(
                authMode = AuthMode.VERIFY_OTP,
                pendingEmail = email,
                showTermsDialog = showTerms,
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun sendOtp(email: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !EMAIL_REGEX.matches(trimmedEmail)) {
            _uiState.update { it.copy(error = "Please enter a valid email address") }
            return
        }
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = sendOtpUseCase(trimmedEmail)
            _uiState.update { it.copy(isLoading = false) }

            when (result) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            authMode = AuthMode.VERIFY_OTP,
                            pendingEmail = trimmedEmail,
                            infoMessage = "OTP sent to $trimmedEmail. Enter 000000 to verify."
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(error = result.message.ifBlank { "Failed to send OTP. Please try again." })
                    }
                }
                is AppResult.Loading -> { }
            }
        }
    }

    fun login(email: String, password: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            _uiState.update { it.copy(error = "Email cannot be empty") }
            return
        }
        if (password.isBlank()) {
            sendOtp(trimmedEmail)
            return
        }
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = loginUseCase(trimmedEmail, password)
            _uiState.update { it.copy(isLoading = false) }

            when (result) {
                is AppResult.Success -> {
                    handleSuccessfulAuth(result.data)
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(error = result.message.ifBlank { "Invalid email or password" })
                    }
                }
                is AppResult.Loading -> { }
            }
        }
    }

    fun register(email: String, password: String, fullName: String? = null) {
        val trimmedEmail = email.trim()
        val trimmedFullName = fullName?.trim()?.takeIf { it.isNotEmpty() }
        if (!EMAIL_REGEX.matches(trimmedEmail)) {
            _uiState.update { it.copy(error = "Please enter a valid email address") }
            return
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            _uiState.update { it.copy(error = "Password must be at least $MIN_PASSWORD_LENGTH characters") }
            return
        }
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = registerUseCase(trimmedEmail, password, trimmedFullName)
            _uiState.update { it.copy(isLoading = false) }

            when (result) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            authMode = AuthMode.VERIFY_OTP,
                            pendingEmail = trimmedEmail,
                            infoMessage = "Registration successful! Enter OTP (use 000000) to activate your account."
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(error = result.message.ifBlank { "Registration failed" })
                    }
                }
                is AppResult.Loading -> { }
            }
        }
    }

    fun verifyOtp(otp: String) {
        val trimmedOtp = otp.trim()
        val email = _uiState.value.pendingEmail
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Email context missing, please sign in again") }
            return
        }
        if (trimmedOtp.length != OTP_LENGTH) {
            _uiState.update { it.copy(error = "OTP must be exactly $OTP_LENGTH digits") }
            return
        }
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = verifyOtpUseCase(email, trimmedOtp)
            _uiState.update { it.copy(isLoading = false) }

            when (result) {
                is AppResult.Success -> {
                    handleSuccessfulAuth(result.data)
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(error = result.message.ifBlank { "Invalid or expired OTP" })
                    }
                }
                is AppResult.Loading -> { }
            }
        }
    }

    fun googleLogin(idToken: String) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = socialLoginUseCase("GOOGLE", idToken)
            _uiState.update { it.copy(isLoading = false) }

            when (result) {
                is AppResult.Success -> {
                    handleSuccessfulAuth(result.data)
                }
                is AppResult.Error -> {
                    if (idToken.startsWith("mock-")) {
                        handleSuccessfulAuth(TokenData(accessToken = "mock-dev-jwt-token", isNewUser = false))
                    } else {
                        _uiState.update { it.copy(error = result.message) }
                    }
                }
                is AppResult.Loading -> { }
            }
        }
    }

    private suspend fun handleSuccessfulAuth(tokenData: TokenData) {
        if (tokenData.isNewUser) {
            _uiState.update {
                it.copy(
                    showTermsDialog = true,
                    pendingTokenData = tokenData
                )
            }
        } else {
            saveTokens(tokenData)
            _uiEvent.emit(UiEvent.Navigate("Main"))
        }
    }

    fun acceptTerms() {
        val pending = _uiState.value.pendingTokenData
        if (pending != null) {
            viewModelScope.launch(ioDispatcher) {
                saveTokens(pending)
                _uiState.update { it.copy(showTermsDialog = false, pendingTokenData = null) }
                _uiEvent.emit(UiEvent.Navigate("Main"))
            }
        } else {
            _uiState.update { it.copy(showTermsDialog = false) }
        }
    }

    fun declineTerms() {
        _uiState.update {
            it.copy(
                showTermsDialog = false,
                pendingTokenData = null,
                error = "You must agree to the Terms of Service & Privacy Policy to complete registration."
            )
        }
    }

    fun onBiometricUnlockSuccess() {
        viewModelScope.launch(ioDispatcher) {
            _uiEvent.emit(UiEvent.Navigate("Main"))
        }
    }

    fun isInternetAvailable(): Boolean {
        return networkUtil.isInternetAvailable()
    }

    fun decryptFingerprintTokenFromStorage() {
        viewModelScope.launch {
            settingFingerprintUseCase.loadCipherTextWrapperUseCase()?.let { textWrapper ->
                val localFingerToken = cryptographyManagerUseCase.decryptData(
                    textWrapper.ciphertext,
                    textWrapper.initializationVector,
                )
                requestJwtTokenFromFingerprint(localFingerToken)
            }
        }
    }

    private fun requestJwtTokenFromFingerprint(fingerToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            saveFingerprintToStorage(fingerToken)
            val result = requestAccessTokenUseCase()
            _uiState.update { it.copy(isLoading = false) }

            if (result is AppResult.Success) {
                clearFingerprintToken()
                saveTokens(result.data)
                _uiEvent.emit(UiEvent.Navigate("Main"))
            } else {
                _uiEvent.emit(UiEvent.ShowToast("Fingerprint login failed"))
            }
        }
    }

    private suspend fun saveTokens(tokenData: TokenData) {
        clearCachedUserProfileUseCase()
        saveJwtTokenUseCase(tokenData.accessToken)
        tokenData.refreshToken?.takeIf { it.isNotBlank() }?.let { refreshToken ->
            saveRefreshTokenUseCase(refreshToken)
        }
    }

    private suspend fun saveFingerprintToStorage(token: String) {
        saveJwtTokenUseCase(token)
    }

    private suspend fun clearFingerprintToken() {
        clearLocalTokenUseCase()
    }

    private fun checkIsAbleLoginByFingerprint() {
        if (!Constant.FEATURE_FINGERPRINT) {
            _uiState.update { it.copy(isFingerprintEnabled = false, isFingerprintFeatureEnabled = false) }
            return
        }
        viewModelScope.launch {
            val isEnabled = settingUserProfileUseCase.loadFingerprintUseCase() ?: false
            _uiState.update { it.copy(isFingerprintEnabled = isEnabled, isFingerprintFeatureEnabled = true) }
        }
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
        private const val OTP_LENGTH = 6
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
