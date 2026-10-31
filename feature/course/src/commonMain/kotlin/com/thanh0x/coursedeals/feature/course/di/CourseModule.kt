package com.thanh0x.coursedeals.feature.course.di

import com.thanh0x.coursedeals.feature.course.CourseViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val courseModule = module {
    factoryOf(::CourseViewModel)
}
