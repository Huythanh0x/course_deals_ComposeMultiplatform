package com.thanh0x.coursedeals.feature.auth

import androidx.biometric.BiometricPrompt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.usecase.LoginUseCase
import com.thanh0x.coursedeals.domain.user.usecase.CryptographyManagerUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SettingFingerprintUseCase
import com.thanh0x.coursedeals.domain.user.usecase.ClearLocalTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.RequestAccessTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SaveJwtTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SettingUserProfileUseCase
import com.thanh0x.coursedeals.core.ui.UiEvent
import com.thanh0x.coursedeals.core.ui.util.NetworkUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.crypto.Cipher
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val saveJwtTokenUseCase: SaveJwtTokenUseCase,
    private val settingFingerprintUseCase: SettingFingerprintUseCase,
    private val cryptographyManagerUseCase: CryptographyManagerUseCase,
    private val clearLocalTokenUseCase: ClearLocalTokenUseCase,
    private val requestAccessTokenUseCase: RequestAccessTokenUseCase,
    private val settingUserProfileUseCase: SettingUserProfileUseCase,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        checkIsAbleLoginByFingerprint()
    }

    fun login(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = loginUseCase(username, password)
            _uiState.update { it.copy(isLoading = false) }

            when (result) {
                is AppResult.Success -> {
                    saveJwtTokenUseCase(result.data.accessToken)
                    _uiEvent.emit(UiEvent.Navigate("Main"))
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                is AppResult.Loading -> { /* Handled by manually setting isLoading */ }
            }
        }
    }

    fun isInternetAvailable(): Boolean {
        return networkUtil.isInternetAvailable()
    }

    fun decryptFingerprintTokenFromStorage(authResult: BiometricPrompt.AuthenticationResult) {
        viewModelScope.launch {
            settingFingerprintUseCase.loadCipherTextWrapperUseCase()?.let { textWrapper ->
                authResult.cryptoObject?.cipher?.let {
                    val localFingerToken =
                        cryptographyManagerUseCase.decryptData(textWrapper.ciphertext, it)
                    requestJwtTokenFromFingerprint(localFingerToken)
                }
            }
        }
    }

    suspend fun generateDecryptCipher(): Cipher? {
        settingFingerprintUseCase.loadCipherTextWrapperUseCase()?.let { textWrapper ->
            return cryptographyManagerUseCase.getInitializedCipherForDecryption(textWrapper.initializationVector)
        }
        return null
    }

    private fun requestJwtTokenFromFingerprint(fingerToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            saveFingerprintToStorage(fingerToken)
            val result = requestAccessTokenUseCase()
            _uiState.update { it.copy(isLoading = false) }
            Timber.d("ACCESS TOKEN RESPONSE: $result")

            if (result is AppResult.Success) {
                clearFingerprintToken()
                saveAccessToken(result.data.accessToken)
                _uiEvent.emit(UiEvent.Navigate("Main"))
            } else {
                Timber.e("ACCESS TOKEN RESPONSE: ERROR")
                _uiEvent.emit(UiEvent.ShowToast("Fingerprint login failed"))
            }
        }
    }

    private suspend fun saveFingerprintToStorage(token: String) {
        saveAccessToken(token)
    }

    private suspend fun saveAccessToken(token: String) {
        saveJwtTokenUseCase(token)
    }

    private suspend fun clearFingerprintToken() {
        clearLocalTokenUseCase()
    }

    private fun checkIsAbleLoginByFingerprint() {
        viewModelScope.launch {
            val isEnabled = settingUserProfileUseCase.loadFingerprintUseCase() ?: false
            _uiState.update { it.copy(isFingerprintEnabled = isEnabled) }
        }
    }
}
