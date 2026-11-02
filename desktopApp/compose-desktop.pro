# Keep main entry point
-keepclasseswithmembers public class com.thanh0x.coursedeals.desktop.MainKt {
    public static void main(java.lang.String[]);
}

# Keep Kotlin runtime & reflection
-keep class kotlin.** { *; }
-dontwarn kotlin.**
-dontwarn java.lang.invoke.**
-dontnote **

# Line numbers for debugging
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Compose, Skiko, and Skia graphics engine
-keep class androidx.compose.** { *; }
-keep class org.jetbrains.compose.** { *; }
-keep class org.jetbrains.skiko.** { *; }
-keep class org.jetbrains.skia.** { *; }

# Keep application classes and models
-keep class com.thanh0x.coursedeals.** { *; }

# Keep Koin DI
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Keep Ktor and OkHttp Networking
-keep class io.ktor.** { *; }
-keep class okhttp3.** { *; }
-dontwarn io.ktor.**
-dontwarn okhttp3.**

# Keep Kotlin Coroutines & Serialization
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class * implements kotlinx.serialization.KSerializer { *; }
-dontwarn kotlinx.coroutines.**
-dontwarn kotlinx.serialization.**
