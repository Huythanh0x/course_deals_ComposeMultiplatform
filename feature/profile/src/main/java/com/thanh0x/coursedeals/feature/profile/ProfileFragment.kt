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
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.thanh0x.coursedeals.core.ui.R as CoreR
import com.thanh0x.coursedeals.feature.profile.R
import com.thanh0x.coursedeals.feature.profile.databinding.FragmentProfileBinding
import com.thanh0x.coursedeals.core.ui.BiometricPromptUtils
import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.TokenData
import com.thanh0x.coursedeals.core.ui.BaseFragment
import com.thanh0x.coursedeals.core.ui.UiEvent
import com.thanh0x.coursedeals.core.common.NetworkStatusCode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ProfileFragment : BaseFragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val profileViewModel: ProfileViewModel by viewModels()
    private var appliedDarkMode: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInteractions()
        setupObservers()
        setupFragmentResultListeners()
    }

    private fun setupFragmentResultListeners() {
        childFragmentManager.setFragmentResultListener(
            CategoryPickerDialog.REQUEST_KEY,
            viewLifecycleOwner,
        ) { _, bundle ->
            val selected = bundle.getStringArrayList(CategoryPickerDialog.EXTRA_SELECTED_CATEGORIES)
            selected?.let {
                profileViewModel.updateFavoriteCategories(it)
            }
        }
    }

    private fun setupInteractions() {
        binding.clProfileRoot.setOnClickListener {
            clearFocusAndHideKeyboard()
        }

        binding.nsvProfile.setOnClickListener {
            clearFocusAndHideKeyboard()
        }

        binding.llEmailContainer.setOnClickListener {
            copyToClipboard(binding.tvEmail.text.toString())
        }

        binding.btnLogout.setOnClickListener {
            profileViewModel.deleteLocalToken()
        }

        binding.tvDevEmail.setOnClickListener {
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
                    getString(CoreR.string.email_failed_to_send),
                    getString(CoreR.string.email_cannot_find_client_app),
                )
            }
        }

        binding.tvDevWebsite.setOnClickListener {
            val devWebsiteAddress = getString(CoreR.string.dev_website)
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(devWebsiteAddress))

            try {
                startActivity(browserIntent)
            } catch (e: android.content.ActivityNotFoundException) {
                Timber.e(e, "Failed to open website: No browser installed")
                Toast.makeText(requireContext(), "No web browser installed.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        binding.swEnableDarkMode.setOnCheckedChangeListener { _, isChecked ->
            profileViewModel.saveIsDarkModeEnable(isChecked)
        }

        binding.swEnableFingerPrint.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!profileViewModel.isNetworkAvailable()) {
                    showAlertDialog(
                        getString(CoreR.string.no_internet_title),
                        getString(CoreR.string.no_internet_message),
                    )
                    binding.swEnableFingerPrint.isChecked = false
                } else {
                    promptFingerPrintSignUp()
                }
            } else {
                profileViewModel.saveIsFingerPrintEnable(isFingerprintEnable = false)
            }
        }

        binding.swEnableNotifications.setOnCheckedChangeListener { _, isChecked ->
            profileViewModel.updateNotificationsEnabled(isChecked)
        }

        binding.swNotifCat.setOnCheckedChangeListener { _, _ ->
            // Sub-settings logic can be added here
        }

        binding.btnAddCat.setOnClickListener {
            val dialog = CategoryPickerDialog.newInstance(profileViewModel.uiState.value.favCategories)
            dialog.show(childFragmentManager, CategoryPickerDialog.TAG)
        }

        binding.btnAddKw.setOnClickListener {
            binding.btnAddKw.isVisible = false
            binding.tilKeyword.isVisible = true
            binding.tietKeyword.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.tietKeyword, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.tietKeyword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && binding.tietKeyword.text.isNullOrEmpty()) {
                binding.tilKeyword.isVisible = false
                binding.btnAddKw.isVisible = true
            }
        }

        binding.tietKeyword.setOnEditorActionListener { _, actionId, event ->
            if (isKeyboardActionDone(actionId, event)) {
                submitKeyword()
                true
            } else {
                false
            }
        }

        binding.tilKeyword.setEndIconOnClickListener {
            submitKeyword()
        }
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
        if (state.isTokenExpired) {
            forceLogout()
            return
        }

        if (binding.swEnableDarkMode.isChecked != state.isDarkModeEnabled) {
            binding.swEnableDarkMode.isChecked = state.isDarkModeEnabled
        }
        if (appliedDarkMode != state.isDarkModeEnabled) {
            appliedDarkMode = state.isDarkModeEnabled
            applyDarkModeToTheApp(state.isDarkModeEnabled)
        }

        if (binding.swEnableFingerPrint.isChecked != state.isFingerprintEnabled) {
            binding.swEnableFingerPrint.isChecked = state.isFingerprintEnabled
        }

        if (binding.swEnableNotifications.isChecked != state.isNotificationsEnabled) {
            binding.swEnableNotifications.isChecked = state.isNotificationsEnabled
        }
        binding.llNotificationSubSettings.isVisible = state.isNotificationsEnabled
        binding.lpSync.isVisible = state.isSyncing

        updateCategoryChips(state.favCategories)
        updateKeywordChips(state.favKeywords)
    }

    private fun updateCategoryChips(categories: List<String>) {
        val childCount = binding.cgFavCats.childCount
        for (i in (childCount - 1) downTo 0) {
            val view = binding.cgFavCats.getChildAt(i)
            if (view.id != R.id.btnAddCat) {
                binding.cgFavCats.removeView(view)
            }
        }

        categories.forEach { cat ->
            val chip = createSelectableChip(cat) {
                val current = profileViewModel.uiState.value.favCategories.toMutableList()
                current.remove(cat)
                profileViewModel.updateFavoriteCategories(current)
            }
            binding.cgFavCats.addView(chip, 0)
        }
    }

    private fun updateKeywordChips(keywords: List<String>) {
        val childCount = binding.cgFavKws.childCount
        for (i in (childCount - 1) downTo 0) {
            val view = binding.cgFavKws.getChildAt(i)
            if (view.id != R.id.btnAddKw) {
                binding.cgFavKws.removeView(view)
            }
        }

        keywords.forEach { kw ->
            val chip = createSelectableChip(kw) {
                profileViewModel.removeFavoriteKeyword(kw)
            }
            binding.cgFavKws.addView(chip, 0)
        }
    }

    private fun createSelectableChip(text: String, onClose: () -> Unit): com.google.android.material.chip.Chip {
        return com.google.android.material.chip.Chip(
            requireContext(),
            null,
            com.google.android.material.R.style.Widget_Material3_Chip_Assist,
        ).apply {
            this.text = text
            isCloseIconVisible = true
            setCloseIconResource(CoreR.drawable.ic_close)
            closeIconSize = 18f * resources.displayMetrics.density
            chipIconTint = android.content.res.ColorStateList.valueOf(
                com.google.android.material.color.MaterialColors.getColor(
                    this,
                    com.google.android.material.R.attr.colorOnSurfaceVariant,
                ),
            )
            closeIconTint = chipIconTint
            setOnCloseIconClickListener {
                onClose()
            }
        }
    }

    private fun submitKeyword() {
        val kw = binding.tietKeyword.text.toString().trim()
        if (kw.isNotEmpty()) {
            profileViewModel.addFavoriteKeyword(kw)
            binding.tietKeyword.text?.clear()
        }
        clearFocusAndHideKeyboard()
    }

    private fun clearFocusAndHideKeyboard() {
        binding.tietKeyword.clearFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.tietKeyword.windowToken, 0)
        binding.clProfileRoot.requestFocus()

        if (binding.tietKeyword.text.isNullOrEmpty()) {
            binding.tilKeyword.isVisible = false
            binding.btnAddKw.isVisible = true
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
                            binding.swEnableFingerPrint.isChecked = false
                            if (result.code == NetworkStatusCode.HTTP_CODE_UNAUTHORIZED) {
                                forceLogout()
                            }
                        }
                    }
                }, {
                    binding.swEnableFingerPrint.isChecked = false
                })
            val promptInfo = BiometricPromptUtils.createPromptInfo(
                getString(CoreR.string.prompt_info_title),
                getString(CoreR.string.prompt_info_description),
                getString(CoreR.string.cancel_button_text)
            )
            val cipher = profileViewModel.generateCypher()
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        } else {
            binding.swEnableFingerPrint.isChecked = false
        }
    }

    private fun isKeyboardActionDone(actionId: Int, event: android.view.KeyEvent?): Boolean {
        val isDone = actionId == EditorInfo.IME_ACTION_DONE
        val isNext = actionId == EditorInfo.IME_ACTION_NEXT
        val isEnter = event?.keyCode == android.view.KeyEvent.KEYCODE_ENTER
        return isDone || isNext || isEnter
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun forceLogout() {
        profileViewModel.clearLocalToken()
        val intent = Intent().apply {
            setClassName(requireContext().packageName, "com.thanh0x.coursedeals.feature.auth.LoginActivity")
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
