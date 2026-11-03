package com.thanh0x.coursedeals.convention

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("io.gitlab.arturbosch.detekt")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                applyDefaultHierarchyTemplate()

                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }

                jvm {
                    compilerOptions {
                        jvmTarget.set(JvmTarget.JVM_17)
                    }
                }

                iosArm64()
                iosSimulatorArm64()
            }

            extensions.configure<DetektExtension> {
                toolVersion = libs.findVersion("detekt").get().toString()
                config.setFrom(rootProject.file("config/detekt/detekt.yml"))
                buildUponDefaultConfig = true
                allRules = false
                source.setFrom(
                    files(
                        "src/commonMain/kotlin",
                        "src/jvmMain/kotlin",
                        "src/iosMain/kotlin",
                        "src/androidMain/kotlin",
                        "src/main/java",
                        "src/main/kotlin",
                    )
                )
            }
        }
    }
}
