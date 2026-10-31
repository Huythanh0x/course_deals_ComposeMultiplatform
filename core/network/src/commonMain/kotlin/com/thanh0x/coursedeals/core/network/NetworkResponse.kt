package com.thanh0x.coursedeals.core.network

class NetworkResponse<T>(
    val isSuccessful: Boolean,
    val code: Int,
    val message: String,
    val body: T?
) {
    fun body(): T? = body
    fun message(): String = message
    fun code(): Int = code

    companion object {
        fun <T> success(body: T?, code: Int = 200, message: String = "OK"): NetworkResponse<T> =
            NetworkResponse(
                isSuccessful = true,
                code = code,
                message = message,
                body = body
            )

        fun <T> error(code: Int, message: String, body: T? = null): NetworkResponse<T> =
            NetworkResponse(
                isSuccessful = false,
                code = code,
                message = message,
                body = body
            )
    }
}
