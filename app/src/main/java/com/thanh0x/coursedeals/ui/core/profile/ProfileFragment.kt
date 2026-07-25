package com.batdaulaptrinh.freeudemycoupons.ui.core.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.batdaulaptrinh.freeudemycoupons.MainActivity
import com.batdaulaptrinh.freeudemycoupons.R
import com.batdaulaptrinh.freeudemycoupons.data.model.TokenResponseData
import com.batdaulaptrinh.freeudemycoupons.databinding.FragmentProfileBinding
import com.batdaulaptrinh.freeudemycoupons.domain.logic.fingerprint.BiometricPromptUtils
import com.batdaulaptrinh.freeudemycoupons.ui.login.LoginActivity
import com.batdaulaptrinh.freeudemycoupons.util.NetworkStatusCode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    var _binding: FragmentProfileBinding? = null
    val binding get() = _binding!!
    private val profileViewModel: ProfileViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        profileViewModel.checkIfTokenExpired()
        binding.btnLogout.setOnClickListener {
            profileViewModel.deleteLocalToken()
            forceLogout()
        }
        profileViewModel.tokenExpired.observe(requireActivity()) {
            if (it) {
                forceLogout()
            }
        }
        profileViewModel.isDarkModeEnable.observe(viewLifecycleOwner) {
            binding.swEnableDarkMode.isChecked = it
            applyDarkModeToTheApp(it)
        }
        var initFingerprintValue = true
        profileViewModel.isFingerPrintEnable.observe(viewLifecycleOwner) {
            binding.swEnableFingerPrint.isChecked = it
            initFingerprintValue = false
        }
        binding.swEnableDarkMode.setOnCheckedChangeListener { _, isChecked ->
            profileViewModel.checkIfTokenExpired()
            profileViewModel.isDarkModeEnable.postValue(isChecked)
            profileViewModel.saveIsDarkModeEnable(isChecked)
        }
        binding.swEnableFingerPrint.setOnCheckedChangeListener { _, isChecked ->
            profileViewModel.checkIfTokenExpired()
            if (!initFingerprintValue) {
                if (!profileViewModel.isNetworkAvailable()) {
                    showNoInternetConnectionDialog(
                        resources.getString(R.string.no_internet_title),
                        resources.getString(R.string.no_internet_message)
                    )
                }
                if (isChecked) {
                    promptFingerPrintSignUp()
                } else {
                    profileViewModel.isFingerPrintEnable.postValue(false)
                }

            }
            profileViewModel.saveIsFingerPrintEnable(isChecked)
        }
        return binding.root
    }

    private fun promptFingerPrintSignUp() {
        val canAuthenticate = BiometricManager.from(requireContext()).canAuthenticate()
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(requireActivity() as MainActivity, {
                    lifecycleScope.launch {
                        val fingerprintTokenResult = profileViewModel.requestFingerprintToken()
                        if (fingerprintTokenResult.isSuccessful && fingerprintTokenResult.body() is TokenResponseData) {
                            val cipherTextWrapper =
                                profileViewModel.encryptedServerTokenWrapper(
                                    it,
                                    fingerprintTokenResult.body()!!.accessToken
                                )
                            Log.d("FINGERPRINT TOKEN", fingerprintTokenResult.body()!!.accessToken)
                            if (cipherTextWrapper == null) {
                                Log.d("FINGERPRINT TOKEN", "cipherTextWrapper == null")
                            } else {
                                showToast("Add fingerprint successfully")
                                profileViewModel.saveCipherTextWrapper(cipherTextWrapper)
                                profileViewModel.isFingerPrintEnable.postValue(true)
                            }
                        } else if (fingerprintTokenResult.code() == NetworkStatusCode.HTTP_CODE_UNAUTHORIZED) {
                            Log.d("REGISTER FINGERPRINT", fingerprintTokenResult.code().toString())
                            setFingerprintSwitchBackToOff()
                            forceLogout()
                        } else {
                            Log.d("REGISTER FINGERPRINT", fingerprintTokenResult.code().toString())
                            setFingerprintSwitchBackToOff()
                        }
                    }
                }, {
                    setFingerprintSwitchBackToOff()
                })
            val promptInfo =
                BiometricPromptUtils.createPromptInfo(requireActivity() as MainActivity)
            val cipher = profileViewModel.generateCypher()
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun forceLogout() {
        try {
            profileViewModel.clearLocalToken()
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        } catch (_: Exception) {
        }
    }

    private fun applyDarkModeToTheApp(isDarkModeEnable: Boolean) {
        if (isDarkModeEnable) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        (activity as AppCompatActivity).delegate.applyDayNight()
    }

    private fun showNoInternetConnectionDialog(title: String, message: String) {
        val loginDialog = MaterialAlertDialogBuilder(
            requireContext(),
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        ).setTitle(title).setMessage(message)
            .setPositiveButton(R.string.ok_text_button) { dialog, _ ->
                dialog.dismiss()
            }.create()
        loginDialog.show()
    }

    private fun setFingerprintSwitchBackToOff() {
        binding.swEnableFingerPrint.isChecked = false
    }
}
