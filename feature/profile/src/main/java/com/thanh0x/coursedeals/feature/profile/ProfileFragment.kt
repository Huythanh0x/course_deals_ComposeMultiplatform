package com.thanh0x.coursedeals.feature.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.core.common.NetworkStatusCode
import com.thanh0x.coursedeals.core.ui.BaseFragment
import com.thanh0x.coursedeals.core.ui.BiometricPromptUtils
import com.thanh0x.coursedeals.core.ui.DialogType
import com.thanh0x.coursedeals.core.ui.R as CoreR
import com.thanh0x.coursedeals.core.ui.UiEvent
import com.thanh0x.coursedeals.core.ui.theme.CourseDealsTheme
import com.thanh0x.coursedeals.domain.user.TokenData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ProfileFragment : BaseFragment() {
    private val profileViewModel: ProfileViewModel by viewModels()
    private var appliedDarkMode: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CourseDealsTheme {
                    ProfileRoute()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ProfileRoute() {
        val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()
        var showCategoryPicker by remember { mutableStateOf(false) }

        ProfileScreen(
            uiState = uiState,
            onEmailClick = {
                if (uiState.isGuestMode) {
                    navigateToLogin()
                } else if (uiState.email.isNotEmpty()) {
                    copyToClipboard(uiState.email)
                }
            },
            onAddCategoryClick = {
                showCategoryPicker = true
            },
            onRemoveCategory = { cat ->
                val current = uiState.favCategories.toMutableList()
                current.remove(cat)
                profileViewModel.updateFavoriteCategories(current)
            },
            onAddKeyword = { kw ->
                profileViewModel.addFavoriteKeyword(kw)
            },
            onRemoveKeyword = { kw ->
                profileViewModel.removeFavoriteKeyword(kw)
            },
            onDarkModeToggled = { isChecked ->
                profileViewModel.saveIsDarkModeEnable(isChecked)
            },
            onFingerprintToggled = { isChecked ->
                handleFingerprintToggle(isChecked)
            },
            onNotificationsToggled = { isChecked ->
                profileViewModel.updateNotificationsEnabled(isChecked)
            },
            onLogoutClick = {
                profileViewModel.deleteLocalToken()
            },
            onDevEmailClick = {
                sendDevEmail()
            },
            onDevWebsiteClick = {
                openDevWebsite()
            },
            onSignInClick = {
                navigateToLogin()
            }
        )

        if (showCategoryPicker) {
            CategoryPickerBottomSheet(
                initialSelected = uiState.favCategories,
                onDismissRequest = { showCategoryPicker = false },
                onDone = { selected ->
                    showCategoryPicker = false
                    profileViewModel.updateFavoriteCategories(selected)
                }
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        collectFlow(profileViewModel.uiState) { state ->
            handleUiState(state)
        }

        collectFlow(profileViewModel.uiEvent) { event ->
            handleUiEvent(event)
        }
    }

    private fun handleUiState(state: ProfileUiState) {
        if (!state.isGuestMode && state.isTokenExpired) {
            forceLogout()
            return
        }

        if (appliedDarkMode != state.isDarkModeEnabled) {
            appliedDarkMode = state.isDarkModeEnabled
            applyDarkModeToTheApp(state.isDarkModeEnabled)
        }
    }

    private fun handleFingerprintToggle(isChecked: Boolean) {
        if (profileViewModel.uiState.value.isGuestMode) {
            if (isChecked) {
                showToast(getString(CoreR.string.guest_biometric_unavailable))
            }
            return
        }
        if (isChecked) {
            if (!profileViewModel.isNetworkAvailable()) {
                showAlertDialog(
                    title = getString(CoreR.string.no_internet_title),
                    message = getString(CoreR.string.no_internet_message),
                    type = DialogType.WARNING,
                )
                profileViewModel.saveIsFingerPrintEnable(false)
            } else {
                promptFingerPrintSignUp()
            }
        } else {
            profileViewModel.saveIsFingerPrintEnable(isFingerprintEnable = false)
        }
    }

    private fun sendDevEmail() {
        val devEmailAddress = getString(CoreR.string.dev_email)
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$devEmailAddress")
            putExtra(Intent.EXTRA_SUBJECT, getString(CoreR.string.email_send_title))
        }

        try {
            startActivity(emailIntent)
        } catch (e: android.content.ActivityNotFoundException) {
            Timber.e(e, "Failed to send email: No activity found")
            showAlertDialog(
                title = getString(CoreR.string.email_failed_to_send),
                message = getString(CoreR.string.email_cannot_find_client_app),
                type = DialogType.ERROR,
            )
        }
    }

    private fun openDevWebsite() {
        val devWebsiteAddress = getString(CoreR.string.dev_website)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(devWebsiteAddress))

        try {
            startActivity(browserIntent)
        } catch (e: android.content.ActivityNotFoundException) {
            Timber.e(e, "Failed to open website: No browser installed")
            Toast.makeText(requireContext(), "No web browser installed.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun handleUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.Navigate -> {
                if (event.destination == "Login") {
                    forceLogout()
                }
            }
            else -> super.handleUiEvent(event)
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Course Deal Email", text)
        clipboard.setPrimaryClip(clip)
        showToast("Email copied to clipboard")
    }

    @Suppress("DEPRECATION")
    private fun promptFingerPrintSignUp() {
        val canAuthenticate = BiometricManager.from(requireContext()).canAuthenticate()
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(requireActivity() as AppCompatActivity, {
                    lifecycleScope.launch {
                        val result = profileViewModel.requestFingerprintToken()
                        if (result is AppResult.Success<TokenData>) {
                            val cipherTextWrapper =
                                profileViewModel.encryptedServerTokenWrapper(
                                    it,
                                    result.data.accessToken,
                                )
                            if (cipherTextWrapper != null) {
                                showToast("Add fingerprint successfully")
                                profileViewModel.saveCipherTextWrapper(cipherTextWrapper)
                                profileViewModel.saveIsFingerPrintEnable(true)
                            }
                        } else if (result is AppResult.Error) {
                            profileViewModel.saveIsFingerPrintEnable(false)
                            if (result.code == NetworkStatusCode.HTTP_CODE_UNAUTHORIZED) {
                                forceLogout()
                            }
                        }
                    }
                }, {
                    profileViewModel.saveIsFingerPrintEnable(false)
                })
            val promptInfo = BiometricPromptUtils.createPromptInfo(
                getString(CoreR.string.prompt_info_title),
                getString(CoreR.string.prompt_info_description),
                getString(CoreR.string.cancel_button_text)
            )
            val cipher = profileViewModel.generateCypher()
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        } else {
            profileViewModel.saveIsFingerPrintEnable(false)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun forceLogout() {
        profileViewModel.clearLocalToken()
        val intent = Intent().apply {
            setClassName(requireContext().packageName, "com.thanh0x.coursedeals.feature.auth.LoginActivity")
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun navigateToLogin() {
        val intent = Intent().apply {
            setClassName(requireContext().packageName, "com.thanh0x.coursedeals.feature.auth.LoginActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun applyDarkModeToTheApp(isDarkModeEnable: Boolean) {
        val mode = if (isDarkModeEnable) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
