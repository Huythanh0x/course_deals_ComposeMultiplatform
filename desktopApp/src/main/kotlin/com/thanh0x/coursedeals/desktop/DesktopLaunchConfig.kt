package com.thanh0x.coursedeals.desktop

import com.thanh0x.coursedeals.feature.auth.AuthMode
import com.thanh0x.coursedeals.shared.AppScreen

const val OUTDATED_TEST_VERSION_CODE = 100
const val MAX_VALID_VERSION_CODE = 10_000
const val DEFAULT_CAPTURE_DELAY_MS = 3500L
const val MIN_CAPTURE_DELAY_MS = 100L
const val MAX_CAPTURE_DELAY_MS = 60_000L
const val DEFAULT_TEST_OTP_EMAIL = "brandnewuser@test.com"

data class DesktopLaunchConfig(
    val initialScreen: AppScreen = AppScreen.Login,
    val initialRoute: String = "home",
    val initialAuthMode: AuthMode = AuthMode.LOGIN,
    val initialPendingEmail: String = "",
    val initialShowTermsDialog: Boolean = false,
    val versionOverride: Int? = null,
    val captureScreenshotPath: String? = null,
    val captureDelayMs: Long = DEFAULT_CAPTURE_DELAY_MS,
)

fun resolveCombinedDesktopArgs(
    cliArgs: List<String>,
    systemPropArgs: String? = System.getProperty("coursedeals.args"),
): List<String> {
    val extraArgs = systemPropArgs
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.split(Regex("\\s+"))
        .orEmpty()
    return cliArgs + extraArgs
}

fun parseDesktopArgs(args: List<String>): DesktopLaunchConfig {
    val screenArg = args
        .firstOrNull { it.startsWith("--screen=") }
        ?.substringAfter("--screen=")
        ?.trim()
        ?.lowercase()

    val isForceUpdate = screenArg == "force-update"
    val isSplash = screenArg == "splash"
    val isOtp = screenArg == "otp"
    val isConsent = screenArg == "consent"
    val isHome = screenArg == "home"
    val isCourse = screenArg == "course"
    val isProfile = screenArg == "profile"

    val explicitVersionOverride = parseExplicitVersionOverride(args)
    val versionOverride = explicitVersionOverride ?: if (isForceUpdate) OUTDATED_TEST_VERSION_CODE else null

    val initialScreen = when {
        isForceUpdate || isSplash || versionOverride != null -> AppScreen.Splash
        isHome || isCourse || isProfile -> AppScreen.Main
        else -> AppScreen.Login
    }

    val initialRoute = when {
        isProfile -> "profile"
        isCourse -> "course"
        else -> "home"
    }

    val initialAuthMode = when {
        isOtp || isConsent -> AuthMode.VERIFY_OTP
        else -> AuthMode.LOGIN
    }

    val captureScreenshotPath = args
        .firstOrNull { it.startsWith("--capture-screenshot=") }
        ?.substringAfter("--capture-screenshot=")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

    val captureDelayMs = args
        .firstOrNull { it.startsWith("--capture-delay-ms=") }
        ?.substringAfter("--capture-delay-ms=")
        ?.trim()
        ?.toLongOrNull()
        ?.takeIf { it in MIN_CAPTURE_DELAY_MS..MAX_CAPTURE_DELAY_MS }
        ?: DEFAULT_CAPTURE_DELAY_MS

    return DesktopLaunchConfig(
        initialScreen = initialScreen,
        initialRoute = initialRoute,
        initialAuthMode = initialAuthMode,
        initialPendingEmail = if (isOtp || isConsent) DEFAULT_TEST_OTP_EMAIL else "",
        initialShowTermsDialog = isConsent,
        versionOverride = versionOverride,
        captureScreenshotPath = captureScreenshotPath,
        captureDelayMs = captureDelayMs,
    )
}

private fun parseExplicitVersionOverride(args: List<String>): Int? {
    val inlineOverride = args
        .firstOrNull {
            it.startsWith("--app-version=") ||
                it.startsWith("--override-version-code=") ||
                it.startsWith("override_version_code=")
        }
        ?.substringAfter("=")
        ?.trim()
        ?.toIntOrNull()
        ?.takeIf { it in 1..MAX_VALID_VERSION_CODE }

    if (inlineOverride != null) {
        return inlineOverride
    }

    return args
        .zipWithNext()
        .firstOrNull { (key, _) ->
            key == "-override_version_code" ||
                key == "--override_version_code" ||
                key == "override_version_code"
        }
        ?.second
        ?.trim()
        ?.toIntOrNull()
        ?.takeIf { it in 1..MAX_VALID_VERSION_CODE }
}
