package com.batdaulaptrinh.freeudemycoupons.util

import android.util.Patterns

object ValidationHelper {
    fun isEmailPasswordEmpty(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            return true
        }
        return false
    }

    fun isEmailAddressValidFormat(email: String): Boolean {
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return true
        }
        return false
    }
}