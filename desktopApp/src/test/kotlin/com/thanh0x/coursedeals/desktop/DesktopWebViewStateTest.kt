package com.thanh0x.coursedeals.desktop

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DesktopWebViewStateTest {

    @Test
    fun `isValidHttpUrl accepts valid http and https URLs`() {
        assertTrue(isValidHttpUrl("https://www.udemy.com/courses/development/"))
        assertTrue(isValidHttpUrl("http://localhost:8081/api/v1/coupons"))
        assertTrue(isValidHttpUrl("  HTTPS://udemy.com/course/kotlin/?couponCode=DEAL2026  "))
    }

    @Test
    fun `isValidHttpUrl rejects blank malformed or non-http schemes`() {
        assertFalse(isValidHttpUrl(""))
        assertFalse(isValidHttpUrl("   "))
        assertFalse(isValidHttpUrl("javascript:alert(1)"))
        assertFalse(isValidHttpUrl("file:///etc/passwd"))
        assertFalse(isValidHttpUrl("ftp://files.example.com/archive.zip"))
        assertFalse(isValidHttpUrl("https://"))
        assertFalse(isValidHttpUrl("not a url at all"))
    }

    @Test
    fun `formatDisplayHost extracts clean host and path for address bar`() {
        assertEquals(
            "udemy.com/courses/development/",
            formatDisplayHost("https://www.udemy.com/courses/development/?couponCode=FREE"),
        )
        assertEquals(
            "udemy.com",
            formatDisplayHost("https://www.udemy.com/"),
        )
        assertEquals(
            "about:blank",
            formatDisplayHost("   "),
        )
        assertEquals(
            "invalid-url",
            formatDisplayHost("invalid-url"),
        )
    }

    @Test
    fun `DesktopBrowserUiState transitions across start progress success and error states`() {
        val initial = DesktopBrowserUiState(currentUrl = "https://www.udemy.com/")
        assertFalse(initial.isLoading)
        assertEquals(0f, initial.progress)
        assertNull(initial.errorMessage)

        val started = initial.onLoadStarted("https://www.udemy.com/courses/development/")
        assertTrue(started.isLoading)
        assertEquals("https://www.udemy.com/courses/development/", started.currentUrl)
        assertEquals(0.1f, started.progress)
        assertNull(started.errorMessage)

        val midProgress = started.onProgressChanged(0.65f)
        assertEquals(0.65f, midProgress.progress)
        assertEquals(1f, started.onProgressChanged(1.5f).progress)
        assertEquals(0f, started.onProgressChanged(-0.4f).progress)

        val succeeded = midProgress.onLoadSucceeded(
            loadedUrl = "https://www.udemy.com/courses/development/",
            title = "Online Development Courses | Udemy",
            canGoBack = true,
            canGoForward = false,
        )
        assertFalse(succeeded.isLoading)
        assertEquals(1f, succeeded.progress)
        assertEquals("Online Development Courses | Udemy", succeeded.pageTitle)
        assertTrue(succeeded.canGoBack)
        assertFalse(succeeded.canGoForward)
        assertNull(succeeded.errorMessage)

        val failed = succeeded.onLoadFailed("Connection timed out")
        assertFalse(failed.isLoading)
        assertEquals(0f, failed.progress)
        assertEquals("Connection timed out", failed.errorMessage)

        val retried = failed.onLoadStarted()
        assertTrue(retried.isLoading)
        assertNull(retried.errorMessage)
    }
}
