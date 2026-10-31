package com.thanh0x.coursedeals.feature.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.core.common.NetworkStatusCode
import com.thanh0x.coursedeals.core.ui.BiometricPromptUtils
import com.thanh0x.coursedeals.core.ui.DialogType
import com.thanh0x.coursedeals.core.ui.R as CoreR
import com.thanh0x.coursedeals.domain.user.TokenData
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier,
    onNavigateToLogin: () -> Unit = {},
    showAlertDialog: (String, String, DialogType) -> Unit = { _, _, _ -> },
) {
    val context = LocalContext.current
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
                copyToClipboard(context, uiState.email)
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
            applyDarkMode(isChecked)
        },
        onFingerprintToggled = { isChecked ->
            handleFingerprintToggle(
                context = context,
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
        onDevEmailClick = { sendDevEmail(context, showAlertDialog) },
        onDevWebsiteClick = { openDevWebsite(context) },
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

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Course Deal Email", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Email copied to clipboard", Toast.LENGTH_SHORT).show()
}

private fun applyDarkMode(isDarkModeEnable: Boolean) {
    val mode = if (isDarkModeEnable) {
        AppCompatDelegate.MODE_NIGHT_YES
    } else {
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
    AppCompatDelegate.setDefaultNightMode(mode)
}

private fun handleFingerprintToggle(
    context: Context,
    viewModel: ProfileViewModel,
    isChecked: Boolean,
    showAlertDialog: (String, String, DialogType) -> Unit,
    onForceLogout: () -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
) {
    if (viewModel.uiState.value.isGuestMode) {
        if (isChecked) {
            Toast.makeText(context, context.getString(CoreR.string.guest_biometric_unavailable), Toast.LENGTH_SHORT).show()
        }
        return
    }
    if (isChecked) {
        if (!viewModel.isNetworkAvailable()) {
            showAlertDialog(
                context.getString(CoreR.string.no_internet_title),
                context.getString(CoreR.string.no_internet_message),
                DialogType.WARNING,
            )
            viewModel.saveIsFingerPrintEnable(false)
        } else {
            promptFingerPrintSignUp(context, viewModel, onForceLogout, coroutineScope)
        }
    } else {
        viewModel.saveIsFingerPrintEnable(false)
    }
}

@Suppress("DEPRECATION")
private fun promptFingerPrintSignUp(
    context: Context,
    viewModel: ProfileViewModel,
    onForceLogout: () -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
) {
    val activity = context as? AppCompatActivity ?: return
    val canAuthenticate = BiometricManager.from(context).canAuthenticate()
    if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
        viewModel.saveIsFingerPrintEnable(false)
        return
    }

    val biometricPrompt = BiometricPromptUtils.createBiometricPrompt(activity, {
        coroutineScope.launch {
            val result = viewModel.requestFingerprintToken()
            if (result is AppResult.Success<TokenData>) {
                val cipherTextWrapper = viewModel.encryptedServerTokenWrapper(it, result.data.accessToken)
                if (cipherTextWrapper != null) {
                    Toast.makeText(context, "Add fingerprint successfully", Toast.LENGTH_SHORT).show()
                    viewModel.saveCipherTextWrapper(cipherTextWrapper)
                    viewModel.saveIsFingerPrintEnable(true)
                }
            } else if (result is AppResult.Error) {
                viewModel.saveIsFingerPrintEnable(false)
                if (result.code == NetworkStatusCode.HTTP_CODE_UNAUTHORIZED) {
                    onForceLogout()
                }
            }
        }
    }, {
        viewModel.saveIsFingerPrintEnable(false)
    })

    val promptInfo = BiometricPromptUtils.createPromptInfo(
        context.getString(CoreR.string.prompt_info_title),
        context.getString(CoreR.string.prompt_info_description),
        context.getString(CoreR.string.cancel_button_text),
    )
    val cipher = viewModel.generateCypher()
    biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
}

private fun sendDevEmail(
    context: Context,
    showAlertDialog: (String, String, DialogType) -> Unit,
) {
    val devEmailAddress = context.getString(CoreR.string.dev_email)
    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:$devEmailAddress")
        putExtra(Intent.EXTRA_SUBJECT, context.getString(CoreR.string.email_send_title))
    }
    try {
        context.startActivity(emailIntent)
    } catch (e: android.content.ActivityNotFoundException) {
        Timber.e(e, "Failed to send email: No activity found")
        showAlertDialog(
            context.getString(CoreR.string.email_failed_to_send),
            context.getString(CoreR.string.email_cannot_find_client_app),
            DialogType.ERROR,
        )
    }
}

private fun openDevWebsite(context: Context) {
    val devWebsiteAddress = context.getString(CoreR.string.dev_website)
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(devWebsiteAddress))
    try {
        context.startActivity(browserIntent)
    } catch (e: android.content.ActivityNotFoundException) {
        Timber.e(e, "Failed to open website: No browser installed")
        Toast.makeText(context, "No web browser installed.", Toast.LENGTH_SHORT).show()
    }
}
