package com.thanh0x.coursedeals.ui.core.profile

import androidx.biometric.BiometricPrompt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thanh0x.coursedeals.domain.user.CiphertextWrapper
import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.TokenData
import com.thanh0x.coursedeals.domain.user.UserProfileRepository
import com.thanh0x.coursedeals.domain.user.usecase.CryptographyManagerUseCase
import com.thanh0x.coursedeals.domain.user.usecase.RequestFingerprintTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SettingFingerprintUseCase
import com.thanh0x.coursedeals.domain.user.usecase.CheckIfTokenExpiredUseCase
import com.thanh0x.coursedeals.domain.user.usecase.ClearLocalTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SaveJwtTokenUseCase
import com.thanh0x.coursedeals.core.ui.UiEvent
import com.thanh0x.coursedeals.core.common.NetworkStatusCode
import com.thanh0x.coursedeals.util.NetworkUtil
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
class ProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    val saveJwtTokenUseCase: SaveJwtTokenUseCase,
    private val cryptographyManagerUseCase: CryptographyManagerUseCase,
    private val settingFingerprintUseCase: SettingFingerprintUseCase,
    private val requestFingerprintTokenUseCase: RequestFingerprintTokenUseCase,
    private val clearLocalTokenUseCase: ClearLocalTokenUseCase,
    private val checkIfTokenExpiredUseCase: CheckIfTokenExpiredUseCase,
    private val networkUtil: NetworkUtil,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        initSettings()
        checkIfTokenExpired()
        syncPreferences()
    }

    private fun initSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            val darkMode = userProfileRepository.getEnableDarkMode() ?: false
            val fingerprint = userProfileRepository.getEnableFingerPrint() ?: false
            val categories = userProfileRepository.getFavoriteCategories().toList()
            val keywords = userProfileRepository.getFavoriteKeywords().toList()
            val notifications = userProfileRepository.getNotificationsEnabled()

            _uiState.update {
                it.copy(
                    isDarkModeEnabled = darkMode,
                    isFingerprintEnabled = fingerprint,
                    favCategories = categories,
                    favKeywords = keywords,
                    isNotificationsEnabled = notifications,
                )
            }
        }
    }

    private fun syncPreferences() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSyncing = true) }
            val result = userProfileRepository.syncPreferences()
            if (result is AppResult.Success) {
                val prefs = result.data
                _uiState.update {
                    it.copy(
                        favCategories = prefs.categories,
                        favKeywords = prefs.keywords,
                        isNotificationsEnabled = prefs.notificationsEnabled,
                    )
                }
            }
            _uiState.update { it.copy(isSyncing = false) }
        }
    }

    fun checkIfTokenExpired() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = checkIfTokenExpiredUseCase()) {
                is AppResult.Success -> _uiState.update { it.copy(isTokenExpired = false) }
                is AppResult.Error -> {
                    if (result.code == NetworkStatusCode.HTTP_CODE_UNAUTHORIZED) {
                        _uiState.update { it.copy(isTokenExpired = true) }
                    } else {
                        Timber.e("CHECK EXPIRED: ${result.code}")
                    }
                }
                is AppResult.Loading -> { }
            }
        }
    }

    fun saveIsDarkModeEnable(isDarkModeEnable: Boolean) {
        _uiState.update { it.copy(isDarkModeEnabled = isDarkModeEnable) }
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.saveEnableDarkMode(isDarkModeEnable)
        }
    }

    fun saveIsFingerPrintEnable(isFingerprintEnable: Boolean) {
        _uiState.update { it.copy(isFingerprintEnabled = isFingerprintEnable) }
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.saveEnableFingerPrint(isFingerprintEnable)
        }
    }

    fun updateFavoriteCategories(categories: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(favCategories = categories) }
            userProfileRepository.saveFavoriteCategories(categories.toSet())
        }
    }

    fun addFavoriteKeyword(keyword: String) {
        val current = _uiState.value.favKeywords.toMutableSet()
        if (current.add(keyword)) {
            val newList = current.toList()
            _uiState.update { it.copy(favKeywords = newList) }
            viewModelScope.launch(Dispatchers.IO) {
                userProfileRepository.saveFavoriteKeywords(newList.toSet())
            }
        }
    }

    fun removeFavoriteKeyword(keyword: String) {
        val current = _uiState.value.favKeywords.toMutableSet()
        if (current.remove(keyword)) {
            val newList = current.toList()
            _uiState.update { it.copy(favKeywords = newList) }
            viewModelScope.launch(Dispatchers.IO) {
                userProfileRepository.saveFavoriteKeywords(newList.toSet())
            }
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isNotificationsEnabled = enabled) }
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.saveNotificationsEnabled(enabled)
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
        fingerprintToken: String,
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
