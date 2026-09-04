package com.thanh0x.coursedeals

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.thanh0x.coursedeals.domain.user.UserProfileRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {

    @Inject
    lateinit var userProfileRepository: UserProfileRepository

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        applyPersistedDarkMode()
    }

    private fun applyPersistedDarkMode() {
        val isDarkModeEnabled = runBlocking { userProfileRepository.getEnableDarkMode() } ?: false
        val mode = if (isDarkModeEnabled) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
