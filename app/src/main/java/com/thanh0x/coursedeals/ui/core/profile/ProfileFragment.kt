package com.thanh0x.coursedeals.ui.core.profile

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
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.databinding.FragmentProfileBinding
import com.thanh0x.coursedeals.domain.logic.fingerprint.BiometricPromptUtils
import com.thanh0x.coursedeals.domain.model.AppResult
import com.thanh0x.coursedeals.ui.base.BaseFragment
import com.thanh0x.coursedeals.ui.login.LoginActivity
import com.thanh0x.coursedeals.util.NetworkStatusCode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : BaseFragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val profileViewModel: ProfileViewModel by viewModels()

    private val selectedCategories = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInteractions()
        setupObservers()
        profileViewModel.checkIfTokenExpired()
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
            forceLogout()
        }

        binding.tvDevEmail.setOnClickListener {
            val devEmailAddress = getString(R.string.dev_email)
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$devEmailAddress")
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_send_title))
            }

            try {
                startActivity(emailIntent)
            } catch (e: android.content.ActivityNotFoundException) {
                showAlertDialog(
                    getString(R.string.email_failed_to_send),
                    getString(R.string.email_cannot_find_client_app)
                )
            }
        }

        binding.tvDevWebsite.setOnClickListener {
            val devWebsiteAddress = getString(R.string.dev_website)
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(devWebsiteAddress))

            try {
                startActivity(browserIntent)
            } catch (e: android.content.ActivityNotFoundException) {
                Toast.makeText(requireContext(), "No web browser installed.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        binding.swEnableDarkMode.setOnCheckedChangeListener { _, isChecked ->
            profileViewModel.checkIfTokenExpired()
            profileViewModel.isDarkModeEnable.postValue(isChecked)
            profileViewModel.saveIsDarkModeEnable(isChecked)
        }

        binding.swEnableFingerPrint.setOnCheckedChangeListener { _, isChecked ->
            profileViewModel.checkIfTokenExpired()
            if (isChecked) {
                if (!profileViewModel.isNetworkAvailable()) {
                    showAlertDialog(
                        getString(R.string.no_internet_title),
                        getString(R.string.no_internet_message)
                    )
                    binding.swEnableFingerPrint.isChecked = false
                } else {
                    promptFingerPrintSignUp()
                }
            } else {
                profileViewModel.isFingerPrintEnable.postValue(false)
                profileViewModel.saveIsFingerPrintEnable(false)
            }
        }

        binding.swEnableNotifications.setOnCheckedChangeListener { _, isChecked ->
            binding.llNotificationSubSettings.isVisible = isChecked
        }

        binding.btnAddCat.setOnClickListener {
            val dialog = CategoryPickerDialog(selectedCategories) { selected ->
                selectedCategories.clear()
                selectedCategories.addAll(selected)
                updateCategoryChips()
            }
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
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_NEXT ||
                (event != null && event.keyCode == android.view.KeyEvent.KEYCODE_ENTER)) {
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

    private fun updateCategoryChips() {
        // Remove all except the add button
        val childCount = binding.cgFavCats.childCount
        for (i in childCount - 1 downTo 0) {
            val view = binding.cgFavCats.getChildAt(i)
            if (view.id != R.id.btnAddCat) {
                binding.cgFavCats.removeView(view)
            }
        }

        // Add new chips before the add button
        selectedCategories.forEach { cat ->
            val chip = com.google.android.material.chip.Chip(
                requireContext(),
                null,
                com.google.android.material.R.style.Widget_Material3_Chip_Assist
            ).apply {
                this.text = cat
                isCloseIconVisible = true
                setCloseIconResource(R.drawable.ic_close)
                closeIconSize = 18f * resources.displayMetrics.density
                chipIconTint = android.content.res.ColorStateList.valueOf(
                    com.google.android.material.color.MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant)
                )
                closeIconTint = chipIconTint
                setOnCloseIconClickListener {
                    selectedCategories.remove(cat)
                    binding.cgFavCats.removeView(this)
                }
            }
            binding.cgFavCats.addView(chip, 0)
        }
    }

    private fun submitKeyword() {
        val kw = binding.tietKeyword.text.toString().trim()
        if (kw.isNotEmpty()) {
            addKeywordChip(kw)
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

    private fun setupObservers() {
        profileViewModel.tokenExpired.observe(viewLifecycleOwner) {
            if (it) forceLogout()
        }

        profileViewModel.isDarkModeEnable.observe(viewLifecycleOwner) {
            binding.swEnableDarkMode.isChecked = it
            applyDarkModeToTheApp(it)
        }

        profileViewModel.isFingerPrintEnable.observe(viewLifecycleOwner) {
            binding.swEnableFingerPrint.isChecked = it
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Course Deal Email", text)
        clipboard.setPrimaryClip(clip)
        showToast("Email copied to clipboard")
    }

    private fun addKeywordChip(text: String) {
        val chip = com.google.android.material.chip.Chip(
            requireContext(),
            null,
            com.google.android.material.R.style.Widget_Material3_Chip_Assist
        ).apply {
            this.text = text
            isCloseIconVisible = true
            setCloseIconResource(R.drawable.ic_close)
            closeIconSize = 18f * resources.displayMetrics.density
            chipIconTint = android.content.res.ColorStateList.valueOf(
                com.google.android.material.color.MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant)
            )
            closeIconTint = chipIconTint
            setOnCloseIconClickListener {
                binding.cgFavKws.removeView(this)
            }
        }
        binding.cgFavKws.addView(chip, 0)
    }

    private fun promptFingerPrintSignUp() {
        val canAuthenticate = BiometricManager.from(requireContext()).canAuthenticate()
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(requireActivity() as AppCompatActivity, {
                    lifecycleScope.launch {
                        val result = profileViewModel.requestFingerprintToken()
                        when (result) {
                            is AppResult.Success -> {
                                val cipherTextWrapper =
                                    profileViewModel.encryptedServerTokenWrapper(
                                        it,
                                        result.data.accessToken
                                    )
                                if (cipherTextWrapper != null) {
                                    showToast("Add fingerprint successfully")
                                    profileViewModel.saveCipherTextWrapper(cipherTextWrapper)
                                    profileViewModel.isFingerPrintEnable.postValue(true)
                                    profileViewModel.saveIsFingerPrintEnable(true)
                                }
                            }
                            is AppResult.Error -> {
                                binding.swEnableFingerPrint.isChecked = false
                                if (result.code == NetworkStatusCode.HTTP_CODE_UNAUTHORIZED) {
                                    forceLogout()
                                }
                            }
                            is AppResult.Loading -> { /* Handle loading */ }
                        }
                    }
                }, {
                    binding.swEnableFingerPrint.isChecked = false
                })
            val promptInfo = BiometricPromptUtils.createPromptInfo(requireActivity() as AppCompatActivity)
            val cipher = profileViewModel.generateCypher()
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        } else {
            binding.swEnableFingerPrint.isChecked = false
        }
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
        startActivity(Intent(requireActivity(), LoginActivity::class.java))
        requireActivity().finish()
    }

    private fun applyDarkModeToTheApp(isDarkModeEnable: Boolean) {
        val mode = if (isDarkModeEnable) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
