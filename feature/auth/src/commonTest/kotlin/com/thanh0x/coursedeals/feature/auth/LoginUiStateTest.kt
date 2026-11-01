package com.thanh0x.coursedeals.feature.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LoginUiStateTest {

    @Test
    fun defaultState_isCorrect() {
        val state = LoginUiState()
        assertFalse(state.isLoading)
        assertFalse(state.isFingerprintEnabled)
        assertFalse(state.showTermsDialog)
        assertNull(state.pendingTokenData)
        assertEquals(AuthMode.LOGIN, state.authMode)
        assertEquals("", state.pendingEmail)
        assertNull(state.infoMessage)
        assertNull(state.error)
    }

    @Test
    fun customState_valuesAreRetained() {
        val state = LoginUiState(
            isLoading = true,
            isFingerprintEnabled = true,
            authMode = AuthMode.REGISTER,
            pendingEmail = "test@example.com",
            infoMessage = "Check your email",
            error = "Invalid format",
        )
        assertTrue(state.isLoading)
        assertTrue(state.isFingerprintEnabled)
        assertEquals(AuthMode.REGISTER, state.authMode)
        assertEquals("test@example.com", state.pendingEmail)
        assertEquals("Check your email", state.infoMessage)
        assertEquals("Invalid format", state.error)
    }

    @Test
    fun authModes_allValuesPresent() {
        val modes = AuthMode.values()
        assertEquals(3, modes.size)
        assertTrue(modes.contains(AuthMode.LOGIN))
        assertTrue(modes.contains(AuthMode.REGISTER))
        assertTrue(modes.contains(AuthMode.VERIFY_OTP))
    }
}
