package com.thanh0x.coursedeals.ui.core.profile

import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.MutableLiveData
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
import com.thanh0x.coursedeals.util.NetworkStatusCode
import com.thanh0x.coursedeals.util.NetworkUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
    val isDarkModeEnable = MutableLiveData<Boolean>()
    val isFingerPrintEnable = MutableLiveData<Boolean>()
    val tokenExpired = MutableLiveData<Boolean>()

    init {
        initSwitchState()
        checkIfTokenExpired()
    }

    private fun initSwitchState() {
        viewModelScope.launch(Dispatchers.IO) {
            isDarkModeEnable.postValue(settingUserProfileUseCase.loadDarkModeUseCase() ?: false)
            isFingerPrintEnable.postValue(
                settingUserProfileUseCase.loadFingerprintUseCase() ?: false
            )
        }
    }

    fun checkIfTokenExpired() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = checkIfTokenExpiredUseCase()
            when (result) {
                is AppResult.Success -> tokenExpired.postValue(false)
                is AppResult.Error -> {
                    if (result.code == NetworkStatusCode.HTTP_CODE_UNAUTHORIZED) {
                        tokenExpired.postValue(true)
                    } else {
                        Log.e("CHECK EXPIRED", result.code.toString())
                    }
                }
                is AppResult.Loading -> { /* Handle loading if needed */ }
            }
        }
    }

    fun saveIsDarkModeEnable(isDarkModeEnable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingUserProfileUseCase.saveDarkModeUseCase(isDarkModeEnable)
        }
    }

    fun saveIsFingerPrintEnable(isFingerprintEnable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingUserProfileUseCase.saveFingerPrintUseCase(isFingerprintEnable)
        }
    }

    fun deleteLocalToken() {
        viewModelScope.launch(Dispatchers.IO) {
            saveJwtTokenUseCase("")
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
