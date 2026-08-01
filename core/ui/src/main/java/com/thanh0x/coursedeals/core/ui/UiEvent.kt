package com.thanh0x.coursedeals.core.ui

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class ShowAlertDialog(val title: String, val message: String) : UiEvent()
    data class Navigate(val destination: String) : UiEvent()
    object HideKeyboard : UiEvent()
}
