package com.thanh0x.coursedeals

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.thanh0x.coursedeals.di.appModules
import com.thanh0x.coursedeals.domain.user.UserProfileRepository
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class MyApplication : Application() {

    private val userProfileRepository: UserProfileRepository by inject()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MyApplication)
            modules(appModules)
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
