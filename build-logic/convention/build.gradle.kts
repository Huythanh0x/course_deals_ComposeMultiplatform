plugins {
    `kotlin-dsl`
}

group = "com.thanh0x.coursedeals.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "coursedeals.android.library"
            implementationClass = "com.thanh0x.coursedeals.convention.AndroidLibraryConventionPlugin"
        }
        register("kotlinLibrary") {
            id = "coursedeals.kotlin.library"
            implementationClass = "com.thanh0x.coursedeals.convention.KotlinLibraryConventionPlugin"
        }
    }
}
