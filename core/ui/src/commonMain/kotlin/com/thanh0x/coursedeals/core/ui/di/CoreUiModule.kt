package com.thanh0x.coursedeals.core.ui.di

import com.thanh0x.coursedeals.core.ui.util.NetworkUtil
import org.koin.dsl.module

val coreUiModule = module {
    single { NetworkUtil() }
}
