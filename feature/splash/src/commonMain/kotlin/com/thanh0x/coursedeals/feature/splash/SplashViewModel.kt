package com.thanh0x.coursedeals.feature.splash

import com.thanh0x.coursedeals.core.common.AppVersion
import com.thanh0x.coursedeals.core.common.Constant
import com.thanh0x.coursedeals.core.ui.ViewModel
import com.thanh0x.coursedeals.domain.user.FeatureFlagManager
import com.thanh0x.coursedeals.domain.user.usecase.GetLocalTokenUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

enum class SplashNavigationTarget {
    MAIN,
    LOGIN,
}

data class SplashUiState(
    val isLoading: Boolean = true,
    val isForceUpdateRequired: Boolean = false,
    val isUpdateRecommended: Boolean = false,
    val forceUpdateUrl: String = Constant.DEFAULT_FORCE_UPDATE_URL,
    val navigationTarget: SplashNavigationTarget? = null,
)

class SplashViewModel(
    private val featureFlagManager: FeatureFlagManager,
    private val getLocalTokenUseCase: GetLocalTokenUseCase,
    private val currentVersionCodeProvider: () -> Int = { AppVersion.currentVersionCode() },
    private val configSyncTimeoutMs: Long = DEFAULT_CONFIG_SYNC_TIMEOUT_MS,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState = _uiState.asStateFlow()

    init {
        evaluateStartupGate()
    }

    fun evaluateStartupGate() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            withTimeoutOrNull(configSyncTimeoutMs) {
                featureFlagManager.refresh()
            }
            val config = featureFlagManager.configFlow.value
            val versionCode = currentVersionCodeProvider()
            val updateUrl = config.forceUpdateUrl.ifBlank { Constant.DEFAULT_FORCE_UPDATE_URL }
            if (versionCode < config.minSupportedVersionCode) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isForceUpdateRequired = true,
                        isUpdateRecommended = false,
                        forceUpdateUrl = updateUrl,
                        navigationTarget = null,
                    )
                }
            } else {
                val token = runCatching { getLocalTokenUseCase() }.getOrNull()
                val target = if (!token.isNullOrBlank()) {
                    SplashNavigationTarget.MAIN
                } else {
                    SplashNavigationTarget.LOGIN
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isForceUpdateRequired = false,
                        isUpdateRecommended = versionCode < config.latestVersionCode,
                        forceUpdateUrl = updateUrl,
                        navigationTarget = target,
                    )
                }
            }
        }
    }

    companion object {
        const val DEFAULT_CONFIG_SYNC_TIMEOUT_MS = 2_500L
    }
}
