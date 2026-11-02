package com.thanh0x.coursedeals.feature.profile

import com.thanh0x.coursedeals.domain.user.CiphertextWrapper
import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.core.logging.LogFileManager
import com.thanh0x.coursedeals.core.logging.LogLevel
import com.thanh0x.coursedeals.core.ui.DialogType
import com.thanh0x.coursedeals.core.ui.ViewModel
import com.thanh0x.coursedeals.domain.user.TokenData
import com.thanh0x.coursedeals.domain.user.UserProfileRepository
import com.thanh0x.coursedeals.domain.user.usecase.CheckIfTokenExpiredUseCase
import com.thanh0x.coursedeals.domain.user.usecase.ClearLocalTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.CryptographyManagerUseCase
import com.thanh0x.coursedeals.domain.user.usecase.DeleteAccountUseCase
import com.thanh0x.coursedeals.domain.user.usecase.GetLocalTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.GetUserProfileUseCase
import com.thanh0x.coursedeals.domain.user.usecase.RequestFingerprintTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SaveJwtTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SettingFingerprintUseCase
import com.thanh0x.coursedeals.core.common.notification.NotificationTopic
import com.thanh0x.coursedeals.core.common.notification.PushNotificationManager
import com.thanh0x.coursedeals.core.ui.UiEvent
import com.thanh0x.coursedeals.core.common.NetworkStatusCode
import com.thanh0x.coursedeals.core.ui.util.NetworkUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    val saveJwtTokenUseCase: SaveJwtTokenUseCase,
    private val cryptographyManagerUseCase: CryptographyManagerUseCase,
    private val settingFingerprintUseCase: SettingFingerprintUseCase,
    private val requestFingerprintTokenUseCase: RequestFingerprintTokenUseCase,
    private val clearLocalTokenUseCase: ClearLocalTokenUseCase,
    private val checkIfTokenExpiredUseCase: CheckIfTokenExpiredUseCase,
    private val getLocalTokenUseCase: GetLocalTokenUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase? = null,
    private val networkUtil: NetworkUtil,
    private val pushNotificationManager: PushNotificationManager? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        initSettings()
    }

    fun initSettings() {
        viewModelScope.launch(ioDispatcher) {
            val token = getLocalTokenUseCase()
            val isGuest = token.isNullOrBlank()
            val cachedProfile = userProfileRepository.getCachedUserProfile()
            val darkMode = userProfileRepository.getEnableDarkMode() ?: false
            val fingerprint = userProfileRepository.getEnableFingerPrint() ?: false
            val categories = userProfileRepository.getFavoriteCategories().toList()
            val keywords = userProfileRepository.getFavoriteKeywords().toList()
            val notifications = userProfileRepository.getNotificationsEnabled()
            val initialTopics = if (notifications) {
                setOf(NotificationTopic.TOPIC_DEALS_ALL)
            } else {
                emptySet()
            }

            _uiState.update {
                it.copy(
                    isGuestMode = isGuest,
                    displayName = if (isGuest) "" else (cachedProfile.fullName ?: it.displayName),
                    email = if (isGuest) "" else (cachedProfile.email ?: it.email),
                    avatarUrl = if (isGuest) null else (cachedProfile.avatarUrl ?: it.avatarUrl),
                    isDarkModeEnabled = darkMode,
                    isFingerprintEnabled = if (isGuest) false else fingerprint,
                    favCategories = categories,
                    favKeywords = keywords,
                    isNotificationsEnabled = notifications,
                    subscribedTopics = it.subscribedTopics.ifEmpty { initialTopics },
                )
            }

            pushNotificationManager?.let { manager ->
                launch {
                    manager.getSubscribedTopics().collectLatest { topics ->
                        if (_uiState.value.isNotificationsEnabled && topics.isNotEmpty()) {
                            _uiState.update { it.copy(subscribedTopics = topics) }
                        }
                    }
                }
            }
            if (!isGuest) {
                checkIfTokenExpired()
                fetchUserProfile()
                syncPreferences()
            }
        }
    }

    fun fetchUserProfile() {
        if (_uiState.value.isGuestMode) return
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = getUserProfileUseCase()) {
                is AppResult.Success -> {
                    val profile = result.data
                    _uiState.update { state ->
                        state.copy(
                            displayName = profile.fullName ?: state.displayName,
                            email = profile.email ?: state.email,
                            avatarUrl = profile.avatarUrl ?: state.avatarUrl,
                            favCategories = profile.preferences?.categories ?: state.favCategories,
                            favKeywords = profile.preferences?.keywords ?: state.favKeywords,
                            isNotificationsEnabled = profile.preferences?.notificationsEnabled
                                ?: state.isNotificationsEnabled,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                        )
                    }
                }
                is AppResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun syncPreferences() {
        if (_uiState.value.isGuestMode) return
        viewModelScope.launch(ioDispatcher) {
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
        if (_uiState.value.isGuestMode) return
        viewModelScope.launch(ioDispatcher) {
            when (val result = checkIfTokenExpiredUseCase()) {
                is AppResult.Success -> _uiState.update { it.copy(isTokenExpired = false) }
                is AppResult.Error -> {
                    if (result.code == NetworkStatusCode.HTTP_CODE_UNAUTHORIZED) {
                        _uiState.update { it.copy(isTokenExpired = true) }
                    }
                }
                is AppResult.Loading -> { }
            }
        }
    }

    fun saveIsDarkModeEnable(isDarkModeEnable: Boolean) {
        _uiState.update { it.copy(isDarkModeEnabled = isDarkModeEnable) }
        viewModelScope.launch(ioDispatcher) {
            userProfileRepository.saveEnableDarkMode(isDarkModeEnable)
        }
    }

    fun saveIsFingerPrintEnable(isFingerprintEnable: Boolean) {
        _uiState.update { it.copy(isFingerprintEnabled = isFingerprintEnable) }
        viewModelScope.launch(ioDispatcher) {
            userProfileRepository.saveEnableFingerPrint(isFingerprintEnable)
        }
    }

    fun updateFavoriteCategories(categories: List<String>) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(favCategories = categories) }
            userProfileRepository.saveFavoriteCategories(categories.toSet())
        }
    }

    fun addFavoriteKeyword(keyword: String) {
        val current = _uiState.value.favKeywords.toMutableSet()
        if (current.add(keyword)) {
            val newList = current.toList()
            _uiState.update { it.copy(favKeywords = newList) }
            viewModelScope.launch(ioDispatcher) {
                userProfileRepository.saveFavoriteKeywords(newList.toSet())
            }
        }
    }

    fun removeFavoriteKeyword(keyword: String) {
        val current = _uiState.value.favKeywords.toMutableSet()
        if (current.remove(keyword)) {
            val newList = current.toList()
            _uiState.update { it.copy(favKeywords = newList) }
            viewModelScope.launch(ioDispatcher) {
                userProfileRepository.saveFavoriteKeywords(newList.toSet())
            }
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isNotificationsEnabled = enabled) }
        viewModelScope.launch(ioDispatcher) {
            userProfileRepository.saveNotificationsEnabled(enabled)
            if (!enabled) {
                val topicsToMute = _uiState.value.subscribedTopics
                topicsToMute.forEach { topic ->
                    pushNotificationManager?.unsubscribeFromTopic(topic)
                }
            } else {
                val topicsToRestore = if (_uiState.value.subscribedTopics.isEmpty()) {
                    setOf(NotificationTopic.TOPIC_DEALS_ALL)
                } else {
                    _uiState.value.subscribedTopics
                }
                _uiState.update { it.copy(subscribedTopics = topicsToRestore) }
                topicsToRestore.forEach { topic ->
                    pushNotificationManager?.subscribeToTopic(topic)
                }
            }
        }
    }

    fun toggleTopic(topicId: String, enabled: Boolean) {
        val currentTopics = _uiState.value.subscribedTopics.toMutableSet()
        if (enabled) {
            currentTopics.add(topicId)
        } else {
            currentTopics.remove(topicId)
        }
        _uiState.update { it.copy(subscribedTopics = currentTopics) }

        viewModelScope.launch(ioDispatcher) {
            if (enabled) {
                pushNotificationManager?.subscribeToTopic(topicId)
            } else {
                pushNotificationManager?.unsubscribeFromTopic(topicId)
            }
        }
    }

    fun deleteLocalToken() {
        viewModelScope.launch(ioDispatcher) {
            clearLocalTokenUseCase()
            userProfileRepository.clearCachedUserProfile()
            _uiEvent.emit(UiEvent.Navigate("Login"))
        }
    }

    fun encryptedServerTokenWrapper(
        fingerprintToken: String,
    ): CiphertextWrapper {
        return cryptographyManagerUseCase.encryptData(fingerprintToken)
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
            userProfileRepository.clearCachedUserProfile()
        }
    }

    fun isNetworkAvailable(): Boolean {
        return networkUtil.isInternetAvailable()
    }

    fun showDeleteAccountDialog() {
        _uiState.update { it.copy(isDeleteAccountDialogOpen = true) }
    }

    fun dismissDeleteAccountDialog() {
        _uiState.update { it.copy(isDeleteAccountDialogOpen = false) }
    }

    fun deleteAccount() {
        if (_uiState.value.isGuestMode) return
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isDeletingAccount = true, isDeleteAccountDialogOpen = false) }
            val result = deleteAccountUseCase?.invoke() ?: userProfileRepository.deleteAccount()
            when (result) {
                is AppResult.Success -> {
                    clearLocalTokenUseCase()
                    userProfileRepository.clearCachedUserProfile()
                    _uiState.update {
                        it.copy(
                            isDeletingAccount = false,
                            isGuestMode = true,
                            displayName = "",
                            email = "",
                            avatarUrl = null,
                            favCategories = emptyList(),
                            favKeywords = emptyList(),
                            isFingerprintEnabled = false,
                            errorMessage = null,
                        )
                    }
                    _uiEvent.emit(UiEvent.ShowToast("Account deleted successfully"))
                    _uiEvent.emit(UiEvent.Navigate("Login"))
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isDeletingAccount = false,
                            errorMessage = result.message,
                        )
                    }
                    _uiEvent.emit(
                        UiEvent.ShowAlertDialog(
                            title = "Delete Account Failed",
                            message = result.message.ifBlank { "Failed to delete account. Please try again later." },
                            type = DialogType.ERROR,
                        )
                    )
                }
                is AppResult.Loading -> { }
            }
        }
    }

    fun openDiagnosticLogs() {
        val currentFilter = _uiState.value.logFilterLevel
        val logs = LogFileManager.getFormattedLogs(maxLines = 500, levelFilter = currentFilter)
        _uiState.update {
            it.copy(
                isDiagnosticLogsOpen = true,
                diagnosticLogs = logs,
            )
        }
    }

    fun dismissDiagnosticLogs() {
        _uiState.update { it.copy(isDiagnosticLogsOpen = false) }
    }

    fun filterLogs(level: LogLevel?) {
        val logs = LogFileManager.getFormattedLogs(maxLines = 500, levelFilter = level)
        _uiState.update {
            it.copy(
                logFilterLevel = level,
                diagnosticLogs = logs,
            )
        }
    }
}
