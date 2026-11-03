package com.thanh0x.coursedeals.desktop

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
            "udemy.com/",
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
}
