package com.batdaulaptrinh.freeudemycoupons.ui.login

import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batdaulaptrinh.freeudemycoupons.data.model.TokenResponseData
import com.batdaulaptrinh.freeudemycoupons.domain.usecase.authentication.LoginUseCase
import com.batdaulaptrinh.freeudemycoupons.domain.usecase.authentication.fingerprint.CryptographyManagerUseCase
import com.batdaulaptrinh.freeudemycoupons.domain.usecase.authentication.fingerprint.SettingFingerprintUseCase
import com.batdaulaptrinh.freeudemycoupons.domain.usecase.authentication.jwt.ClearLocalTokenUseCase
import com.batdaulaptrinh.freeudemycoupons.domain.usecase.authentication.jwt.RequestAccessTokenUseCase
import com.batdaulaptrinh.freeudemycoupons.domain.usecase.authentication.jwt.SaveJwtTokenUseCase
import com.batdaulaptrinh.freeudemycoupons.domain.usecase.user_profile.SettingUserProfileUseCase
import com.batdaulaptrinh.freeudemycoupons.util.NetWorkResult
import com.batdaulaptrinh.freeudemycoupons.util.NetworkUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    val loginResponseResult = MutableLiveData<NetWorkResult<TokenResponseData>>()
    val decryptionResult = MutableLiveData<String>()
    val newAccessTokenFromFingerprint = MutableLiveData<String>()
    val isLoginByFingerprintEnable = MutableLiveData<Boolean?>()

    init {
        checkIsAbleLoginByFingerprint()
    }

    fun login(username: String, password: String) {
        loginResponseResult.postValue(NetWorkResult.Loading())
        viewModelScope.launch(Dispatchers.IO) {
            val networkResult = loginUseCase(username, password)
            if (networkResult is NetWorkResult.Success && networkResult.data != null) {
                saveJwtTokenUseCase(networkResult.data.accessToken)
            }
            loginResponseResult.postValue(networkResult)
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
                    decryptionResult.postValue(localFingerToken)
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

    fun requestJwtTokenFromFingerprint(fingerToken: String) {
        viewModelScope.launch {
            saveFingerprintToStorage(fingerToken)
            val accessTokenResponse = requestAccessTokenUseCase()
            Log.d("ACCESS TOKEN RESPONSE", accessTokenResponse.toString())
            if (accessTokenResponse.isSuccessful && accessTokenResponse.body() is TokenResponseData) {
                newAccessTokenFromFingerprint.postValue(accessTokenResponse.body()!!.accessToken)
                clearFingerprintToken()
                saveAccessToken(accessTokenResponse.body()!!.accessToken)
            } else {
                Log.e("ACCESS TOKEN RESPONSE", "ERROR")
            }
        }
    }

    private suspend fun saveFingerprintToStorage(token: String) {
        saveAccessToken(token)
    }

    suspend fun saveAccessToken(token: String) {
        saveJwtTokenUseCase(token)
    }

    private suspend fun clearFingerprintToken() {
        clearLocalTokenUseCase()
    }

    private fun checkIsAbleLoginByFingerprint() {
        viewModelScope.launch {
            isLoginByFingerprintEnable.postValue(settingUserProfileUseCase.loadFingerprintUseCase())
        }
    }
}
