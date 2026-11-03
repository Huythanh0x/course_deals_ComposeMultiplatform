package com.thanh0x.coursedeals.shared.di

import com.thanh0x.coursedeals.core.common.notification.PushNotificationManager
import com.thanh0x.coursedeals.shared.IosPushNotificationManager
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformPushModule: Module = module {
    single<PushNotificationManager> {
        IosPushNotificationManager(deviceTokenRepository = get())
    }
}
