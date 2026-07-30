package com.thanh0x.coursedeals.ui.core.profile

import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thanh0x.coursedeals.domain.logic.fingerprint.CiphertextWrapper
import com.thanh0x.coursedeals.domain.model.AppResult
import com.thanh0x.coursedeals.domain.model.TokenData
import com.thanh0x.coursedeals.domain.usecase.authentication.fingerprint.CryptographyManagerUseCase
import com.thanh0x.coursedeals.domain.usecase.authentication.fingerprint.RequestFingerprintTokenUseCase
import com.thanh0x.coursedeals.domain.usecase.authentication.fingerprint.SettingFingerprintUseCase
import com.thanh0x.coursedeals.domain.usecase.authentication.jwt.CheckIfTokenExpiredUseCase
import com.thanh0x.coursedeals.domain.usecase.authentication.jwt.ClearLocalTokenUseCase
import com.thanh0x.coursedeals.domain.usecase.authentication.jwt.SaveJwtTokenUseCase
import com.thanh0x.coursedeals.domain.usecase.user_profile.SettingUserProfileUseCase
import com.thanh0x.coursedeals.ui.base.UiEvent
import com.thanh0x.coursedeals.util.NetworkStatusCode
import com.thanh0x.coursedeals.util.NetworkUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.crypto.Cipher
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val settingUserProfileUseCase: SettingUserProfileUseCase,
    val saveJwtTokenUseCase: SaveJwtTokenUseCase,
    private val cryptographyManagerUseCase: CryptographyManagerUseCase,
    private val settingFingerprintUseCase: SettingFingerprintUseCase,
    private val requestFingerprintTokenUseCase: RequestFingerprintTokenUseCase,
    private val clearLocalTokenUseCase: ClearLocalTokenUseCase,
    private val checkIfTokenExpiredUseCase: CheckIfTokenExpiredUseCase,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        initSwitchState()
        checkIfTokenExpired()
    }

    private fun initSwitchState() {
        viewModelScope.launch(Dispatchers.IO) {
            val darkMode = settingUserProfileUseCase.loadDarkModeUseCase() ?: false
            val fingerprint = settingUserProfileUseCase.loadFingerprintUseCase() ?: false
            _uiState.update { it.copy(isDarkModeEnabled = darkMode, isFingerprintEnabled = fingerprint) }
        }
    }

    fun checkIfTokenExpired() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = checkIfTokenExpiredUseCase()
            when (result) {
                is AppResult.Success -> _uiState.update { it.copy(isTokenExpired = false) }
                is AppResult.Error -> {
                    if (result.code == NetworkStatusCode.HTTP_CODE_UNAUTHORIZED) {
                        _uiState.update { it.copy(isTokenExpired = true) }
                    } else {
                        Log.e("CHECK EXPIRED", result.code.toString())
                    }
                }
                is AppResult.Loading -> { }
            }
        }
    }

    fun saveIsDarkModeEnable(isDarkModeEnable: Boolean) {
        _uiState.update { it.copy(isDarkModeEnabled = isDarkModeEnable) }
        viewModelScope.launch(Dispatchers.IO) {
            settingUserProfileUseCase.saveDarkModeUseCase(isDarkModeEnable)
        }
    }

    fun saveIsFingerPrintEnable(isFingerprintEnable: Boolean) {
        _uiState.update { it.copy(isFingerprintEnabled = isFingerprintEnable) }
        viewModelScope.launch(Dispatchers.IO) {
            settingUserProfileUseCase.saveFingerPrintUseCase(isFingerprintEnable)
        }
    }

    fun deleteLocalToken() {
        viewModelScope.launch(Dispatchers.IO) {
            saveJwtTokenUseCase("")
            _uiEvent.emit(UiEvent.Navigate("Login"))
        }
    }

    fun generateCypher(): Cipher {
        return cryptographyManagerUseCase.getInitializedCipherForEncryption()
    }

    fun encryptedServerTokenWrapper(
        authResult: BiometricPrompt.AuthenticationResult,
        fingerprintToken: String
    ): CiphertextWrapper? {
        authResult.cryptoObject?.cipher?.apply {
            return cryptographyManagerUseCase.encryptData(fingerprintToken, this)
        }
        return null
    }

    fun saveCipherTextWrapper(encryptedServerTokenWrapper: CiphertextWrapper) {
        viewModelScope.launch(Dispatchers.IO) {
            settingFingerprintUseCase.saveCipherTextWrapperUseCase(encryptedServerTokenWrapper)
        }
    }

    suspend fun requestFingerprintToken(): AppResult<TokenData> {
        return requestFingerprintTokenUseCase()
    }

    fun clearLocalToken() {
        viewModelScope.launch(Dispatchers.IO) {
            clearLocalTokenUseCase()
        }
    }

    fun isNetworkAvailable(): Boolean {
        return networkUtil.isInternetAvailable()
    }
}
