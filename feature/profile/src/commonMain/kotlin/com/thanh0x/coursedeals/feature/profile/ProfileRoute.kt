package com.thanh0x.coursedeals.feature.profile

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.core.common.NetworkStatusCode
import com.thanh0x.coursedeals.core.ui.DialogType
import com.thanh0x.coursedeals.core.ui.resources.Res
import com.thanh0x.coursedeals.core.ui.resources.getString
import com.thanh0x.coursedeals.core.ui.util.PlatformActions
import com.thanh0x.coursedeals.domain.user.TokenData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier,
    onNavigateToLogin: () -> Unit = {},
    showAlertDialog: (String, String, DialogType) -> Unit = { _, _, _ -> },
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCategoryPicker by remember { mutableStateOf(false) }

    ProfileScreen(
        uiState = uiState,
        modifier = modifier,
        onEmailClick = {
            if (uiState.isGuestMode) {
                onNavigateToLogin()
            } else if (uiState.email.isNotEmpty()) {
                PlatformActions.copyToClipboard("Course Deal Email", uiState.email)
                PlatformActions.showToast("Email copied to clipboard")
            }
        },
        onAddCategoryClick = { showCategoryPicker = true },
        onRemoveCategory = { cat ->
            val current = uiState.favCategories.toMutableList().apply { remove(cat) }
            viewModel.updateFavoriteCategories(current)
        },
        onAddKeyword = { kw -> viewModel.addFavoriteKeyword(kw) },
        onRemoveKeyword = { kw -> viewModel.removeFavoriteKeyword(kw) },
        onDarkModeToggled = { isChecked ->
            viewModel.saveIsDarkModeEnable(isChecked)
            PlatformActions.applyDarkMode(isChecked)
        },
        onFingerprintToggled = { isChecked ->
            handleFingerprintToggle(
                viewModel = viewModel,
                isChecked = isChecked,
                showAlertDialog = showAlertDialog,
                onForceLogout = onNavigateToLogin,
                coroutineScope = coroutineScope,
            )
        },
        onNotificationsToggled = { isChecked -> viewModel.updateNotificationsEnabled(isChecked) },
        onLogoutClick = {
            viewModel.deleteLocalToken()
            onNavigateToLogin()
        },
        onDevEmailClick = {
            val sent = PlatformActions.sendEmail(
                email = getString(Res.string.dev_email),
                subject = getString(Res.string.email_send_title),
            )
            if (!sent) {
                showAlertDialog(
                    getString(Res.string.email_failed_to_send),
                    getString(Res.string.email_cannot_find_client_app),
                    DialogType.ERROR,
                )
            }
        },
        onDevWebsiteClick = {
            PlatformActions.openUrl(getString(Res.string.dev_website))
        },
        onSignInClick = onNavigateToLogin,
    )

    if (showCategoryPicker) {
        CategoryPickerBottomSheet(
            initialSelected = uiState.favCategories,
            onDismissRequest = { showCategoryPicker = false },
            onDone = { selected ->
                showCategoryPicker = false
                viewModel.updateFavoriteCategories(selected)
            },
        )
    }
}

private fun handleFingerprintToggle(
    viewModel: ProfileViewModel,
    isChecked: Boolean,
    showAlertDialog: (String, String, DialogType) -> Unit,
    onForceLogout: () -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
) {
    if (viewModel.uiState.value.isGuestMode) {
        if (isChecked) {
            PlatformActions.showToast(getString(Res.string.guest_biometric_unavailable))
        }
        return
    }
    if (isChecked) {
        if (!viewModel.isNetworkAvailable()) {
            showAlertDialog(
                getString(Res.string.no_internet_title),
                getString(Res.string.no_internet_message),
                DialogType.WARNING,
            )
            viewModel.saveIsFingerPrintEnable(false)
        } else {
            coroutineScope.launch {
                val result = viewModel.requestFingerprintToken()
                if (result is AppResult.Success<TokenData>) {
                    val cipherTextWrapper = viewModel.encryptedServerTokenWrapper(result.data.accessToken)
                    PlatformActions.showToast("Add fingerprint successfully")
                    viewModel.saveCipherTextWrapper(cipherTextWrapper)
                    viewModel.saveIsFingerPrintEnable(true)
                } else if (result is AppResult.Error) {
                    viewModel.saveIsFingerPrintEnable(false)
                    if (result.code == NetworkStatusCode.HTTP_CODE_UNAUTHORIZED) {
                        onForceLogout()
                    }
                }
            }
        }
    } else {
        viewModel.saveIsFingerPrintEnable(false)
    }
}
