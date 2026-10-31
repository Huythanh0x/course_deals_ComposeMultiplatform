package com.thanh0x.coursedeals.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.thanh0x.coursedeals.core.ui.util.PlatformActions
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    PlatformActions.onCopyToClipboard = { text ->
        UIPasteboard.generalPasteboard.string = text
    }
    PlatformActions.onOpenUrl = { urlString ->
        NSURL.URLWithString(urlString)?.let { nsUrl ->
            UIApplication.sharedApplication.openURL(
                url = nsUrl,
                options = emptyMap<Any?, Any>(),
                completionHandler = null,
            )
        }
    }
    PlatformActions.onSendEmail = { email, subject ->
        val encodedSubject = subject.replace(" ", "%20")
        val mailUrl = NSURL.URLWithString("mailto:$email?subject=$encodedSubject")
        if (mailUrl != null && UIApplication.sharedApplication.canOpenURL(mailUrl)) {
            UIApplication.sharedApplication.openURL(
                url = mailUrl,
                options = emptyMap<Any?, Any>(),
                completionHandler = null,
            )
            true
        } else {
            false
        }
    }
    PlatformActions.onShareLink = { title, url ->
        UIPasteboard.generalPasteboard.string = "$title - $url"
    }

    return ComposeUIViewController {
        App()
    }
}
