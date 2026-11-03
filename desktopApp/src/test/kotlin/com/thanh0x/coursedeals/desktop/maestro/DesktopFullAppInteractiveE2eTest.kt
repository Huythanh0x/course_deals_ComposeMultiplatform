package com.thanh0x.coursedeals.desktop.maestro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.thanh0x.coursedeals.core.common.AppVersion
import com.thanh0x.coursedeals.desktop.TAG_WEBVIEW_ADDRESS_BAR
import com.thanh0x.coursedeals.desktop.TAG_WEBVIEW_OPEN_EXTERNAL_BUTTON
import com.thanh0x.coursedeals.desktop.TAG_WEBVIEW_RELOAD_BUTTON
import com.thanh0x.coursedeals.desktop.TAG_WEBVIEW_TOOLBAR
import com.thanh0x.coursedeals.shared.App
import com.thanh0x.coursedeals.shared.AppScreen
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.stopKoin
import java.io.File
import java.nio.file.Files

class DesktopFullAppInteractiveE2eTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var env: DesktopFullAppTestEnvironment
    private lateinit var screenshotDir: File
    private var deleteScreenshotDirOnTearDown: Boolean = true

    @Before
    fun setUp() {
        AppVersion.overrideVersionCode = null
        env = installFullAppDesktopTestKoin()
        val configuredDir = System.getenv("DESKTOP_E2E_SCREENSHOT_DIR")?.trim().orEmpty()
        if (configuredDir.isNotEmpty()) {
            screenshotDir = File(configuredDir).apply { mkdirs() }
            deleteScreenshotDirOnTearDown = false
        } else {
            screenshotDir = Files.createTempDirectory("desktop-full-app-e2e-").toFile()
            deleteScreenshotDirOnTearDown = true
        }
    }

    @After
    fun tearDown() {
        AppVersion.overrideVersionCode = null
        runCatching { stopKoin() }
        if (deleteScreenshotDirOnTearDown) {
            screenshotDir.deleteRecursively()
        }
    }

    @Test
    fun `e2e01 splashAndForceUpdateGate interactsWithAllElements`() {
        var launchEpoch by mutableIntStateOf(0)
        var initialScreen by mutableStateOf<AppScreen>(AppScreen.Splash)

        AppVersion.overrideVersionCode = 100
        composeTestRule.setContent {
            key(launchEpoch) {
                App(initialScreen = initialScreen)
            }
        }

        composeTestRule.runDesktopMaestroDsl(screenshotDir) {
            waitForVisible("Update Required")
            assertVisible("Update Required")
            assertVisible("A new version of Course Deals is required to continue.")
            assertVisible("Update Now")
            assertNotVisible("HOME")
            takeScreenshot("e2e01_01_force_update_gate")

            tapOn("Update Now")
        }
        assertTrue(
            "Clicking Update Now must open forceUpdateUrl",
            env.openedUrls.contains("https://coursedeals.thanh0x.com/download"),
        )

        // Now upgrade client version to 105 >= 105 and verify Splash navigates to Login
        AppVersion.overrideVersionCode = 105
        initialScreen = AppScreen.Splash
        launchEpoch++

        composeTestRule.runDesktopMaestroDsl(screenshotDir) {
            waitForVisible("Continue with Email")
            assertVisible("Email")
            assertVisible("Access as Guest")
            assertNotVisible("Update Required")
            takeScreenshot("e2e01_02_nominal_splash_to_login")
        }
    }

    @Test
    fun `e2e02 authLoginOtpAndTermsConsent interactsWithEveryControl`() {
        composeTestRule.setContent {
            App(initialScreen = AppScreen.Login)
        }

        composeTestRule.runDesktopMaestroDsl(screenshotDir) {
            waitForVisible("Continue with Email")
            assertVisible("Email")
            assertVisible("Continue with Google")
            assertVisible("Access as Guest")

            // 1. Empty email validation dialog
            tapOn("Continue with Email")
            waitForVisible("Email cannot be empty")
            tapOn("OK")

            // 2. Valid email -> OTP Verification screen
            tapOn("Email")
            inputText("fulltest@coursedeals.com")
            tapOn("Continue with Email")

            waitForVisible("Verify Your Email")
            assertVisible("fulltest@coursedeals.com")
            assertVisible("6-digit OTP")
            assertVisible("Resend Code")
            assertVisible("Change Email")
            takeScreenshot("e2e02_01_otp_verification")

            // 3. Test Resend Code button
            tapOn("Resend Code")

            // 4. Test Change Email button -> returns to Login, then re-send OTP
            tapOn("Change Email")
            waitForVisible("Continue with Email")
            tapOn("Email")
            eraseText()
            inputText("fulltest@coursedeals.com")
            tapOn("Continue with Email")
            waitForVisible("Verify Your Email")

            // 5. Enter 6-digit OTP -> Terms & Privacy Dialog
            tapOn("6-digit OTP")
            inputText("000000")
            tapOn("Verify OTP")

            waitForVisible("Terms of Service & Privacy Policy")
            assertVisible("Agree & Continue")
            assertVisible("Decline")
            takeScreenshot("e2e02_02_terms_dialog")

            // 6. Test Decline -> shows error dialog, then dismiss & re-verify
            tapOn("Decline")
            waitForVisible("You must agree to the Terms of Service & Privacy Policy")
            tapOn("OK")

            tapOn("Verify OTP")
            waitForVisible("Terms of Service & Privacy Policy")
            tapOn("Agree & Continue")

            // 7. Lands on authenticated HOME feed
            waitForVisible("HOME")
            assertVisible("Complete Web Development Bootcamp 2026")
            takeScreenshot("e2e02_03_authenticated_home")
        }
        assertEquals(1, env.userProfileRepository.termsAcceptedCount)
    }

    @Test
    fun `e2e03 homeFeedSearchFilterSubmitDealAndCourseCards interactsWithEveryControl`() {
        composeTestRule.setContent {
            App(initialScreen = AppScreen.Main, initialRoute = "home")
        }

        composeTestRule.runDesktopMaestroDsl(screenshotDir) {
            waitForVisible("Complete Web Development Bootcamp 2026")
            assertVisible("Professional Diploma in Procurement Management")
            assertVisible("Kotlin Multiplatform & Compose Desktop Masterclass")
            assertVisible("3 deals")
            val initialWebDevTop = nodeTop("Complete Web Development Bootcamp 2026")
            val initialKmpTop = nodeTop("Kotlin Multiplatform & Compose Desktop Masterclass")

            // 1. Toggle timestamp display (Updated <-> Fetched)
            tapOn("Updated")
            waitForVisible("Fetched")
            tapOn("Fetched")
            waitForVisible("Updated")

            // 2. Submit a Deal bottom sheet: invalid URL validation + valid submission + Cancel
            tapOn("Submit")
            waitForVisible("Submit a Deal")
            tapOn("Submit")
            waitForVisible("Please enter a valid Udemy URL")
            tapOn("Course URL")
            inputText("https://www.udemy.com/course/new-deal/?couponCode=FREE2026")
            tapOn("Submit")
            waitForVisible("Deal submitted successfully! It will be reviewed soon.")
            takeScreenshot("e2e03_01_submit_deal_success")
            tapOn("OK")

            // 3. Filter bottom sheet: Category, Language, Sort, Reset, Apply
            tapOn("Filter")
            waitForVisible("Minimum Rating")
            tapOn("Development")
            tapOn("Design")
            tapOn("English")
            tapOn("Rating")
            tapOn("Students")
            tapOn("Expiring Soon")
            takeScreenshot("e2e03_02_filter_sheet_controls")
            tapOn("Reset")
            tapOn("Business")
            tapOn("Apply")

            // Verify filtered results persist across Detail navigation & top Clear Filters button
            waitForVisible("Professional Diploma in Procurement Management")
            assertVisible("Matching 1/3 deals")
            assertNotVisible("Complete Web Development Bootcamp 2026")
            tapOn("Professional Diploma in Procurement Management")
            waitForVisible("Enroll Now")
            tapOn("Back")
            waitForVisible("Professional Diploma in Procurement Management")
            assertVisible("Matching 1/3 deals")
            assertNotVisible("Complete Web Development Bootcamp 2026")
            tapOn("Clear")
            waitForVisible("3 deals")
            assertVisible("Complete Web Development Bootcamp 2026")
            assertVisible("Kotlin Multiplatform & Compose Desktop Masterclass")
            assertTopCourseRowAt(initialWebDevTop, initialKmpTop)

            // 4. Search input, empty state Reset Filters button, Detail back navigation, and Clear search button
            tapOn("Search courses")
            inputText("NonExistentCourseName999")
            waitForVisible("No courses matching your criteria")
            assertVisible("Reset Filters")
            takeScreenshot("e2e03_03_empty_search_state")
            tapOn("Reset Filters")
            waitForVisible("Complete Web Development Bootcamp 2026")

            tapOn("Search courses")
            inputText("Procurement")
            waitForVisible("Professional Diploma in Procurement Management")
            assertVisible("Matching 1/3 deals")
            assertNotVisible("Complete Web Development Bootcamp 2026")
            tapOn("Professional Diploma in Procurement Management")
            waitForVisible("Enroll Now")
            tapOn("Back")
            waitForVisible("Professional Diploma in Procurement Management")
            assertVisible("Matching 1/3 deals")
            assertNotVisible("Complete Web Development Bootcamp 2026")
            tapOn("Clear")
            waitForVisible("Complete Web Development Bootcamp 2026")
            assertVisible("Kotlin Multiplatform & Compose Desktop Masterclass")
            assertVisible("3 deals")
            assertTopCourseRowAt(initialWebDevTop, initialKmpTop)
        }
    }

    private fun nodeTop(text: String): Float = composeTestRule
        .onNodeWithText(text, useUnmergedTree = true)
        .fetchSemanticsNode()
        .boundsInRoot
        .top

    private fun assertTopCourseRowAt(expectedWebDevTop: Float, expectedKmpTop: Float) {
        assertEquals(expectedWebDevTop, nodeTop("Complete Web Development Bootcamp 2026"), 0.5f)
        assertEquals(expectedKmpTop, nodeTop("Kotlin Multiplatform & Compose Desktop Masterclass"), 0.5f)
    }

    @Test
    fun `e2e04 couponDetailBookmarkShareReportAndEnroll interactsWithEveryControl`() {
        composeTestRule.setContent {
            App(initialScreen = AppScreen.Main, initialRoute = "home")
        }

        composeTestRule.runDesktopMaestroDsl(screenshotDir) {
            waitForVisible("Complete Web Development Bootcamp 2026")
            tapOn("Complete Web Development Bootcamp 2026")

            // 1. Verify Detail Screen sections (Hero, Title, Metrics, Pricing, Cache Banner, Description)
            waitForVisible("Enroll Now")
            assertVisible("Complete Web Development Bootcamp 2026")
            assertVisible("Dr. Angela Yu")
            assertVisible("WEBDEV2026")
            assertVisible("DESCRIPTION")
            assertVisible("Report")
            takeScreenshot("e2e04_01_course_detail")

            // 2. Toggle Bookmark icon button & Refresh cache banner
            tapOn("Bookmark")
            tapOn("Bookmark")
            tapOn("Refresh", optional = true)

            // 3. Click Share icon button -> copies link & opens Notice dialog
            tapOn("Share")
            waitForVisible("Link copied to clipboard")
            tapOn("OK")

            // 4. Open Report Deal Bottom Sheet -> test all radio options including Other validation
            tapOn("Report")
            waitForVisible("Report this deal")
            tapOn("Not free")
            tapOn("No data")
            tapOn("Other")
            assertVisible("Please describe the issue")
            tapOnId("report_other_input")
            inputText("Coupon code reached maximum redemptions")
            tapOn("Expired deal")
            takeScreenshot("e2e04_02_report_deal_sheet")
            tapOnId("report_submit_button")

            waitForVisible("Report Submitted")
            assertVisible("Thank you for reporting this deal. Our team will verify it shortly.")
            tapOn("OK")
        }

        assertTrue(
            "Share must copy course title and URL to clipboard",
            env.copiedClipboardEntries.any { it.contains("WEBDEV2026") },
        )
        assertEquals(1, env.couponRepository.reportedCourses.size)
        assertEquals("Expired deal", env.couponRepository.reportedCourses.first().second)
    }

    @Test
    fun `e2e05 enrollAndCourseEmbeddedBrowser interactsWithToolbarAndNavigation`() {
        composeTestRule.setContent {
            App(initialScreen = AppScreen.Main, initialRoute = "home")
        }

        composeTestRule.runDesktopMaestroDsl(screenshotDir) {
            waitForVisible("Complete Web Development Bootcamp 2026")
            tapOn("Complete Web Development Bootcamp 2026")
            waitForVisible("Enroll Now")
            tapOnId("enroll_button")

            // 1. Verify CouponEnrollScreen + DesktopBrowserToolbar
            waitForVisible("udemy.com/course/complete-web-development-bootcamp/")
            assertVisible("Enroll Now")
            tapOnId(TAG_WEBVIEW_TOOLBAR)
            tapOnId(TAG_WEBVIEW_ADDRESS_BAR)
            tapOnId(TAG_WEBVIEW_RELOAD_BUTTON)
            tapOnId(TAG_WEBVIEW_OPEN_EXTERNAL_BUTTON)
            takeScreenshot("e2e05_01_enroll_embedded_browser")

            // 2. Close Enroll screen -> returns to Detail -> Back returns to Home
            tapOn("Cancel")
            waitForVisible("DESCRIPTION")
            tapOn("Back")
            waitForVisible("HOME")

            // 3. Navigate to COURSE tab -> verify embedded browser toolbar for My Courses
            tapOn("COURSE")
            waitForVisible("udemy.com")
            tapOnId(TAG_WEBVIEW_RELOAD_BUTTON)
            tapOnId(TAG_WEBVIEW_OPEN_EXTERNAL_BUTTON)
            takeScreenshot("e2e05_02_course_tab_embedded_browser")
        }

        assertTrue(
            "Opening external browser from Enroll and Course tabs must record URLs",
            env.openedUrls.size >= 2,
        )
    }

    @Test
    fun `e2e06 profileGuestAndAuthenticatedSettingsCategoriesKeywordsLogsAndDeleteAccount interactsWithEveryItem`() {
        composeTestRule.setContent {
            App(initialScreen = AppScreen.Main, initialRoute = "profile")
        }

        composeTestRule.runDesktopMaestroDsl(screenshotDir) {
            // 1. Guest mode auth gate on Profile
            waitForVisible("Sign in to Access Settings")
            assertVisible("Guest User")
            takeScreenshot("e2e06_01_profile_guest_gate")
            tapOn("Sign In")

            // 2. Sign in via Google on LoginScreen -> navigates back to Main
            waitForVisible("Continue with Google")
            tapOn("Continue with Google")
            waitForVisible("HOME")
            tapOn("PROFILE")

            // 3. Authenticated Profile Header & Copy Email
            waitForVisible("google.user@coursedeals.com")
            assertVisible("Course Deals Member")
            tapOn("google.user@coursedeals.com")
            waitForVisible("Email copied to clipboard")
            tapOn("OK")

            // 4. Favorite Categories: open CategoryPickerBottomSheet, toggle chips, Done, remove chip
            tapOn("Add Category")
            waitForVisible("Done")
            tapOn("Design")
            tapOn("Business")
            takeScreenshot("e2e06_02_category_picker_sheet")
            tapOn("Done")
            waitForVisible("Design")
            assertVisible("Business")
            tapOn("Remove category")

            // 5. Favorite Keywords: verify existing keyword chip, remove action, and Add Keyword chip
            assertVisible("Favorite Keywords")
            assertVisible("Kotlin")
            tapOn("Remove keyword")
            tapOn("Add Keyword")

            // 6. Settings switches: Dark Mode & Notifications (+ sub-toggles)
            tapOn("Dark Mode")
            assertTrue(env.userProfileRepository.darkModeEnabled)
            tapOn("Dark Mode")
            assertFalse(env.userProfileRepository.darkModeEnabled)

            assertVisible("All Deals")
            assertVisible("Hot Deals")
            assertVisible("Development")
            tapOn("Notifications")
            assertFalse(env.userProfileRepository.notificationsEnabled)
            tapOn("Notifications")
            assertTrue(env.userProfileRepository.notificationsEnabled)

            // 7. Diagnostic Logs sheet: filter chips ALL/DEBUG/INFO/WARN/ERROR + Copy Logs + Share Logs + Close
            tapOnId("diagnostic_logs_row")
            waitForVisible("Copy Logs")
            tapOnId("log_filter_debug")
            tapOnId("log_filter_info")
            assertVisible("CommonTracingInitializer initialized")
            assertVisible("ColdStart complete in")
            takeScreenshot("e2e06_03_diagnostic_logs_info")
            tapOnId("log_filter_warn")
            tapOnId("log_filter_error")
            tapOnId("log_filter_all")
            assertVisible("CommonTracingInitializer initialized")
            assertVisible("ColdStart complete in")
            takeScreenshot("e2e06_03_diagnostic_logs_sheet")

            tapOnId("copy_logs_button")
            waitForVisible("Logs copied to clipboard")
            tapOn("OK")

            tapOnId("share_logs_button")
            waitForVisible("Link copied to clipboard")
            tapOn("OK")

            tapOnId("close_logs_button")

            // 8. Developer Contact Links (Email & Website)
            tapOn("coursedeals@thanh0x.com")
            tapOn("https://thanh0x.com")

            // 9. Delete Account Dialog -> Cancel -> Re-open -> Confirm Delete -> Returns to Login
            tapOn("Delete Account")
            waitForVisible("Are you sure you want to permanently delete your account")
            takeScreenshot("e2e06_04_delete_account_dialog")
            tapOn("Cancel")

            tapOn("Delete Account")
            waitForVisible("Delete")
            tapOn("Delete")

            waitForVisible("Continue with Email")
            tapOn("OK", optional = true)
            assertVisible("Continue with Email")
            takeScreenshot("e2e06_05_deleted_account_back_to_login")
        }

        assertEquals(1, env.userProfileRepository.accountDeletedCount)
        assertTrue(env.sentEmails.any { it.first == "coursedeals@thanh0x.com" })
        assertTrue(env.openedUrls.contains("https://thanh0x.com"))
    }

    @Test
    fun `e2e07 fullAppRegressionYamlFlow executesAll18StepsEndToEnd`() {
        val repoRoot = File(System.getProperty("user.dir") ?: ".").let { dir ->
            if (File(dir, ".maestro/flows").exists()) dir else (dir.parentFile ?: dir)
        }
        val flowFile = File(repoRoot, ".maestro/flows/10-desktop-full-regression.yaml")
        assertTrue("Expected 10-desktop-full-regression.yaml to exist", flowFile.exists())

        var launchEpoch by mutableIntStateOf(0)
        var currentInitialScreen by mutableStateOf<AppScreen>(AppScreen.Login)
        var hasLaunchedOnce = false

        composeTestRule.setContent {
            key(launchEpoch) {
                App(initialScreen = currentInitialScreen)
            }
        }

        val runner = DesktopMaestroFlowRunner(
            rule = composeTestRule,
            screenshotDir = screenshotDir,
            onLaunchApp = { launchCmd ->
                val versionCode = launchCmd.arguments["override_version_code"]?.toIntOrNull()
                if (versionCode != null) {
                    AppVersion.overrideVersionCode = versionCode
                    currentInitialScreen = AppScreen.Splash
                    launchEpoch++
                } else if (hasLaunchedOnce && (launchCmd.stopApp || launchCmd.clearState)) {
                    AppVersion.overrideVersionCode = null
                    currentInitialScreen = AppScreen.Login
                    launchEpoch++
                }
                hasLaunchedOnce = true
            },
        )

        val result = runner.runFlowFile(flowFile)
        assertEquals(18, result.capturedScreenshots.size)
        for (screenshot in result.capturedScreenshots) {
            assertTrue("Expected screenshot to exist: ${screenshot.name}", screenshot.exists())
            assertTrue("Expected non-empty PNG: ${screenshot.name}", screenshot.length() > 0L)
        }
    }
}
