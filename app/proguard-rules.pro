# Keep line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclassmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Domain & Data DTO Models
-keep class com.thanh0x.coursedeals.domain.** { *; }
-keep class com.thanh0x.coursedeals.data.** { *; }
-keep class com.thanh0x.coursedeals.core.network.** { *; }

# Ktor Client
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Koin DI
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Coil Image Loader
-keep class coil.** { *; }
-keep class coil3.** { *; }
-dontwarn coil.**
-dontwarn coil3.**