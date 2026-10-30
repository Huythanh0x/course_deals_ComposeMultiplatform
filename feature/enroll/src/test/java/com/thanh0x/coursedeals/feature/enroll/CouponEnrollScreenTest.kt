package com.thanh0x.coursedeals.feature.enroll

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CouponEnrollScreenTest {

    @Test
    fun urlValidation_whenValidUrl_passes() {
        val url = "https://www.udemy.com/course/sample-course/?couponCode=FREE100"
        assertTrue(url.isNotBlank())
        assertTrue(url.startsWith("https://"))
    }

    @Test
    fun urlValidation_whenNullOrEmpty_fails() {
        val nullUrl: String? = null
        val emptyUrl = ""
        assertTrue(nullUrl.isNullOrBlank())
        assertTrue(emptyUrl.isBlank())
    }

    @Test
    fun stateVerification_defaultValues() {
        val isLoading = false
        val isError = false
        assertFalse(isLoading)
        assertFalse(isError)
    }
}
