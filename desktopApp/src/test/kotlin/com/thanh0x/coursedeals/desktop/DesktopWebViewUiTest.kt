package com.thanh0x.coursedeals.desktop

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.thanh0x.coursedeals.core.ui.util.PlatformActions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DesktopWebViewUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `happyPath DesktopBrowserToolbar displays formatted URL and invokes navigation callbacks`() {
        var backClicked = false
        var forwardClicked = false
        var reloadClicked = false
        var externalClicked = false

        val uiState = DesktopBrowserUiState(
            currentUrl = "https://www.udemy.com/courses/development/",
            pageTitle = "Development Courses",
            isLoading = false,
            progress = 1f,
            canGoBack = true,
            canGoForward = false,
        )

        composeTestRule.setContent {
            MaterialTheme {
                DesktopBrowserToolbar(
                    uiState = uiState,
                    onBackClick = { backClicked = true },
                    onForwardClick = { forwardClicked = true },
                    onReloadClick = { reloadClicked = true },
                    onOpenExternalClick = { externalClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithTag(TAG_WEBVIEW_TOOLBAR).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TAG_WEBVIEW_ADDRESS_BAR).assertIsDisplayed()
        composeTestRule.onNodeWithText("udemy.com/courses/development/").assertIsDisplayed()

        composeTestRule.onNodeWithTag(TAG_WEBVIEW_BACK_BUTTON)
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
        assertTrue(backClicked)

        composeTestRule.onNodeWithTag(TAG_WEBVIEW_FORWARD_BUTTON)
            .assertIsDisplayed()
            .assertIsNotEnabled()
        assertEquals(false, forwardClicked)

        composeTestRule.onNodeWithTag(TAG_WEBVIEW_RELOAD_BUTTON)
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
        assertTrue(reloadClicked)

        composeTestRule.onNodeWithTag(TAG_WEBVIEW_OPEN_EXTERNAL_BUTTON)
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
        assertTrue(externalClicked)
    }

    @Test
    fun `failedPath DesktopWebView with invalid URL renders error card and fires onPageError`() {
        var pageErrorCount = 0
        var openedExternalUrl: String? = null
        PlatformActions.onOpenUrl = { url -> openedExternalUrl = url }

        composeTestRule.setContent {
            MaterialTheme {
                DesktopWebView(
                    url = "invalid-url",
                    onPageError = { pageErrorCount++ },
                    enableNativeWebEngine = false,
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TAG_WEBVIEW_TOOLBAR).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TAG_WEBVIEW_ERROR_CARD).assertIsDisplayed()
        composeTestRule.onNodeWithText("Unable to load page").assertIsDisplayed()
        composeTestRule.onNodeWithText("Invalid or unsupported URL: invalid-url").assertIsDisplayed()
        assertEquals(1, pageErrorCount)

        composeTestRule.onNodeWithTag(TAG_WEBVIEW_RETRY_BUTTON)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(2, pageErrorCount)

        composeTestRule.onNodeWithTag(TAG_WEBVIEW_OPEN_EXTERNAL_BUTTON)
            .assertIsDisplayed()
            .performClick()
        assertEquals("invalid-url", openedExternalUrl)
    }

    @Test
    fun `errorPath DesktopBrowserErrorCard displays network failure reason and triggers retry and external actions`() {
        var retryClicked = false
        var openExternalClicked = false

        composeTestRule.setContent {
            MaterialTheme {
                DesktopBrowserErrorCard(
                    url = "https://www.udemy.com/course/kotlin-for-java-developers/",
                    errorMessage = "SSL handshake timed out",
                    onRetryClick = { retryClicked = true },
                    onOpenExternalClick = { openExternalClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithTag(TAG_WEBVIEW_ERROR_CARD).assertIsDisplayed()
        composeTestRule.onNodeWithText("Unable to load page").assertIsDisplayed()
        composeTestRule.onNodeWithText("SSL handshake timed out").assertIsDisplayed()

        composeTestRule.onNodeWithTag(TAG_WEBVIEW_RETRY_BUTTON)
            .assertIsDisplayed()
            .performClick()
        assertTrue(retryClicked)

        composeTestRule.onNodeWithText("Open in Browser")
            .assertIsDisplayed()
            .performClick()
        assertTrue(openExternalClicked)
    }
}
