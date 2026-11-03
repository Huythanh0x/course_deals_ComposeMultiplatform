package com.thanh0x.coursedeals.feature.profile

import com.thanh0x.coursedeals.core.logging.LogLevel

data class ProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val isLoading: Boolean = false,
    val isDarkModeEnabled: Boolean = false,
    val isFingerprintEnabled: Boolean = false,
    val isTokenExpired: Boolean = false,
    val favCategories: List<String> = emptyList(),
    val favKeywords: List<String> = emptyList(),
    val isNotificationsEnabled: Boolean = true,
    val subscribedTopics: Set<String> = emptySet(),
    val isSyncing: Boolean = false,
    val isGuestMode: Boolean = false,
    val isDeleteAccountDialogOpen: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val errorMessage: String? = null,
    val isDiagnosticLogsOpen: Boolean = false,
    val diagnosticLogs: String = "",
    val logFilterLevel: LogLevel? = null,
)
