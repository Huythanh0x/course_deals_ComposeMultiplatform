package com.thanh0x.coursedeals.core.ui.util

object PlatformActions {
    var onCopyToClipboard: ((String) -> Unit)? = null
    var onOpenUrl: ((String) -> Unit)? = null
    var onSendEmail: ((email: String, subject: String) -> Boolean)? = null
    var onShareLink: ((title: String, url: String) -> Unit)? = null
    var onApplyDarkMode: ((Boolean) -> Unit)? = null
    var onShowToast: ((String) -> Unit)? = null

    fun copyToClipboard(text: String) {
        onCopyToClipboard?.invoke(text)
    }

    @Suppress("UnusedParameter")
    fun copyToClipboard(label: String, text: String) {
        onCopyToClipboard?.invoke(text)
    }

    fun openUrl(url: String) {
        onOpenUrl?.invoke(url)
    }

    fun sendEmail(email: String, subject: String): Boolean {
        return onSendEmail?.invoke(email, subject) ?: true
    }

    fun shareLink(title: String, url: String) {
        onShareLink?.invoke(title, url)
    }

    fun applyDarkMode(enabled: Boolean) {
        onApplyDarkMode?.invoke(enabled)
    }

    fun showToast(message: String) {
        onShowToast?.invoke(message)
    }
}
