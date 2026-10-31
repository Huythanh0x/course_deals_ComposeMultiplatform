package com.thanh0x.coursedeals.convention

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("com.google.devtools.ksp")
            }

            extensions.configure<LibraryExtension> {
                compileSdk = 37
                defaultConfig {
                    minSdk = 24
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
                buildFeatures {
                    dataBinding = false
                    buildConfig = true
                }
            }

            dependencies {
                add("implementation", libs.findLibrary("koin.core").get())
                add("implementation", libs.findLibrary("koin.android").get())
            }
        }
    }
}
