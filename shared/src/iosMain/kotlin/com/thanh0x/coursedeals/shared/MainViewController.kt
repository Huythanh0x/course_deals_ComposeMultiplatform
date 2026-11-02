package com.thanh0x.coursedeals.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.thanh0x.coursedeals.core.common.AppVersion
import com.thanh0x.coursedeals.core.logging.IosLoggingConfigurator
import com.thanh0x.coursedeals.core.logging.LoggerFactory
import com.thanh0x.coursedeals.core.logging.debug
import com.thanh0x.coursedeals.core.ui.util.PlatformActions
import com.thanh0x.coursedeals.feature.auth.AuthMode
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSURL
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard
import platform.UIKit.UIViewController

private const val OUTDATED_TEST_VERSION_CODE = 100
private const val MAX_VALID_VERSION_CODE = 10_000

private fun parseIosVersionOverride(args: List<String>): Int? {
    if (args.any { it.contains("--screen=force-update") }) {
        return OUTDATED_TEST_VERSION_CODE
    }
    args.firstOrNull { it.startsWith("--app-version=") }
        ?.substringAfter("--app-version=")
        ?.toIntOrNull()
        ?.takeIf { it in 1..MAX_VALID_VERSION_CODE }
        ?.let { return it }

    args.firstOrNull { it.startsWith("--override-version-code=") || it.startsWith("override_version_code=") }
        ?.substringAfter("=")
        ?.toIntOrNull()
        ?.takeIf { it in 1..MAX_VALID_VERSION_CODE }
        ?.let { return it }

    args.zipWithNext().firstOrNull { (key, _) ->
        key == "-override_version_code" || key == "--override_version_code" || key == "override_version_code"
    }?.second?.toIntOrNull()?.takeIf { it in 1..MAX_VALID_VERSION_CODE }?.let { return it }

    return NSUserDefaults.standardUserDefaults
        .objectForKey("override_version_code")
        ?.toString()
        ?.toIntOrNull()
        ?.takeIf { it in 1..MAX_VALID_VERSION_CODE }
}

fun MainViewController(): UIViewController {
    IosLoggingConfigurator.install()
    val logger = LoggerFactory.getLogger("CourseDeals")
    logger.debug { "iOS Application initialized" }

    lateinit var composeViewController: UIViewController

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
        val activityController = UIActivityViewController(
            activityItems = listOf("$title\n$url"),
            applicationActivities = null
        )
        composeViewController.presentViewController(activityController, animated = true, completion = null)
    }

    val args = NSProcessInfo.processInfo.arguments.map { it.toString() }
    val versionOverride = parseIosVersionOverride(args)
    AppVersion.overrideVersionCode = versionOverride

    val isForceUpdate = args.any { it.contains("--screen=force-update") } || versionOverride != null
    val isOtp = args.any { it.contains("--screen=otp") }
    val isConsent = args.any { it.contains("--screen=consent") }
    val isHome = args.any { it.contains("--screen=home") }
    val isProfile = args.any { it.contains("--screen=profile") }

    val initialScreen = when {
        isForceUpdate -> AppScreen.Splash
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

    composeViewController = ComposeUIViewController {
        App(
            initialScreen = initialScreen,
            initialRoute = initialRoute,
            initialAuthMode = initialAuthMode,
            initialPendingEmail = if (isOtp || isConsent) "brandnewuser@test.com" else "",
            initialShowTermsDialog = isConsent,
        )
    }
    return composeViewController
}
