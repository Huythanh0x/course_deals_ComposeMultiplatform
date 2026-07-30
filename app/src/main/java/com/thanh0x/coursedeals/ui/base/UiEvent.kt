package com.thanh0x.coursedeals.ui.base

/**
 * Represents one-time UI events like Navigation, Toasts, or Dialogs.
 * Using SharedFlow for these ensures they aren't re-emitted on configuration changes.
 */
sealed interface UiEvent {
    data class ShowToast(val message: String) : UiEvent
    data class ShowErrorDialog(val title: String, val message: String) : UiEvent
    data class Navigate(val destination: Any) : UiEvent // Any for simple navigation triggers
}
