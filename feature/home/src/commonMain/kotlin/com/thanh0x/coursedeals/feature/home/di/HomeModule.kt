package com.thanh0x.coursedeals.feature.home.di

import com.thanh0x.coursedeals.feature.home.HomeViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val homeModule = module {
    factoryOf(::HomeViewModel)
}
