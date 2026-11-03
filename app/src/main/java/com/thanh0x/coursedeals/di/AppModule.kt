package com.thanh0x.coursedeals.di

import com.thanh0x.coursedeals.core.common.notification.PushNotificationManager
import com.thanh0x.coursedeals.fcm.AndroidPushNotificationManager
import com.thanh0x.coursedeals.shared.sharedKoinModules
import org.koin.dsl.module

val appModule = module {
    single<PushNotificationManager> {
        AndroidPushNotificationManager(deviceTokenRepository = get())
    }
}

val appModules = sharedKoinModules + appModule
