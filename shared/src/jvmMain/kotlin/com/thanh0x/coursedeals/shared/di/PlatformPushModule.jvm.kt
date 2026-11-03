package com.thanh0x.coursedeals.shared.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformPushModule: Module = module {
    // In Android runtime, PushNotificationManager is provided by appModule in :app
}
