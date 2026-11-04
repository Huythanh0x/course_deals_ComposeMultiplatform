package com.thanh0x.coursedeals.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.thanh0x.coursedeals.core.common.AppVersion
import com.thanh0x.coursedeals.core.logging.CommonTracingInitializer
import com.thanh0x.coursedeals.core.logging.IosLoggingConfigurator
import com.thanh0x.coursedeals.core.logging.LoggerFactory
import com.thanh0x.coursedeals.core.logging.StartupTimeMonitor
import com.thanh0x.coursedeals.core.logging.debug
import com.thanh0x.coursedeals.core.ui.util.PlatformActions
import com.thanh0x.coursedeals.domain.coupons.CouponRepository
import com.thanh0x.coursedeals.feature.auth.AuthMode
import com.thanh0x.coursedeals.shared.watch.WatchDealJsonCodec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatformTools
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSURL
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard
import platform.UIKit.UIViewController

private const val OUTDATED_TEST_VERSION_CODE = 100
private const val MAX_VALID_VERSION_CODE = 10_000
private const val WATCH_BOOKMARKED_COURSE_IDS_KEY = "watch_bookmarked_course_ids"

private val SUPPORTED_DEEP_LINK_PREFIXES = listOf(
    "coursedeals://courses/",
    "coursedeals://coupon/",
    "https://coursedeals.com/deal/",
    "https://coursedeals.com/courses/",
)

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
    StartupTimeMonitor.onAppStart()
    val args = NSProcessInfo.processInfo.arguments.map { it.toString() }
    val versionOverride = parseIosVersionOverride(args)
    AppVersion.overrideVersionCode = versionOverride

    StartupTimeMonitor.trackStep(StartupTimeMonitor.StepName.Logging) {
        IosLoggingConfigurator.install()
        CommonTracingInitializer.initialize(
            distroChannel = "ios",
            appVersion = "1.0.0 (${AppVersion.currentVersionCode()})",
        )
    }
    val logger = LoggerFactory.getLogger("CourseDeals")
    logger.debug { "iOS Application initialized" }
    StartupTimeMonitor.startStep(StartupTimeMonitor.StepName.FirstFrame)

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

    val isForceUpdate = args.any { it.contains("--screen=force-update") } || versionOverride != null
    val deepLinkArg = parseIosDeepLinkArg(args)
    val initialDetailCourseId = deepLinkArg?.let { parseCourseIdFromDeepLink(it) }
    val isOtp = args.any { it.contains("--screen=otp") }
    val isConsent = args.any { it.contains("--screen=consent") }
    val isHome = args.any { it.contains("--screen=home") }
    val isProfile = args.any { it.contains("--screen=profile") }

    val initialScreen = when {
        initialDetailCourseId != null -> AppScreen.Detail(initialDetailCourseId)
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

private fun parseIosDeepLinkArg(args: List<String>): String? {
    val inlineArg = args.firstOrNull {
        it.startsWith("--deeplink=") || it.startsWith("-deeplink=") || it.startsWith("deeplink=")
    }?.substringAfter("=")?.takeIf { it.isNotBlank() }

    val pairArg = args.zipWithNext().firstOrNull { (key, _) ->
        key == "-deeplink" || key == "--deeplink" || key == "deeplink"
    }?.second?.takeIf { it.isNotBlank() }

    val defaultsArg = NSUserDefaults.standardUserDefaults
        .stringForKey("deeplink")
        ?.takeIf { it.isNotBlank() }

    return inlineArg ?: pairArg ?: defaultsArg
}

fun parseCourseIdFromDeepLink(url: String): Int? {
    val trimmed = url.trim()
    val matchingPrefix = SUPPORTED_DEEP_LINK_PREFIXES.firstOrNull { trimmed.startsWith(it) }
        ?: return null
    val pathPart = trimmed
        .removePrefix(matchingPrefix)
        .substringBefore("?")
        .substringBefore("#")
        .substringBefore("/")
    return pathPart.toIntOrNull()?.takeIf { it > 0 }
}

fun handleDeepLink(url: String) {
    val courseId = parseCourseIdFromDeepLink(url)
    if (courseId != null) {
        PlatformActions.navigateToDetail(courseId)
    }
}

fun handleHandoffCourse(courseId: Int) {
    if (courseId > 0) {
        PlatformActions.navigateToDetail(courseId)
    }
}

fun syncWatchBookmark(courseId: Int, isBookmarked: Boolean) {
    if (courseId <= 0) return
    val defaults = NSUserDefaults.standardUserDefaults
    val updatedIds = readWatchBookmarkedCourseIds(defaults)
    if (isBookmarked) {
        updatedIds.add(courseId)
    } else {
        updatedIds.remove(courseId)
    }
    defaults.setObject(updatedIds.sorted().joinToString(","), forKey = WATCH_BOOKMARKED_COURSE_IDS_KEY)

    if (isBookmarked) {
        CoroutineScope(Dispatchers.Default).launch {
            val repo = KoinPlatformTools.defaultContext().getOrNull()?.getOrNull<CouponRepository>()
            repo?.fetchCouponDetail(courseId)
        }
    }
}

fun isWatchCourseBookmarked(courseId: Int): Boolean {
    if (courseId <= 0) return false
    return courseId in readWatchBookmarkedCourseIds(NSUserDefaults.standardUserDefaults)
}

fun buildWatchHotDealsUrl(preference: String?): String =
    WatchDealJsonCodec.buildHotDealsUrl(preference = preference)

fun mapHotDealsJsonForWatch(apiJson: String, bookmarkedIdsCsv: String): String {
    val bookmarkedIds = bookmarkedIdsCsv
        .split(",")
        .mapNotNull { it.trim().toIntOrNull() }
        .filter { it > 0 }
        .toSet() + readWatchBookmarkedCourseIds(NSUserDefaults.standardUserDefaults)
    return WatchDealJsonCodec.mapHotDealsApiJsonToWatchDealsJson(
        apiJson = apiJson,
        bookmarkedIds = bookmarkedIds,
    )
}

private fun readWatchBookmarkedCourseIds(defaults: NSUserDefaults): MutableSet<Int> {
    val result = mutableSetOf<Int>()
    defaults.stringForKey(WATCH_BOOKMARKED_COURSE_IDS_KEY)
        ?.split(",")
        ?.mapNotNull { it.trim().toIntOrNull() }
        ?.filter { it > 0 }
        ?.let { result.addAll(it) }
    defaults.arrayForKey(WATCH_BOOKMARKED_COURSE_IDS_KEY)
        ?.mapNotNull { it?.toString()?.trim()?.toIntOrNull() }
        ?.filter { it > 0 }
        ?.let { result.addAll(it) }
    return result
}
