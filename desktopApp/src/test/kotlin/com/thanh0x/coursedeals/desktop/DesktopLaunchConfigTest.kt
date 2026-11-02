package com.thanh0x.coursedeals.desktop

import com.thanh0x.coursedeals.feature.auth.AuthMode
import com.thanh0x.coursedeals.shared.AppScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DesktopLaunchConfigTest {

    @Test
    fun `parseDesktopArgs returns Login screen with default route when args are empty`() {
        val config = parseDesktopArgs(emptyList())

        assertEquals(AppScreen.Login, config.initialScreen)
        assertEquals("home", config.initialRoute)
        assertEquals(AuthMode.LOGIN, config.initialAuthMode)
        assertEquals("", config.initialPendingEmail)
        assertFalse(config.initialShowTermsDialog)
        assertNull(config.versionOverride)
        assertNull(config.captureScreenshotPath)
        assertEquals(DEFAULT_CAPTURE_DELAY_MS, config.captureDelayMs)
    }

    @Test
    fun `parseDesktopArgs maps home course and profile screens to Main scaffold routes`() {
        val homeConfig = parseDesktopArgs(listOf("--screen=home"))
        assertEquals(AppScreen.Main, homeConfig.initialScreen)
        assertEquals("home", homeConfig.initialRoute)

        val courseConfig = parseDesktopArgs(listOf("--screen=course"))
        assertEquals(AppScreen.Main, courseConfig.initialScreen)
        assertEquals("course", courseConfig.initialRoute)

        val profileConfig = parseDesktopArgs(listOf("--screen=profile"))
        assertEquals(AppScreen.Main, profileConfig.initialScreen)
        assertEquals("profile", profileConfig.initialRoute)

        val enrollDefaultConfig = parseDesktopArgs(listOf("--screen=enroll"))
        assertEquals(
            AppScreen.Enroll(couponUrl = DEFAULT_TEST_ENROLL_URL, previousCourseId = null),
            enrollDefaultConfig.initialScreen,
        )

        val customEnrollUrl = "https://www.udemy.com/course/compose-multiplatform/?couponCode=FREE100"
        val enrollCustomConfig = parseDesktopArgs(listOf("--screen=enroll", "--enroll-url=$customEnrollUrl"))
        assertEquals(
            AppScreen.Enroll(couponUrl = customEnrollUrl, previousCourseId = null),
            enrollCustomConfig.initialScreen,
        )
    }

    @Test
    fun `parseDesktopArgs maps otp and consent screens to OTP verification mode`() {
        val otpConfig = parseDesktopArgs(listOf("--screen=otp"))
        assertEquals(AppScreen.Login, otpConfig.initialScreen)
        assertEquals(AuthMode.VERIFY_OTP, otpConfig.initialAuthMode)
        assertEquals(DEFAULT_TEST_OTP_EMAIL, otpConfig.initialPendingEmail)
        assertFalse(otpConfig.initialShowTermsDialog)

        val consentConfig = parseDesktopArgs(listOf("--screen=consent"))
        assertEquals(AppScreen.Login, consentConfig.initialScreen)
        assertEquals(AuthMode.VERIFY_OTP, consentConfig.initialAuthMode)
        assertEquals(DEFAULT_TEST_OTP_EMAIL, consentConfig.initialPendingEmail)
        assertTrue(consentConfig.initialShowTermsDialog)
    }

    @Test
    fun `parseDesktopArgs maps force-update to Splash screen with outdated version override`() {
        val forceUpdateConfig = parseDesktopArgs(listOf("--screen=force-update"))

        assertEquals(AppScreen.Splash, forceUpdateConfig.initialScreen)
        assertEquals(OUTDATED_TEST_VERSION_CODE, forceUpdateConfig.versionOverride)
    }

    @Test
    fun `parseDesktopArgs accepts valid version overrides and ignores malformed or out-of-range values`() {
        val explicitVersionConfig = parseDesktopArgs(listOf("--app-version=104"))
        assertEquals(AppScreen.Splash, explicitVersionConfig.initialScreen)
        assertEquals(104, explicitVersionConfig.versionOverride)

        val pairVersionConfig = parseDesktopArgs(listOf("-override_version_code", "102"))
        assertEquals(AppScreen.Splash, pairVersionConfig.initialScreen)
        assertEquals(102, pairVersionConfig.versionOverride)

        val nonNumericConfig = parseDesktopArgs(listOf("--app-version=abc"))
        assertEquals(AppScreen.Login, nonNumericConfig.initialScreen)
        assertNull(nonNumericConfig.versionOverride)

        val outOfRangeConfig = parseDesktopArgs(listOf("--app-version=999999"))
        assertEquals(AppScreen.Login, outOfRangeConfig.initialScreen)
        assertNull(outOfRangeConfig.versionOverride)
    }

    @Test
    fun `parseDesktopArgs parses screenshot capture path and bounded delay`() {
        val screenshotConfig = parseDesktopArgs(
            listOf(
                "--screen=home",
                "--capture-screenshot=/tmp/desktop_home.png",
                "--capture-delay-ms=2000",
            )
        )

        assertEquals(AppScreen.Main, screenshotConfig.initialScreen)
        assertEquals("/tmp/desktop_home.png", screenshotConfig.captureScreenshotPath)
        assertEquals(2000L, screenshotConfig.captureDelayMs)

        val invalidDelayConfig = parseDesktopArgs(
            listOf(
                "--capture-screenshot=/tmp/out.png",
                "--capture-delay-ms=-50",
            )
        )
        assertEquals("/tmp/out.png", invalidDelayConfig.captureScreenshotPath)
        assertEquals(DEFAULT_CAPTURE_DELAY_MS, invalidDelayConfig.captureDelayMs)
    }

    @Test
    fun `resolveCombinedDesktopArgs merges CLI arguments and coursedeals system property`() {
        val combined = resolveCombinedDesktopArgs(
            cliArgs = listOf("--screen=home"),
            systemPropArgs = "--capture-screenshot=/tmp/home.png --capture-delay-ms=1500",
        )

        assertEquals(
            listOf("--screen=home", "--capture-screenshot=/tmp/home.png", "--capture-delay-ms=1500"),
            combined,
        )
    }
}
