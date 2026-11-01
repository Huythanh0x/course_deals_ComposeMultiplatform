package com.thanh0x.coursedeals.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.thanh0x.coursedeals.core.ui.util.PlatformActions
import com.thanh0x.coursedeals.feature.auth.AuthMode
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    PlatformActions.onCopyToClipboard = { text ->
        UIPasteboard.generalPasteboard.string = text
    }
    PlatformActions.onOpenUrl = { urlString ->
        NSURL.URLWithString(urlString)?.let { nsUrl ->
            UIApplication.sharedApplication.openURL(
                url = nsUrl,
                options = emptyMap<Any?, Any>(),
                completionHandler = null,
            )
        }
    }
    PlatformActions.onSendEmail = { email, subject ->
        val encodedSubject = subject.replace(" ", "%20")
        val mailUrl = NSURL.URLWithString("mailto:$email?subject=$encodedSubject")
        if (mailUrl != null && UIApplication.sharedApplication.canOpenURL(mailUrl)) {
            UIApplication.sharedApplication.openURL(
                url = mailUrl,
                options = emptyMap<Any?, Any>(),
                completionHandler = null,
            )
            true
        } else {
            false
        }
    }
    PlatformActions.onShareLink = { title, url ->
        UIPasteboard.generalPasteboard.string = "$title - $url"
    }

    val args = NSProcessInfo.processInfo.arguments.map { it.toString() }
    val isOtp = args.any { it.contains("--screen=otp") }
    val isConsent = args.any { it.contains("--screen=consent") }
    val isHome = args.any { it.contains("--screen=home") }
    val isProfile = args.any { it.contains("--screen=profile") }

    val initialScreen = when {
        isHome || isProfile -> AppScreen.Main
        else -> AppScreen.Login
    }
    val initialRoute = when {
        isProfile -> "profile"
        else -> "home"
    }
    val initialAuthMode = when {
        isOtp || isConsent -> AuthMode.VERIFY_OTP
        else -> AuthMode.LOGIN
    }

    return ComposeUIViewController {
        App(
            initialScreen = initialScreen,
            initialRoute = initialRoute,
            initialAuthMode = initialAuthMode,
            initialPendingEmail = if (isOtp || isConsent) "brandnewuser@test.com" else "",
            initialShowTermsDialog = isConsent,
        )
    }
}
