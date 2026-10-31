package com.thanh0x.coursedeals.feature.auth.di

import com.thanh0x.coursedeals.feature.auth.LoginViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    viewModelOf(::LoginViewModel)
}
