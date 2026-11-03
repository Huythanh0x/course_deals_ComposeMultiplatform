package com.thanh0x.coursedeals.desktop

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

        composeTestRule.setContent {
            MaterialTheme {
                DesktopBrowserToolbar(
                    url = "https://www.udemy.com/courses/development/",
                    canGoBack = true,
                    canGoForward = false,
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
    fun `disabledPath DesktopWebView with native engine disabled renders placeholder`() {
        composeTestRule.setContent {
            MaterialTheme {
                DesktopWebView(
                    url = "https://www.udemy.com/",
                    enableNativeWebEngine = false,
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TAG_WEBVIEW_DISABLED).assertIsDisplayed()
        composeTestRule.onNodeWithText("Native Web Engine Disabled (Preview / Test Mode)").assertIsDisplayed()
    }
}
